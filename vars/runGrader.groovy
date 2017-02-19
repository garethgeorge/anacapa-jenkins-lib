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
        stage("Checkout ${course_org}/${lab_name}-${github_user}") {
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
          sh 'ls'
        }
    }
}
