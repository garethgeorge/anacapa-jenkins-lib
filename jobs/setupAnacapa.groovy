folder('Anacapa Grader')
job('Anacapa Grader/setupAnacapa') {
  displayName("Setup Anacapa")
  scm {
    github('project-anacapa/anacapa-jenkins-lib')
  }
  steps {
    dsl('jobs/setupCourse.groovy', 'DELETE')
  }
}
