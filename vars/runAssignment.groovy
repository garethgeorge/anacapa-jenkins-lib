import static edu.ucsb.cs.anacapa.pipeline.Lib.*

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def course_org = config.course_org.trim()
    def lab_name = config.lab_name.trim()

    node {
        stage('Hello World') {
            sh "echo 'Creating/Updating ${course_org}/assignment-${lab_name}'"
        }
    }
}
