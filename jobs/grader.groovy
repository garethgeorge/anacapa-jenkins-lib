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
        course_org = build.environment.get("course_org")
        credentials_id = build.environment.get("credentials_id")
        lab_name = build.environment.get("lab_name")
        github_user = build.environment.get("github_user")
      }
	    '''.stripIndent())
      sandbox()
    }
  }
}
