folder("Anacapa Grader/${course_org_url}")
pipelineJob("Anacapa Grader/${course_org_url}/assignment-${lab_name}") {
  scm {
    git {
	    remote {
        url("${course_org_url}/assignment-${lab_name}.git")
        credentials("${credentials_id}")
      }
    }
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
        runAssignment {
          course_org_url = "${evars['course_org_url']}"
          credentials_id = "${evars['credentials_id']}"
          lab_name = "${evars['lab_name']}"
        }
      }
	    '''.stripIndent())
      sandbox()
    }
  }
}
