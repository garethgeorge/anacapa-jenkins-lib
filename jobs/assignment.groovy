folder("Anacapa Grader/${course_org}")
pipelineJob("Anacapa Grader/${course_org}/assignment-${lab_name}") {
  scm {
    git {
	    remote {
        github("${course_org}/assignment-${lab_name}")
        credentials("${credentials_id}")
      }
    }
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
      runAssignment {
        course_org = build.environment.get("course_org")
        credentials_id = build.environment.get("credentials_id")
        lab_name = build.environment.get("lab_name")
      }
	    '''.stripIndent())
      sandbox()
    }
  }
}
