class PasosMaven {
    static def nombres() {      
      return ['Compile','Unit','Jar','SonarQube analysis','Nexus Upload','Run','Test']
    }
}

class PasosMavenCI {
    static def nombres() {      
      return ['compile','unit','jar','sonarQube','nexusCIUpload']
    }
}

class PasosMavenCD {
    static def nombres() {      
      return ['nexusDownload','runStage','test','nexusCDUpload']
    }
}

def llamar_pasos_ci(){
    def pasos = new PasosMavenCI()
    def nombres = pasos.nombres()
    return nombres
}

def llamar_pasos_cd(){
    def pasos = new PasosMavenCD()
    def nombres = pasos.nombres()
    return nombres
}

def llamar_pasos(){
    def pasos = new PasosMaven()
    def nombres = pasos.nombres()
    return nombres
}

def call(stgs,ci_cd){
  
    script{
      def pasos = new PasosMaven()
      def nombres = pasos.nombres()
      def stages = []

      if(ci_cd == 'ci'){
        pasos = new PasosMavenCI()
        nombres = pasos.nombres()
        stgs.each{
          if(nombres.indexOf(it) != -1 ){
            stages.add(it)
          }
        }
      }
      else if(ci_cd == 'cd'){
        pasos = new PasosMavenCD()
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


def compile(){
  sh 'nohup bash ./mvnw spring-boot:run &'
}


def unit(){
  sh './mvnw clean test -e'
}


def jar(){
  sh './mvnw clean package -e'
}


def sonarQube(){
  withSonarQubeEnv('sonar') {
      sh './mvnw org.sonarsource.scanner.maven:sonar-maven-plugin:3.7.0.1746:sonar'
  }
}


def nexusCIUpload(){
  nexusArtifactUploader(
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
  )
}


def nexusCDUpload(){
  nexusArtifactUploader(
    nexusVersion: 'nexus3',
    protocol: 'http',
    nexusUrl: 'localhost:8081',
    groupId: 'com.devopsusach2020',
    version: env.VERSION_PACKAGE_CD,
    repository: 'test-nexus',
    credentialsId: 'credencial_nexus',
    artifacts: [
          [artifactId: 'DevOpsUsach2020',
          classifier: '',
          file: 'build/DevOpsUsach2020-' + env.VERSION_PACKAGE_CD + '.jar',
          type: 'jar']
    ]
  )
}


def runStage(){
  sh 'nohup bash ./mvnw spring-boot:run &'
}


def test(){
  sleep(time: 10, unit: "SECONDS")
  sh 'curl -X GET "http://localhost:8081/rest/mscovid/test?msg=testing"'
}


def nexusDownload(String vers){
  sh 'curl -X GET -u admin:admin http://localhost:8081/repository/test-nexus/com/devopsusach2020/DevOpsUsach2020/' + env.VERSION_PACKAGE_CI + '/DevOpsUsach2020-' + env.VERSION_PACKAGE_CI + '.jar -O'
}


return this;