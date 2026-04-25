pipeline {
    agent {
        docker {
            // markhobson/maven-chrome bundles Maven + Chrome + ChromeDriver in one image
            image 'markhobson/maven-chrome'
            args  '-u root:root -v /var/lib/jenkins/.m2:/root/.m2'
        }
    }

    stages {

        // ------------------------------------------------------------------
        stage('Clone Repository') {
            steps {
                // Replace the URL below with YOUR GitHub repository URL
                git branch: 'main', url: 'https://github.com/YOUR_USERNAME/SeleniumLoginTest.git'
            }
        }

        // ------------------------------------------------------------------
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        // ------------------------------------------------------------------
        stage('Publish Test Results') {
            steps {
                // Publish JUnit XML reports to the Jenkins build page
                junit '**/target/surefire-reports/*.xml'
            }
        }
    }

    // ----------------------------------------------------------------------
    post {
        always {
            script {
                // Mark the workspace as safe for git operations
                sh "git config --global --add safe.directory ${env.WORKSPACE}"

                // Get the email of the person who triggered this build via a commit
                def committer = sh(
                    script: "git log -1 --pretty=format:'%ae'",
                    returnStdout: true
                ).trim()

                // ---- Parse Surefire XML reports for pass/fail/skip counts ----
                def raw = sh(
                    script: "grep -h \"<testcase\" target/surefire-reports/*.xml",
                    returnStdout: true
                ).trim()

                int total   = 0
                int passed  = 0
                int failed  = 0
                int skipped = 0
                def details = ""

                raw.split('\n').each { line ->
                    if (line.trim().isEmpty()) return
                    total++

                    def nameMatcher = (line =~ /name="([^"]+)"/)
                    def name = nameMatcher ? nameMatcher[0][1] : "Unknown"

                    if (line.contains("<failure")) {
                        failed++
                        details += "  ✗ ${name} — FAILED\n"
                    } else if (line.contains("<skipped") || line.contains("</skipped>")) {
                        skipped++
                        details += "  ⊘ ${name} — SKIPPED\n"
                    } else {
                        passed++
                        details += "  ✓ ${name} — PASSED\n"
                    }
                }

                // Overall build status
                def buildStatus = currentBuild.currentResult ?: 'UNKNOWN'

                // ---- Compose the email body ----
                def emailBody = """
Jenkins Build Notification
==========================
Project  : ${env.JOB_NAME}
Build    : #${env.BUILD_NUMBER}
Status   : ${buildStatus}
URL      : ${env.BUILD_URL}

Test Summary
------------
Total Tests  : ${total}
Passed       : ${passed}
Failed       : ${failed}
Skipped      : ${skipped}

Detailed Results
----------------
${details}

--
This email was sent automatically by Jenkins.
"""

                // ---- Send email to the committer ----
                emailext(
                    to      : committer,
                    subject : "[Jenkins] Build #${env.BUILD_NUMBER} – ${env.JOB_NAME} – ${buildStatus}",
                    body    : emailBody
                )

                echo "Notification sent to: ${committer}"
            }
        }
    }
}
