job("AnacapaGrader-setupAssignment") {
  parameters {
    stringParam('callback_url', '', '''
      The URL that will be notified once the job has finished and complete all
      post-build activities, such as generating artifacts.
    '''.stripIndent())
    stringParam('git_provider_domain', 'github.com', '''
      The domain of your HTTPS git provider, such as github.com or gitlab.com or
      github.ucsb.edu.
    '''.stripIndent())
    stringParam('course_org', '', '''
      The name of your course's github organization, github enterprise organization,
      or gitlab group.
    '''.stripIndent())
    credentialsParam('credentials_id') {
        required()
        description('The identifier of the credentials needed to check out the git repository (note: if using ssh url for git, the credentials will need to be Username + Private Key, otherwise only https is allowed)')
    }
    stringParam('lab_name', '', '''
      The name of the lab to create (corresponding to an existing git repository
      with name `assignment-${lab_name}`)
    '''.stripIndent())
  }
  scm {
    github('project-anacapa/anacapa-jenkins-lib')
  }
  steps {
    // trigger the standalone assignment and standalone grader creation
    dsl(['jobs/assignment.groovy', 'jobs/grader.groovy'], 'IGNORE')
  }
}
