folder("Anacapa Grader/${course_org}")
pipelineJob("Anacapa Grader/${course_org}/grader-${lab_name}") {
  parameters {
    stringParam('github_user', '', 'The github username(s) of the student(s)')
  }
  environmentVariables {
    envs(
      course_org: "${course_org}",
      credentials_id: "${credentials_id}",
      lab_name: "${lab_name}"
    )
  }
  definition {
    cps {
      script('''
      @Library('anacapa-jenkins-lib') _
      runGrader {
        course_org = "${course_org}"
        credentials_id = "${credentials_id}"
        lab_name = "${lab_name}"
        github_user = "${github_user}"
      }

      node {
          sh 'env > .env.txt'
          readFile('.env.txt').split("\\r?\\n").each {
              println it
          }
      }
	    '''.stripIndent())
      sandbox()
    }
  }
}
