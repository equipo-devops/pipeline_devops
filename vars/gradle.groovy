
import pipeline.*

def call(String eleccionstages ){
  

    //Escribir directamente el código del stage, sin agregarle otra clausula de Jenkins.

def stagesvalidas = ['buildtest','sonar','run','rest','nexus']
def  metodo = new test.Metodos()
def stages =  metodo.getValidaStages(eleccionstages,stagesvalidas)

stages.each{
        stage(it){
         
          try {

            "${it}"()
          }
          catch(Exception e){
               error "Stage ${it} tiene problemas : ${e}"

          }

        }


}
}



def buildtest()
{

    script { env.ETAPA = "buildtest" }
    sh  "gradle clean build"

}


def sonar(){

script { env.ETAPA = "sonar" }
                           def scannerHome = tool 'sonar-scanner'; // scanner
                        withSonarQubeEnv('Sonar') { // server
                            sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build " 
                        }

}



def run(){

script { env.ETAPA = "run" }

                         sh "nohup bash gradle bootRun &"


}


def rest(){

script { env.ETAPA = "rest" }
                        sleep(time: 10, unit: 'SECONDS')
                        sh 'curl -X GET "http://localhost:8081/rest/mscovid/test?msg=testing"'

}


def nexus(){

 script { env.ETAPA = "nexus" }
                            nexusArtifactUploader(
                        nexusVersion: 'nexus3',
                        protocol: 'http',
                        nexusUrl: 'localhost:8081',
                        groupId: 'com.devopsusach2020',
                        version: '0.0.1',
                        repository: 'test-nexus',
                        credentialsId: 'nexus',
                        artifacts: [
                            [artifactId: 'DevOpsUsach2020',
                            classifier: '',
                            file: 'build/libs/DevOpsUsach2020-0.0.1.jar',
                            type: 'jar']
                        ]
                        )

    
}

     /*
                script {
                    stage('build & test') {
                          script { env.ETAPA = "build & test" }
                          sh  "gradle clean build"
                    }
                    stage('sonar') {
                        script { env.ETAPA = "sonar" }
                           def scannerHome = tool 'sonar-scanner'; // scanner
                        withSonarQubeEnv('Sonar') { // server
                            sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build " 
                        }
                    
                    }
                    stage('run') {
                         script { env.ETAPA = "run" }

                         sh "nohup bash gradle bootRun &"
                        
                    }
                      stage('rest') {
                        script { env.ETAPA = "rest" }
                        sleep(time: 10, unit: 'SECONDS')
                        sh 'curl -X GET "http://localhost:8081/rest/mscovid/test?msg=testing"'
                    }
                    stage('nexus') {
                           script { env.ETAPA = "nexus" }
                    	    nexusArtifactUploader(
                        nexusVersion: 'nexus3',
                        protocol: 'http',
                        nexusUrl: 'localhost:8081',
                        groupId: 'com.devopsusach2020',
                        version: '0.0.1',
                        repository: 'test-nexus',
                        credentialsId: 'nexus',
                        artifacts: [
                            [artifactId: 'DevOpsUsach2020',
                            classifier: '',
                            file: 'build/libs/DevOpsUsach2020-0.0.1.jar',
                            type: 'jar']
                        ]
                        )
                        
                    }
                }
            
  */



return this;
