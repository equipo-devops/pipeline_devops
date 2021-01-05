import pipeline.*

class PasosGradle {
    static def nombres() {      
      return ['build & test','sonar','run','rest','nexus']
    }
}

def llamar_pasos(){
    def pasos = new PasosGradle()
    def nombres = pasos.nombres()
    return nombres
}

def call(stgs) {
  
    script {
        // nombres
        def pasos = new PasosGradle()
        def nombres = pasos.nombres()

        if(stgs.indexOf(nombres[0]) != -1 ){
            stage(nombres[0]) {
                env.STG_NAME = nombres[0]
                sh "gradle clean build"
            }
        }

        if(stgs.indexOf(nombres[1]) != -1 ){
            stage(nombres[1]) {
                env.STG_NAME = nombres[1]
                def scannerHome = tool 'sonar'; // scanner
                withSonarQubeEnv('sonar') { // server
                    sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build " 
                }
            }
        }

        if(stgs.indexOf(nombres[2]) != -1 ){
            stage(nombres[2]) {
                env.STG_NAME = nombres[2]
                sh 'nohup bash gradle bootRun &'
                
            }
        }

        if(stgs.indexOf(nombres[3]) != -1 ){
            stage(nombres[3]) {
                env.STG_NAME = nombres[3]
                sleep(time: 10, unit: 'SECONDS')
                sh 'curl -X GET "http://localhost:8081/rest/mscovid/test?msg=testing"'
                
            }
        }

        if(stgs.indexOf(nombres[4]) != -1 ){
            stage(nombres[4]){
                env.STG_NAME = nombres[4]
                nexusArtifactUploader(
                nexusVersion: 'nexus3',
                protocol: 'http',
                nexusUrl: 'localhost:8081',
                groupId: 'com.devopsusach2020',
                version: '0.0.1',
                repository: 'test-nexus',
                credentialsId: 'nexus-local',
                artifacts: [
                    [artifactId: 'DevOpsUsach2020',
                    classifier: '',
                    file: 'build/libs/DevOpsUsach2020-1.0.2.jar',
                    type: 'jar']
                ]
                )
                
            }
        }
    }

}

return this;