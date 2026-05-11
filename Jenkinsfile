pipeline {
    agent any

    // Keep the last 10 builds
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    // Poll SCM hourly
    triggers {
        pollSCM('H * * * *')
    }

    stages {
        // Run Gradle build with all tasks
        stage('Build & Test') {
            steps {
                sh './gradlew test pmdMain checkstyleMain checkstyleTest'
            }
        }
    }

    post {
        always {
            // Record JUnit results (all branches - useful for PR feedback)
            junit '**/build/test-results/test/*.xml'
        }
        success {
            // Record PMD and Checkstyle results (main branch only)
            script {
                if (env.BRANCH_NAME == 'main') {
                    recordIssues enabledForFailure: true, tool: pmdParser(pattern: '**/build/reports/pmd/*.xml')
                    recordIssues enabledForFailure: true, tool: checkStyle(pattern: '**/build/reports/checkstyle/*.xml')
                }
            }
        }
    }
}
