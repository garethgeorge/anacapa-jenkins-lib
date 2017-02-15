def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    node {
        stage('Hello World') {
            sh 'echo "Hello World!"'
            sh 'env'
        }
    }
}
