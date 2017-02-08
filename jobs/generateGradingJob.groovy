pipelineJob('generate-grading-job') {
  parameters {
    stringParam('github_org', '', 'The course github organization') 
    stringParam('github_user', '', 'The github username of the student')
    stringParam('lab_name', '', 'The name of the lab to grade')
    stringParam('credentials_id', 'github_ncbrown1', 'The identifier of the credentials needed to check out the git repository (note: if using ssh url for git, the credentials will need to be Username + Private Key, otherwise only https is allowed)')
    booleanParam('student_submission', true, 'Whether this submission is meant to be graded or not')
  }
  displayName('${github_org}/${lab_name}-${github_user}')
  scm {
    git {
	  remote {
        github('${github_org}/${lab_name}-${github_user}')
        credentials('${credentials_id}')
      }
    }
  }
  environmentVariables {
    envs(
      github_org: '${github_org}',
      github_user: '${github_user}',
      lab_name: '${lab_name}',
      credentials_id: '${credentials_id}',
      student_submission: '${student_submission}'
    )
  }
  definition {
    cps {
      script('''
node {
  stage('Hello World') {
    sh 'Hello World!'
    sh 'env'
  }
}
	  ''')
    }
  }
}
