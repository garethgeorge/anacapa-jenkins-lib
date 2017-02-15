def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    node {
        stage('Hello World') {
            sh "echo \"Grading ${config.course_org}/${config.lab_name}-${config.github_user}\""
            sh 'env'
        }
    }
}
