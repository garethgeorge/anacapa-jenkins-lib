folder("Anacapa Grader/${course_org_url}")
job("Anacapa Grader/${course_org_url}/setupAssignment") {
  parameters {
    stringParam('lab_name', '', '''
      The name of the lab to create (corresponding to an existing git repository
      with name `assignment-${lab_name}`)
    '''.stripIndent)
  }
  scm {
    github('project-anacapa/anacapa-jenkins-lib')
  }
  environmentVariables {
    envs(
      course_org_url: "${course_org_url}",
      credentials_id: "${credentials_id}"
    )
  }
  steps {
    dsl(['jobs/assignment.groovy', 'jobs/grader.groovy'], 'DELETE')
  }
}
