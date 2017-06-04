import static edu.ucsb.cs.anacapa.pipeline.Lib.*

def call(body) {
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  def git_provider_domain = config.git_provider_domain.trim()
  def course_org = config.course_org.trim()
  def lab_name = config.lab_name.trim()
  def credentials_id = config.credentials_id.trim()
  def spec_file = ".anacapa/assignment_spec.json"
  def assignment = [:]

  node {
    stage('Start runAssignment') {
      sh "echo 'Creating/Updating ${git_provider_domain}/${course_org}/assignment-${lab_name}'"
    }
  }

  node('submit') {
    stage("Checkout Assignment Reference Repo") {
      // start with a clean workspace
      step([$class: 'WsCleanup'])
      checkout scm: [
        $class: 'GitSCM',
        branches: [[name: "**/master"]],
        userRemoteConfigs: [[
          url:"https://${git_provider_domain}/${course_org}/assignment-${lab_name}.git",
		      credentialsId: "${credentials_id}",
        ]]
      ]

      dir(".anacapa") {
        dir("build_data") {
          sh 'touch .keep'
          stash name: "build_data"
        }
        dir("test_data") {
          sh 'touch .keep'
          stash name: "test_data"
        }
        dir("expected_outputs") {
          sh 'touch .keep'
          stash name: "expected_outputs"
        }
      }
      stash name: 'fresh'
    }

    /* Make sure the assignment spec conforms to the proper conventions */
    stage('validateJSON') {
      assignment = parseJSON(readFile(spec_file))
      // TODO: insert validation step here...
      //    * This allows us to guarantee that the object has certain properties
      if (assignment == null) { sh 'fail' }
    }

    /* for each test group */
    def testables = assignment['testables']
    for (int index = 0; index < testables.size(); index++) {
      def i = index
      def curtest = testables[index]

      run_test_group(curtest)
    }
  }
}

def solution_output_name(testable, test_case) {
  return slugify("${testable.test_name}_${test_case.command}_solution")
}

def save_result(command, output_name) {
  sh "${command} > ${output_name}"
  archiveArtifacts artifacts: "${output_name}", fingerprint: true
}

def run_test_group(testable) {
  stage(testable['test_name']) {
    // assume built at first
    def built = true
    // clean up the workspace for this particular test group and start fresh
    step([$class: 'WsCleanup'])
    unstash 'fresh'

    /* Try to build the binaries for the current test group */
    try {
      unstash 'build_data'
      // execute the desired build command
      sh testable.build_command
      // save this state so each individual test case can run independently
      stash name: slugify(testable.test_name)
    } catch (e) {
      // if something went wrong building this test case, assume all
      // test cases will fail
      built = false
      println(e)
    }

    if (built) {
      println("Successfully built!")
      for (int i = 0; i < testable.test_cases.size(); i++) {
        def index = i
        run_test_case(testable, testable.test_cases[index])
      }
    } else {
      println("Build Failed... Checking for expectd output file")
      for (int i = 0; i < testable.test_cases.size(); i++) {
        def index = i
        def expected = testable.test_cases[index]['expected'] - ".anacapa/expected_outputs/"
        def output_name = solution_output_name(testable, testable.test_cases[index])
        // if we needed to generate, fail because that's not possible.
        if (expected.equals('generate')) { sh 'fail' }
        unstash 'expected_outputs'
        save_result("cat ${expected}", output_name)
      }
    }
  }
}

def run_test_case(testable, test_case) {
  def command = test_case.command
  def expected = test_case['expected'] - ".anacapa/expected_outputs/"
  def output_name = solution_output_name(testable, test_case)

  try {
    // refresh the workspace to facilitate test case independence
    step([$class: 'WsCleanup'])
    unstash name: unstash(testable.test_name)
    // save the output for this test case

    if (expected.equals('generate')) {
      unstash 'test_data'
      save_result(test_case.command, output_name)
    } else {
      unstash 'expected_outputs'
      save_result("cat ${expected}", output_name)
    }
  } catch (e) {
    println("Failed to run test case")
    println(e)
    if (! expected.equals('generate')) {
      unstash 'expected_outputs'
      save_result("cat ${expected}", output_name)
    }
  }
}
