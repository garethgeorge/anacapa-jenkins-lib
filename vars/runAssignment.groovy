import static edu.ucsb.cs.anacapa.pipeline.Lib.*

def call(body) {
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  def course_org = config.course_org.trim()
  def lab_name = config.lab_name.trim()
  def credentials_id = config.credentials_id.trim()
  def assignment = [:]

  node {
    stage('Start runAssignment') {
      sh "echo 'Creating/Updating ${course_org}/assignment-${lab_name}'"
    }

    stage("Checkout Assignment Reference Repo") {
      // start with a clean workspace
      step([$class: 'WsCleanup'])
      checkout([
        $class: 'GitSCM',
        branches: [[name: '*/master']],
        userRemoteConfigs: [
          [url:"https://github.com/${course_org}/assignment-${lab_name}.git"],
          [credentialsId:"${credentials_id}"]
        ]
      ])
      stash name: 'fresh'
    }

    /* Make sure the assignment spec conforms to the proper conventions */
    stage('validateJSON') {
      assignment = parseJSON(readFile(spec_file))
      // TODO: insert validation step here...
      //    * This allows us to guarantee that the object has certain properties
      if (assignment == null) { sh 'fail' }
    }

    /* Generate the build stages to run the tests */
    stage('Generate Testing Stages') {
      /* for each test group */
      def testables = assignment['testables']
      for (int index = 0; index < testables.size(); index++) {
        def i = index
        def curtest = testables[index]
        /* create a parallel group */
        stage(curtest['test_name']) {
          run_test_group(curtest)
        }
      }
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
  node('submit') {
    // assume built at first
    def built = true
    // clean up the workspace for this particular test group and start fresh
    step([$class: 'WsCleanup'])
    unstash 'fresh'

    /* Try to build the binaries for the current test group */
    try {
      // execute the desired build command
      sh testable.build_command
      // save this state so each individual test case can run independently
      stash name: testable.test_name
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
      println("Build Failed...")
      for (int i = 0; i < testable.test_cases.size(); i++) {
        def index = i
        def expected = testable.test_cases[index]['expected']
        def output_name = solution_output_name(testable, test_case)
        // if we needed to generate, fail because that's not possible.
        if (expected.equals('generate')) { sh 'fail' }
        save_result("cat ${expected}", output_name)
      }
    }
  }
}

def run_test_case(testable, test_case) {
  def command = test_case.command

  try {
    // refresh the workspace to facilitate test case independence
    step([$class: 'WsCleanup'])
    unstash name: testable.test_name
    def expected = test_case['expected']
    // save the output for this test case
    def output_name = solution_output_name(testable, test_case)

    if (expected.equals('generate')) {
      save_result(test_case.command, output_name)
    } else {
      save_result("cat ${expected}", output_name)
    }
  } catch (e) {
    println("Failed to run test case")
    println(e)
  }
}
