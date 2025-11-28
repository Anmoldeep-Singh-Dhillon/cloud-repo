pipeline {
    agent any

    environment {
        // --- DockerHub ---
        REGISTRY = "anmoldhillon"                   // Your DockerHub username
        IMAGE_NAME = "hello-app"
        IMAGE_TAG = "latest"

        // --- SonarCloud ---
        SONARQUBE_ENV = "sonar-server"              // Jenkins SonarCloud Server Name
        SONAR_PROJECT_KEY = "anmoldeep-singh-dhillon"    // Your SonarCloud project key
        SONAR_ORG = "Anmoldeep-Singh-Dhillon"            // Your org key
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Unit Tests') {
            steps {
                sh "./mvnw test"
            }
        }

        stage('SonarCloud Analysis') {
            environment {
                SONAR_TOKEN = credentials('sonar-token')
            }
            steps {
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

        stage("Quality Gate") {
            steps {
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build JAR') {
            steps {
                sh "./mvnw clean package -DskipTests"
            }
        }

        stage('Build Docker Image') {
            steps {
                sh """
                    docker build -t ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} .
                """
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                    sh """
                        echo \$PASS | docker login -u \$USER --password-stdin
                        docker push ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                    """
                }
            }
        }

        stage('Deploy to Test (T) Server') {
            steps {
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
