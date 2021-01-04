def call(){
  

    //Escribir directamente el código del stage, sin agregarle otra clausula de Jenkins.
     pipeline {
    agent any

    parameters {
      choice(name: 'Eleccion', choices:['gradle','maven'],description: 'Elección de herramienta de construcción')
      string(name: 'stages', defaultValue:'',description: 'Elección de Stages')}

    options {
      timeout(time: 120, unit: 'SECONDS') 
    }
    stages {



        stage('Pipeline') {
            steps {

                   wrap([$class: 'BuildUser']) { script { env.USER_ID = "${BUILD_USER_ID}" } }  

                script{
                    if (params.Eleccion == 'maven') {
                        echo "ejecución maven"
                
                        //def ejecucion_maven = load 'maven.groovy'
                        //ejecucion_maven.call()
                       // maven.call()
                       maven ="${params.stages}"
                    } else {
                        echo "ejecución gradle"
                        //def etapa
                        //def ejecucion_gradle = load 'gradle.groovy'
                        //ejecucion_gradle.call()
                        //gradle.call()
                        gradle = "${params.stages}"
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

