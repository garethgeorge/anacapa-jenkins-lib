folder('Anacapa Grader')
job('Anacapa Grader/setupCourse') {
  parameters {
    stringParam('course_org', '', 'The course github organization')
    stringParam('credentials_id', 'github_ncbrown1', 'The identifier of the credentials needed to check out the git repository (note: if using ssh url for git, the credentials will need to be Username + Private Key, otherwise only https is allowed)')
  }
  steps {
    dsl(['assignmentGen.groovy', 'graderGen.groovy'], 'DELETE')
  }
}
