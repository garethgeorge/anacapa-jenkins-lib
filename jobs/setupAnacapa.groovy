folder('Anacapa Grader')
job('setupAnacapa') {
  displayName("Setup Anacapa")
  steps {
    dsl('setupCourse.groovy', 'DELETE')
  }
}
