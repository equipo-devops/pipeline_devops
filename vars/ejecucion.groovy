def call(){
    def pasos_maven = []
    def pasos_gradle = []
   
    pipeline {
        agent any
        options {
        timeout(time: 120, unit: 'SECONDS') 
        }
        parameters {
        string defaultValue: '', description: '', name: 'stage', trim: false
        string defaultValue: '', description: '', name: 'tipo', trim: false
        }
        stages {
            stage('Validaciones') {
                steps {
                    script{
                        sh 'env'
                        figlet 'bienvenidos'

           
                        // validar tipo
                        if (params.tipo == "maven"){
                            figlet "ejecución maven"
                        }
                        else if (params.tipo == "gradle"){
                            figlet "ejecución gradle"
                        }
                        else {
                            error("error de validacion de tipo. parámetro tipo ${params.tipo} no es válido")
                        }

                      
                        // validar stage
                        if (params.stage == "") {
                            echo "ejecución de todos los stages"
                            if (params.tipo == "maven" ){
                                pasos_maven = maven.llamar_pasos()
                            }  
                            else{
                                pasos_gradle = gradle.llamar_pasos()
                            }
                        }
                        else if (params.stage.split(';').length > 0 ){
                            echo "ejecutar los siguientes stages"
                            def pasos_a_ejecutar = params.stage.split(';')
                            if (params.tipo == "maven"){
                                try {
                                    mvn_stgs = maven.llamar_pasos()
                                    pasos_a_ejecutar.each { 
                                        echo "${it}"
                                        if(mvn_stgs.indexOf(it) != -1){
                                            pasos_maven.add(it)
                                        }
                                    }
                                    if(pasos_maven.size() == 0){
                                        error("sin pasos detectados")
                                    }
                                } catch (Exception e) {
                                    echo 'Exception occurred: ' + e.toString()
                                    sh 'Handle the exception!'
                                }
                            }
                            else {
                                try{
                                    gradle_stgs = gradle.llamar_pasos()
                                    pasos_a_ejecutar.each { 
                                        echo "${it}"
                                        if(gradle_stgs.indexOf(it) != -1){
                                            try {
                                                pasos_gradle.add(it)
                                            } catch (Exception e) {
                                                echo 'Exception occurred: ' + e.toString()
                                                sh 'Handle the exception!'
                                            }
                                        }
                                    }
                                    if(pasos_gradle.size() == 0){
                                        error("sin pasos detectados")
                                    }
                                } catch (Exception e) {
                                    echo 'Exception occurred: ' + e.toString()
                                    sh 'Handle the exception!'
                                }
                            }

                        }
                         else {
                            error("error de validacion de stage. parámetro stage ${params.stage} no es válido")
                        }

                    }
                }

            }
            stage('Pipeline') {
                steps {
                    script{
                        wrap([$class: 'BuildUser']) {
                            def user = env.BUILD_USER_ID
                        }

                        if (params.tipo == 'maven') {
                            maven.call(pasos_maven)
 
                        } else {
                            gradle.call(pasos_gradle)
                        }
                    }
                }
            }
        } 
        post {
            success {
                slackSend (color: '#00FF00', message: "Build Success: [${user}] ['${env.JOB_NAME} [${env.BUILD_NUMBER}]'] [${params.tipo}] Ejecución exitosa")
            }
            failure {
            slackSend (color: '#FF0000', message: "Build Failure: [${user}] ['${env.JOB_NAME} [${env.BUILD_NUMBER}]'] [${params.tipo}] Ejecución fallida en stage [${env.STG_NAME}]")
            }
        }
    }

}
return this;