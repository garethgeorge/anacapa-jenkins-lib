folder("AnacapaGrader/${git_provider_domain}")
folder("AnacapaGrader/${git_provider_domain}/${course_org}")
job("AnacapaGrader/${git_provider_domain}/${course_org}/setupAssignment") {
  parameters {
    stringParam('lab_name', '', '''
      The name of the lab to create (corresponding to an existing git repository
      with name `assignment-${lab_name}`)
    '''.stripIndent())
  }
  scm {
    github('project-anacapa/anacapa-jenkins-lib')
  }
  environmentVariables {
    envs(
      git_provider_domain: "${git_provider_domain}",
      course_org: "${course_org}",
      credentials_id: "${credentials_id}"
    )
  }
  steps {
    dsl(['jobs/assignment.groovy', 'jobs/grader.groovy'], 'IGNORE')
  }
}
