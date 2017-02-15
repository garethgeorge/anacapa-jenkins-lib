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
          def evars = readFile('.env.txt').split("\\r?\\n")
          for (int index = 0; index < evars.size(); index++) {
              def i = index
              println evars[i]
          }
      }
	    '''.stripIndent())
      sandbox()
    }
  }
}
