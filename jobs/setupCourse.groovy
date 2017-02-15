folder('Anacapa Grader')
job('Anacapa Grader/setupCourse') {
  parameters {
    stringParam('course_org', '', 'The course github organization')
    credentialsParam('credentials_id') {
        required()
        defaultValue('github_ncbrown1')
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
