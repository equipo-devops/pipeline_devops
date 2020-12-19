/*

	forma de invocación de método call:

	def ejecucion = load 'script.groovy'
	ejecucion.call()

*/

def call(){
  
  script{
    //Escribir directamente el código del stage, sin agregarle otra clausula de Jenkins.
            stage('Compile') {
              script { env.ETAPA = "Compile" }
          
              sh './mvnw clean compile -e'
          
            
        }
        stage('Unit') {
            
                    script { env.ETAPA = "Unit" }
             
                    sh './mvnw clean test -e'
            
            
        }
        stage('Jar') {
          
                   script { env.ETAPA = "Jar" }
                    sh './mvnw clean package -e'
            
            
        }

          stage('SonarQube') {
             script { env.ETAPA = "SonarQube" }
          	
    			withSonarQubeEnv('Sonar') { // You can override the credential to be used
      			sh './mvnw org.sonarsource.scanner.maven:sonar-maven-plugin:3.7.0.1746:sonar'
    				}
			   
  		}

      stage('Nexus Upload'){
                        script { env.ETAPA = "Nexus Upload" }
                    
                        nexusPublisher nexusInstanceId: 'nexus', nexusRepositoryId: 'test-nexus',
                         packages: [[$class: 'MavenPackage', mavenAssetList: [[classifier: '',
                          extension: 'jar', filePath: 'build/DevOpsUsach2020-0.0.1.jar']], mavenCoordinate: [artifactId: 'DevOpsUsach2020',
                           groupId: 'com.devopsusach2020', packaging: 'jar', version: '1.0.2']]]
                        
                }


        stage('Run') {
            
                    script { env.ETAPA = "Run" }
                    sh 'nohup bash mvnw spring-boot:run &'
                    
              
             
               
            
        }
         stage('Test') {
                script { env.ETAPA = "Test" }
            
                sleep 20
                sh 'curl http://localhost:8081/rest/mscovid/test?msg=testing'
            
        } 
        
       stage('Stop') {
                    script { env.ETAPA = "Stop" }
            
                    sh 'bash mvnw spring-boot:stop &'
                    
                 
            
        }
  }

}

return this;
