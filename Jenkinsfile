pipeline {
    agent any

    options {
        skipDefaultCheckout(false)
    }

    environment {
        // DockerHub
        REGISTRY = "anmoldhillon"
        IMAGE_NAME = "hello-app"
        IMAGE_TAG = "latest"

        // SonarCloud
        SONARQUBE_ENV = "sonar-cloud"
        SONAR_PROJECT_KEY = "Anmoldeep-Singh-Dhillon_cloud-repo"
        SONAR_ORG = "anmoldeep-singh-dhillon"
    }

    stages {

        stage('Checkout') {
            steps {
                withChecks("Checkout") {
                    checkout scm
                }
            }
        }

        stage('Unit Tests') {
            steps {
                withChecks("Unit Tests") {
                    sh "./mvnw test"
                }
            }
        }

        stage('SonarCloud Analysis') {
            environment {
                SONAR_TOKEN = credentials('sonar-token')
            }
            steps {
                withChecks("SonarCloud Analysis") {
                    withSonarQubeEnv("${SONARQUBE_ENV}") {
                        sh """
                            ./mvnw sonar:sonar \
                              -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                              -Dsonar.organization=${SONAR_ORG} \
                              -Dsonar.host.url=\$SONAR_HOST_URL \
                              -Dsonar.login=\$SONAR_TOKEN
                        """
                    }
                }
            }
        }

        stage("Quality Gate") {
            steps {
                withChecks("Quality Gate") {
                    timeout(time: 2, unit: 'MINUTES') {
                        waitForQualityGate abortPipeline: true
                    }
                }
            }
        }

        stage('Build JAR') {
            when { branch 'main' }
            steps {
                withChecks("Build JAR") {
                    sh "./mvnw clean package -DskipTests"
                }
            }
        }

        stage('Build Docker Image') {
            when { branch 'main' }
            steps {
                withChecks("Build Docker Image") {
                    sh """
                        docker build -t ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} .
                    """
                }
            }
        }

        stage('Push Docker Image') {
            when { branch 'main' }
            steps {
                withChecks("Push Docker Image") {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                        usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                        sh """
                            echo \$PASS | docker login -u \$USER --password-stdin
                            docker push ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                        """
                    }
                }
            }
        }

        stage('Deploy to Test Server') {
            when { branch 'main' }
            steps {
                withChecks("Deploy to Test Server") {
                    sshagent (['test-server-key']) {
                        sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@129.154.226.10 \
                        "docker pull ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} &&
                         docker stop app || true &&
                         docker rm app || true &&
                         docker run -d -p 8080:8080 --name app ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
                        """
                    }
                }
            }
        }
    }
}
