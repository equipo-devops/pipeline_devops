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
                          extension: 'jar', filePath: 'DevOpsUsach2020-' + env.VERSION_PACKAGE_CI + '.jar']], mavenCoordinate: [artifactId: 'DevOpsUsach2020',
                           groupId: 'com.devopsusach2020', packaging: 'jar', version: env.VERSION_PACKAGE_CD]]]


}






return this;