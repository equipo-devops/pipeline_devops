def call(){
  

    //Escribir directamente el código del stage, sin agregarle otra clausula de Jenkins.
     pipeline {
    agent any

    parameters {
      choice(name: 'Eleccion', choices:['gradle','maven'],description: 'Elección de herramienta de construcción')
      string(name: 'stage', defaultValue:'',description: 'Elección de Stage')}

    options {
      timeout(time: 120, unit: 'SECONDS') 
    }
    stages {

      stage('Validaciones') {
                steps {
                    script{
                      
                        echo "validaciones antes de ejecución de pipeline"

                      if (params.Eleccion == 'gradle') {
                        // validacion de stages
                        if (params.stages.contains('build & test') || params.stages.contains('sonar') || params.stages.contains('run') || params.stages.contains('rest') || params.stages.contains('nexus')) {
                            echo "ejecutar stages válidos"
                        }
                        else if (params.stages.trim() == ""){
                            echo "ejecutar todos los stages"
                        }
                         else {
                            error("error de validacion de stage. parámetro stage ${params.stage} no es válido")
                        }

                     }
                     else{ 

                       if (params.stages.contains('Compile') || params.stages.contains('Unit') || params.stages.contains('Jar') || params.stages.contains('SonarQube') || params.stages.contains('Nexus Upload')|| params.stages.contains('Run')|| params.stages.contains('Test')|| params.stages.contains('Stop')) {
                            echo "ejecutar stages válidos"
                        }
                        else if (params.stages.trim() == ""){
                            echo "ejecutar todos los stages"
                        }
                         else {
                            error("error de validacion de stage. parámetro stage ${params.stage} no es válido")
                        }
                     


                     }
                    }
                }
            }



        stage('Pipeline') {
            steps {

                   wrap([$class: 'BuildUser']) { script { env.USER_ID = "${BUILD_USER_ID}" } }  

                script{
                    if (params.Eleccion == 'maven') {
                        echo "ejecución maven"
                
                        //def ejecucion_maven = load 'maven.groovy'
                        //ejecucion_maven.call()
                        maven.call()
                    } else {
                        echo "ejecución gradle"
                        //def etapa
                        //def ejecucion_gradle = load 'gradle.groovy'
                        //ejecucion_gradle.call()
                        gradle.call()
                    }
                }
             }
        }
}
post {
        success {
           // slackSend (color: '#00FF00', message: "SUCCESSFUL: Job '[${USER}] [${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
         
          slackSend (color: '#00FF00', message: "Build Success: [${env.USER_ID}] [${env.JOB_NAME}] [${params.Eleccion}] Ejecución exitosa. ")
          
              
             
        }
        failure {
          // slackSend (color: '#FF0000', message: "FAILED: Job '${USER} ${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
          slackSend (color: '#FF0000', message: "Build Failure: [${env.USER_ID}] [${env.JOB_NAME}] [${params.Eleccion}] Ejecución fallida en stage [${env.ETAPA}]. ")
        }
    }
}
          
            
  

}

return this;

