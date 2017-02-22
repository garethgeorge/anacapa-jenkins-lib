folder('AnacapaGrader')
job('AnacapaGrader/setupCourse') {
  parameters {
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
  }
  scm {
    github('project-anacapa/anacapa-jenkins-lib')
  }
  steps {
    dsl(['jobs/setupAssignment.groovy'], 'DELETE')
  }
}
