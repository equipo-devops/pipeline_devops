class PasosMaven {
    static def nombres() {      
      return ['Compile','Unit','Jar','sonarQube','nexusUpload','Run','Test']
    }
}




def llamar_pasos(){
    def pasos = new PasosMaven()
    def nombres = pasos.nombres()
    return nombres
}

def call(stgs){
  
    script{
      def pasos = new PasosMaven()
      def nombres = pasos.nombres()
      def stages = []

        stgs.each{
          if(nombres.indexOf(it) != -1 ){
            stages.add(it)
          }
        }

      

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


def Compile(){
  sh 'nohup bash ./mvnw spring-boot:run &'
}


def Unit(){
  sh './mvnw clean test -e'
}


def Jar(){
  sh './mvnw clean package -e'
}


def sonarQube(){
  withSonarQubeEnv('sonar') {
      sh './mvnw org.sonarsource.scanner.maven:sonar-maven-plugin:3.7.0.1746:sonar'
  }
}


def nexusUpload(){


      nexusPublisher nexusInstanceId: 'nexus', nexusRepositoryId: 'test-nexus',
                         packages: [[$class: 'MavenPackage', mavenAssetList: [[classifier: '',
                          extension: 'jar', filePath: 'build/DevOpsUsach2020-0.0.1.jar']], mavenCoordinate: [artifactId: 'DevOpsUsach2020',
                           groupId: 'com.devopsusach2020', packaging: 'jar', version: '2.0.1']]]

  /*nexusArtifactUploader(
    nexusVersion: 'nexus3',
    protocol: 'http',
    nexusUrl: 'localhost:8081',
    groupId: 'com.devopsusach2020',
    version: env.VERSION_PACKAGE_CI,
    repository: 'test-nexus',
    credentialsId: 'credencial_nexus',
    artifacts: [
          [artifactId: 'DevOpsUsach2020',
          classifier: '',
          file: 'build/DevOpsUsach2020-' + env.VERSION_PACKAGE_CI + '.jar',
          type: 'jar']
    ]
  )*/
}





def Run(){
  sh 'nohup bash ./mvnw spring-boot:run &'
}


def Test(){
  sleep(time: 10, unit: "SECONDS")
  sh 'curl -X GET "http://localhost:8081/rest/mscovid/test?msg=testing"'
}




return this;