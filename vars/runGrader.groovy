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
        }
        stage("Look around") {
          sh 'ls -al'
        }
    }
}
