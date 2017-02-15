folder("Anacapa Grader/${course_org}")
job("Anacapa Grader/${course_org}/assignmentGen") {
  parameters {
    stringParam('lab_name', '', 'The name of the lab to create')
  }
  environmentVariables {
    envs(
      course_org: "${course_org}",
      credentials_id: "${credentials_id}"
    )
  }
  steps {
    shell 'echo "Hello World Master!'
    shell 'env'
  }
}
