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
      @Library('anacapa-jenkins-lib') import static edu.ucsb.cs.anacapa.pipeline.Lib.*

      def evars = get_envvars this
      runGrader {
        course_org = "${evars['course_org']}"
        credentials_id = "${evars['credentials_id']}"
        lab_name = "${evars['lab_name']}"
        github_user = "${evars['github_user']}"
      }
	    '''.stripIndent())
      sandbox()
    }
  }
}
