import static edu.ucsb.cs.anacapa.pipeline.Lib.*

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def cfgstr = jsonString(config)
    def course_org = config.course_org.trim()
    def lab_name = config.lab_name.trim()
    def github_user = config.github_user.trim()

    node {
        stage('Hello World') {
            sh "echo ${cfgstr}"
            sh "echo \"Grading ${course_org}/${lab_name}-${github_user}\""
            // sh 'env'
        }
    }
}
