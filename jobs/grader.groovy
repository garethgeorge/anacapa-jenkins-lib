folder("Anacapa Grader/${course_org_url}")
pipelineJob("Anacapa Grader/${course_org_url}/grader-${lab_name}") {
  parameters {
    stringParam('github_user', '', 'The github username(s) of the student(s)')
  }
  environmentVariables {
    envs(
      course_org_url: "${course_org_url}",
      credentials_id: "${credentials_id}",
      lab_name: "${lab_name}"
    )
  }
  definition {
    cps {
      script('''
      @Library('anacapa-jenkins-lib') _
      import static edu.ucsb.cs.anacapa.pipeline.Lib.*

      node {
        def evars = get_envvars this
        runGrader {
          course_org_url = "${evars['course_org_url']}"
          credentials_id = "${evars['credentials_id']}"
          lab_name = "${evars['lab_name']}"
          github_user = "${evars['github_user']}"
        }
      }
	    '''.stripIndent())
      sandbox()
    }
  }
}
