import static edu.ucsb.cs.anacapa.pipeline.Lib.*

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def cfgstr = jsonString(config)

    node {
        stage('Hello World') {
            sh "echo ${cfgstr}"
            sh 'echo "Hello World!"'
            sh 'env'
        }
    }
}
