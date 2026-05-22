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
            // Record JUnit / checkStyle & PMD  results (all branches - useful for PR feedback)
            junit '**/build/test-results/test/*.xml'
            recordIssues(
                enabledForFailure: true,
                tool: pmdParser(pattern: '**/build/reports/pmd/*.xml'),
                qualityGates: [[threshold: 1, type: 'TOTAL', unstable: false]]
            )
            recordIssues(
                enabledForFailure: true,
                tool: checkStyle(pattern: '**/build/reports/checkstyle/*.xml'),
                qualityGates: [[threshold: 1, type: 'TOTAL_ERROR', unstable: false]]
            )
        }
        failure {
            script {
                def reasons = []
                def csCount = 0
                findFiles(glob: '**/build/reports/checkstyle/*.xml').each { f ->
                    csCount += (readFile(f.path) =~ /severity="error"/).size()
                }
                if (csCount > 0) reasons << "Checkstyle: ${csCount} error(s)"

                def pmdCount = 0
                findFiles(glob: '**/build/reports/pmd/*.xml').each { f ->
                    pmdCount += (readFile(f.path) =~ /<violation /).size()
                }
                if (pmdCount > 0) reasons << "PMD: ${pmdCount} violation(s)"

                if (reasons) {
                    currentBuild.description = reasons.join(' · ') + ' — see Warnings'
                }
            }
        }
    }
}
