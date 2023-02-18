import com.mycompany.colinbut.Git

def call(Map args=[:], Closure body={}) {
    node {
        stage("Checkout") {
            new Git(this).checkout("${args.repo}")
        }

        stage("Package Artifact Jar") {
            sh "mvn -Dmaven.test.skip=true -Dmaven.test.failure.ignore clean package"
        }
        
        body()
    }
}
