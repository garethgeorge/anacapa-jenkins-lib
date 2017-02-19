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
      import static edu.ucsb.cs.anacapa.pipeline.Lib.*

      node {
        def evars = get_envvars this
        runAssignment {
          course_org = "${evars['course_org']}"
          credentials_id = "${evars['credentials_id']}"
          lab_name = "${evars['lab_name']}"
        }
      }
	    '''.stripIndent())
      sandbox()
    }
  }
}
