import static edu.ucsb.cs.anacapa.pipeline.Lib.*

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def git_provider_domain = config.git_provider_domain.trim()
    def course_org = config.course_org.trim()
    def lab_name = config.lab_name.trim()
    def github_user = config.github_user.trim()
    def credentials_id = config.credentials_id.trim()
    def commit = config.commit.trim()
    def assignment = [:]

    node {
        stage('Start runGrader') {
            sh "echo \"Grading ${git_provider_domain}/${course_org}/${lab_name}-${github_user}\""
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
        }

        stage("Stash reference data") {
          dir(".anacapa") {
            assignment = parseJSON(readFile("assignment_spec.json"))
            // save off the assignment_spec
            stash name: "assignment_spec", includes: "assignment_spec.json"
            // save off build data
            dir("build_data") {
              sh 'touch .keep'
              stash name: "build_data"
            }
            // save off test data
            dir("test_data") {
              sh 'touch .keep'
              stash name: "test_data"
            }
            // save off expected outputs
            dir("expected_outputs") {
              sh 'touch .keep'
              copy_solution_artifacts(git_provider_domain, course_org, lab_name, assignment)
              stash name: "expected_outputs"
            }
          }
        }

        stage("Clean WS, Checkout ${git_provider_domain}/${course_org}/${lab_name}-${github_user}") {
          // start with a clean workspace
          step([$class: 'WsCleanup'])
          checkout scm: [
            $class: 'GitSCM',
            branches: [[name: "${commit}"]],
            userRemoteConfigs: [[
              url:"https://${git_provider_domain}/${course_org}/${lab_name}-${github_user}.git",
    		      credentialsId: "${credentials_id}",
            ]]
          ]
          // save the current directory as the "fresh" start state
          stash name: 'fresh'
        }

        /* for each test group */
        def testables = assignment['testables']
        for (int index = 0; index < testables.size(); index++) {
          def i = index
          def curtest = testables[index]

          run_test_group(curtest)
        }
    }

    node {
      stage('Report Results') {
        def testables = assignment.testables
        // initial JSON result object
        def test_results = [
          assignment_name: assignment['assignment_name'],
          repo: "${course_org}/${lab_name}-${github_user}",
          results: []
        ]
        // for each testable, unstash the results and put them in the final obj
        for (int index = 0; index < testables.size(); index++) {
          def i = index
          def curtest = testables[index]
          // gather the partial results from this testable
          unstash "${slugify(curtest.test_name)}_results"
          def tmp_results = readFile(
            temp_results_file_for(curtest.test_name)
          ).split("\r?\n")
          for(int j = 0; j < tmp_results.size(); j++) {
            def realj = j
            test_results.results << parseJSON(tmp_results[j])
          }
        }

        def test_results_json = jsonString(test_results, pretty=true)
        println(test_results_json)
        // write out complete test results to a file and archive it
        sh "echo '${test_results_json}' > grade.json"
        archiveArtifacts artifacts: "grade.json", fingerprint: true
        // clean up the workspace for the next build
        step([$class: 'WsCleanup'])
      }
    }
}

def solution_output_name(testable, test_case) {
  return slugify("${testable.test_name}_${test_case.command}_solution")
}

def copy_solution_artifacts(git_provider_domain, course_org, lab_name, assignment) {
  def master_project = "AnacapaGrader ${git_provider_domain} ${course_org} assignment-${lab_name}"
  def testables = assignment['testables']
  // for each testable
  for (int index = 0; index < testables.size(); index++) {
    def i = index
    def testable = testables[i]
    // for each test case in this testable
    for (int jindex = 0; jindex < testable.test_cases.size(); jindex++) {
      def j = jindex
      def solution_name = solution_output_name(testable, testable.test_cases[j])
      // copy over the solution artifact
      step ([$class: 'CopyArtifact',
              projectName: master_project,
              filter: solution_name])
    }
  }
}

def temp_results_file_for(name) {
  return ".anacapa.tmp_results_${slugify(name)}"
}

def save_temp_result(testable, test_case, score) {
  def jstr = jsonString([
    test_group: testable['test_name'],
    test_name: test_case['command'],
    score: score,
    max_score: test_case['points']
  ])

  unstash "${slugify(testable.test_name)}_results"
  // stash the partial results so the master can gather them all in the end
  sh "echo '${jstr}' >> ${temp_results_file_for(testable['test_name'])}"
  stash includes: temp_results_file_for(testable.test_name), name: "${slugify(testable.test_name)}_results"
}


def run_test_group(testable) {
  stage(testable['test_name']) {
    // assume built at first
    def built = true
    // clean up the workspace for this particular test group and start fresh
    step([$class: 'WsCleanup'])
    unstash 'fresh'
    // make sure we're starting from scratch for the partial results
    if (fileExists(temp_results_file_for(testable.test_name))) {
      sh "rm ${temp_results_file_for(testable.test_name)}"
    }

    /* Try to build the binaries for the current test group */
    try {
      sh 'find . -print | sort > _fresh'
      unstash 'build_data'
      sh 'find . -print | sort > _fresh_w_build'
      sh 'comm -13 _fresh _fresh_w_build > _build_files'

      // execute the desired build command
      sh testable.build_command
    } catch (e) {
      // if something went wrong building this test case, assume all
      // test cases will fail
      built = false
      println(e)
    } finally {
      // remove build data
      sh 'xargs rm < _build_files'
      def _ = sh returnStatus: true, script: 'rm _fresh_w_build _build_files'
      // new state is fresh state + compiled targets
      // save this state so each individual test case can run independently
      stash name: testable.test_name
    }

    // make sure the temporary results file exists and is stashed but empty
    sh "touch ${temp_results_file_for(testable['test_name'])}"
    stash includes: temp_results_file_for(testable.test_name), name: "${slugify(testable.test_name)}_results"

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
        save_temp_result(testable, testable.test_cases[index], 0)
      }
    }
  }
}


def run_test_case(testable, test_case) {
  def command = test_case.command
  // assume perfect score to begin with (we will set it to 0 upon failure)
  def score = test_case.points

  try {
    // refresh the workspace to facilitate test case independence
    step([$class: 'WsCleanup'])
    unstash name: testable.test_name

    // get the test_data folder contents
    unstash 'test_data'

    // save the output for this test case
    def output_name = slugify("${testable.test_name}_${test_case.command}_output")
    sh "${test_case.command} > ${output_name}"
    // done running student code.

    // get the expected outputs folder contents
    unstash 'expected_outputs'

    def solution_name = solution_output_name(testable, test_case)
    def solution_file = solution_name

    def diff_source = test_case.diff_source
    if (diff_source.equals('stdout')) {
      diff_source = output_name
    }

    def diff_cmd = "diff ${output_name} ${solution_file} > ${output_name}.diff"
    def ret = sh returnStatus: true, script: diff_cmd


    sh "cat ${output_name}.diff"
    if (ret != 0) {
      score = 0
      archiveArtifacts artifacts: "${output_name}.diff", fingerprint: true
    }
  } catch (e) {
    println("Failed to run test case")
    println(e)
    score = 0 // fail
  } finally {
    save_temp_result(testable, test_case, score)
    step([$class: 'WsCleanup'])
  }
}
