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
  sh './mvnw clean compile -e'
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


      nexusPublisher nexusInstanceId: 'nexus', nexusRepositoryId: 'test-nexus',
                         packages: [[$class: 'MavenPackage', mavenAssetList: [[classifier: '',
                          extension: 'jar', filePath: 'build/DevOpsUsach2020-' + env.VERSION_PACKAGE_CI + '.jar']], mavenCoordinate: [artifactId: 'DevOpsUsach2020',
                           groupId: 'com.devopsusach2020', packaging: 'jar', version: env.VERSION_PACKAGE_CI]]]

  
}


def nexusCDUpload(){
  

nexusPublisher nexusInstanceId: 'nexus', nexusRepositoryId: 'test-nexus',
                         packages: [[$class: 'MavenPackage', mavenAssetList: [[classifier: '',
                          extension: 'jar', filePath: 'DevOpsUsach2020-' + env.VERSION_PACKAGE_CI + '.jar']], mavenCoordinate: [artifactId: 'DevOpsUsach2020',
                           groupId: 'com.devopsusach2020', packaging: 'jar', version: env.VERSION_PACKAGE_CD]]]

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