pipelineJob("AnacapaGrader ${git_provider_domain} ${course_org} assignment-${lab_name}") {
  scm {
    git {
	    remote {
        url("https://${git_provider_domain}/${course_org}/assignment-${lab_name}.git")
        credentials("${credentials_id}")
      }
    }
  }
  environmentVariables {
    envs(
      git_provider_domain: "${git_provider_domain}",
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
          git_provider_domain = "${evars['git_provider_domain']}"
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
