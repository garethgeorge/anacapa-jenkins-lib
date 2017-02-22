folder('Anacapa Grader')
job('Anacapa Grader/setupCourse') {
  parameters {
    stringParam('course_org_url', '', '''
      The URL of your course's github organization, github enterprise organization,
      or gitlab group. (the HTTPS url, without the `https://`)
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
