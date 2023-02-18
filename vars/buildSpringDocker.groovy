import com.mycompany.colinbut.Constants
import com.mycompany.colinbut.DockerEcr

def call(Map args) {
    node {
        stage("Checkout") {
          
            inputBranch = input([
                message: 'Input branch',
                parameters: [
                    string(name: 'Input branch')
                ]
            ])

            echo "Branch selecionada: ${inputBranch}"
            echo "Clonando repositório: https://github.com/pdrodavi/app-job-deploy-sb.git"
          
            git(branch: "${inputBranch}", Constants.JENKINS_GITHUB_CREDENTIALS_ID, url: "https://github.com/pdrodavi/app-job-deploy-sb.git")
          
        }

        stage("Compile") {
            sh "./mvnw clean compile"
        }

        stage("Unit Test") {
            sh "./mvnw test"
        }

        stage("Integration Test") {
            sh "./mvnw verify"
        }

        stage("Static Code Analysis: Sonar") {
            echo "Running static code analysis using Sonar"
            withSonarQubeEnv(credentialsId: Constants.SONARQUBE_CREDENTIALS_ID, installationName: Constants.SONARQUBE_INSTALLATION_NAME) {
                    sh './mvnw sonar:sonar'
            }
        }

        stage("Package Artifact Jar") {
            sh "./mvnw package -DskipTests=true"
        }

        def dockerEcr = new DockerEcr(this)

        stage("Build Docker Image") {
            dockerEcr.buildDockerImage("${args.microserviceName}")
        }

        stage("Publish Docker Image") {
            dockerEcr.publishDockerImageToECR("${args.microserviceName}")
        }

        stage("Deploying to Dev") {
            echo "Deploying to Dev environment"
        }
    }
}
