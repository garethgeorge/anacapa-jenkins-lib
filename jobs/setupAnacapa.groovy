folder('Anacapa Grader')
job('Anacapa Grader/setupAnacapa') {
  displayName("Setup Anacapa")
  steps {
    dsl('setupCourse.groovy', 'DELETE')
  }
}
