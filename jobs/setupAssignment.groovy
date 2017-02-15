folder("Anacapa Grader/${course_org}")
job("Anacapa Grader/${course_org}/setupAssignment") {
  parameters {
    stringParam('lab_name', '', 'The name of the lab to create')
  }
  scm {
    github('project-anacapa/anacapa-jenkins-lib')
  }
  environmentVariables {
    envs(
      course_org: "${course_org}",
      credentials_id: "${credentials_id}"
    )
  }
  steps {
    dsl(['jobs/assignment.groovy', 'jobs/grader.groovy'], 'DELETE')
  }
}
