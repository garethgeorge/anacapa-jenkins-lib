import static edu.ucsb.cs.anacapa.pipeline.Lib.*

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def course_org = config.course_org.trim()
    def lab_name = config.lab_name.trim()
    def github_user = config.github_user.trim()
    def credentials_id = config.credentials_id.trim()
    def assignment = [:]

    node {

        stage('Hello World') {
            sh "echo \"Grading ${course_org}/${lab_name}-${github_user}\""
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
        }

        stage("Stash reference data") {
          assignment = parseJSON(readFile("assignment_spec.json"))
          stash name: "assignment_spec", includes: "assignment_spec.json"
          dir("resources") {
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
        }
        stage("Clean WS, Checkout ${course_org}/${lab_name}-${github_user}") {
          // start with a clean workspace
          step([$class: 'WsCleanup'])
          checkout([
            $class: 'GitSCM',
            branches: [[name: '*/master']],
            userRemoteConfigs: [
              [url:"https://github.com/${course_org}/${lab_name}-${github_user}.git"],
              [credentialsId:"${credentials_id}"]
            ]
          ])
          // save the current directory as the "fresh" start state
          stash name: 'fresh'
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

        stage('Report Results') {
          def testables = assignment.testables
          def test_results = [
            assignment_name: assignment['assignment_name'],
            repo: env.JOB_NAME,
            results: []
          ]
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
          def name = slugify("${env.JOB_NAME}_test_results")
          def test_results_json = jsonString(test_results, pretty=true)
          println(test_results_json)
          // write out complete test results to a file and archive it
          sh "echo '${test_results_json}' > ${name}.json"
          archiveArtifacts artifacts: "${name}.json", fingerprint: true
          // clean up the workspace for the next build
          step([$class: 'WsCleanup'])
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
  sh "echo '${jstr}' >> ${temp_results_file_for(testable['test_name'])}"
}


def run_test_group(testable) {
  node('submit') {
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
      dir("resources") {
        dir("build_data") {
          // unstash the build data
          unstash 'build_data'
        }
      }
      // execute the desired build command
      sh testable.build_command
      // remove build data
      dir("resources") { deleteDir() }
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
        save_temp_result(testable, testable.test_cases[index], 0)
      }
    }

    // stash the partial results so the master can gather them all in the end
    stash includes: temp_results_file_for(testable.test_name), name: "${slugify(testable.test_name)}_results"
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
    dir("resources") {
      dir("test_data") {
        // get the test_data folder contents
        unstash 'test_data'
      }
    }
    // save the output for this test case
    def output_name = slugify("${testable.test_name}_${test_case.command}_output")
    sh "${test_case.command} > ${output_name}"

    // remove test data
    dir("resources") { deleteDir() }
    dir("resources") {
      dir("expected_outputs") {
        // get the expected outputs folder contents
        unstash 'expected_outputs'
      }
    }

    if (test_case['expected'].equals('generate')) {
      // TODO: figure out how/when to generate the expected outputs
    } else {
      def diff_source = test_case.diff_source
      if (diff_source.equals('stdout')) {
        diff_source = output_name
      }

      def diff_cmd = "diff ${output_name} ${test_case['expected']} > ${output_name}.diff"
      def ret = sh returnStatus: true, script: diff_cmd
      // remove expected outputs data
      dir("resources") { deleteDir() }

      sh "cat ${output_name}.diff"
      if (ret != 0) {
        score = 0
        archiveArtifacts artifacts: "${output_name}.diff", fingerprint: true
      }
    }
  } catch (e) {
    println("Failed to run test case")
    println(e)
    score = 0 // fail
  } finally {
    save_temp_result(testable, test_case, score)
  }
}
