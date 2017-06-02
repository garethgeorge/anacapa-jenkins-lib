// folder("AnacapaGrader/${git_provider_domain}")
// folder("AnacapaGrader/${git_provider_domain}/${course_org}")
pipelineJob("AnacapaGrader ${git_provider_domain} ${course_org} grader-${lab_name}") {
  parameters {
    stringParam('github_user', '', 'The github username(s) of the student(s)')
    stringParam('commit', '*/master', 'The hash of the commit to build')
  }
  environmentVariables {
    envs(
      git_provider_domain: "${git_provider_domain}",
      course_org: "${course_org}",
      credentials_id: "${credentials_id}",
      lab_name: "${lab_name}",
      callback_url: "${callback_url}"
    )
  }
  notifications {
    endpoint("${callback_url}") {
      event('finalized')
    }
  }
  definition {
    cps {
      script('''
      @Library('anacapa-jenkins-lib') _
      import static edu.ucsb.cs.anacapa.pipeline.Lib.*

      node {
        def evars = get_envvars this
        runGrader {
          git_provider_domain = "${evars['git_provider_domain']}"
          course_org = "${evars['course_org']}"
          credentials_id = "${evars['credentials_id']}"
          lab_name = "${evars['lab_name']}"
          github_user = "${evars['github_user']}"
          commit = "${evars['commit']}"
        }
      }
	    '''.stripIndent())
      sandbox()
    }
  }
}
