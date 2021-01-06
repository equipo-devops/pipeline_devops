import pipeline.*

class PasosGradle {
    static def nombres() {      
      return ['build & test','sonar','run','rest','nexus']
    }
}


class PasosGradleCI {
    static def nombres() {      
      return ['buildAndTest','sonar','runJar','rest','nexusCI']
    }
}

class PasosGradleCD {
    static def nombres() {      
      return ['downloadNexus','runDownloadedJar','rest','nexusCD']
    }
}


def llamar_pasos_ci(){
    def pasos = new PasosGradleCI()
    def nombres = pasos.nombres()
    return nombres
}

def llamar_pasos_cd(){
    def pasos = new PasosGradleCD()
    def nombres = pasos.nombres()
    return nombres
}

def llamar_pasos(){
    def pasos = new PasosGradle()
    def nombres = pasos.nombres()
    return nombres
}

def call(stgs,ci_cd){
  
    script{
      def pasos = new PasosGradle()
      def nombres = pasos.nombres()
      def stages = []

      if(ci_cd == 'ci'){
        pasos = new PasosGradleCI()
        nombres = pasos.nombres()
        stgs.each{
          if(nombres.indexOf(it) != -1 ){
            stages.add(it)
          }
        }
      }
      else if(ci_cd == 'cd'){
        pasos = new PasosGradleCD()
        nombres = pasos.nombres()
        stgs.each{
          if(nombres.indexOf(it) != -1 ){
            stages.add(it)
          }
        }
      }
      else{}

      stages.each{
        stage(it){
          try{
            "${it}"()
          }
          catch(Exception e){
            error("Stage ${it} tiene problemas: ${e} o no existe.")
          }
        }
      }
    }
}


def buildAndTest(){

     sh "gradle clean build"
}



def sonar(){

        def scannerHome = tool 'sonar-scanner'; // scanner
                withSonarQubeEnv('sonar') { // server
                    sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build " 
                }

}


def runJar(){

     sh 'nohup bash gradle bootRun &'

}

def rest(){

     sleep(time: 10, unit: 'SECONDS')
    sh 'curl -X GET "http://localhost:8081/rest/mscovid/test?msg=testing"'

}

def nexusCI(){

     nexusPublisher nexusInstanceId: 'nexus', nexusRepositoryId: 'test-nexus',
     packages: [[$class: 'MavenPackage', mavenAssetList: [[classifier: '',
     extension: 'jar', filePath: 'build/libs/DevOpsUsach2020-' + env.VERSION_PACKAGE_CI + '.jar']], mavenCoordinate: [artifactId: 'DevOpsUsach2020',
     groupId: 'com.devopsusach2020', packaging: 'jar', version: env.VERSION_PACKAGE_CI]]]

}

def downloadNexus(){

    sh 'curl -X GET -u admin:admin http://localhost:8081/repository/test-nexus/com/devopsusach2020/DevOpsUsach2020/' + env.VERSION_PACKAGE_CI + '/DevOpsUsach2020-' + env.VERSION_PACKAGE_CI + '.jar -O'

}


def runDownloadedJar(){

    sh 'nohup bash gradle bootRun &'


}

def restCD(){

         sleep(time: 10, unit: 'SECONDS')
    sh 'curl -X GET "http://localhost:8081/rest/mscovid/test?msg=testing"'

}

def nexusCD(){

    nexusPublisher nexusInstanceId: 'nexus', nexusRepositoryId: 'test-nexus',
                         packages: [[$class: 'MavenPackage', mavenAssetList: [[classifier: '',
                          extension: 'jar', filePath: 'build/DevOpsUsach2020-' + env.VERSION_PACKAGE_CD + '.jar']], mavenCoordinate: [artifactId: 'DevOpsUsach2020',
                           groupId: 'com.devopsusach2020', packaging: 'jar', version: env.VERSION_PACKAGE_CD]]]


}






/*************************+anterior ********/
/*
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

}*/

return this;