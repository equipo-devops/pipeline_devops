def call(){
    def pasos_maven = []
    def pasos_gradle = []
    def ci_cd = ''
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

                        // version a env
                        env.VERSION_PACKAGE_CI = '0.0.1'
                        env.VERSION_PACKAGE_CD = '0.0.2'
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

                        // validar rama variable entorno
                        if (env.GIT_BRANCH == 'develop' || env.GIT_BRANCH.contains('feature')){
                            ci_cd = 'ci'
                            figlet "Intregracion Continua"
                        }
                        else if (env.GIT_BRANCH.contains('release')){
                            ci_cd = 'cd'
                            figlet "Entrega Continua"
                        }
                        else{
                            error("rama a ejecutar no corresponde a ninguna conocida: develop, feature, release.")
                        }

                        // validar stage
                        if (params.stage == "") {
                            figlet "ejecución de todos los stages"
                            if (params.tipo == "maven" && ci_cd == "ci"){
                                pasos_maven = maven.llamar_pasos_ci()
                            }
                            else if (params.tipo == "maven" && ci_cd == "cd"){
                                pasos_maven = maven.llamar_pasos_cd()
                            }
                            else if (params.tipo == "gradle" && ci_cd == "ci"){
                                pasos_gradle = gradle.llamar_pasos_ci()
                            }
                            else if (params.tipo == "gradle" && ci_cd == "cd"){
                                pasos_gradle = gradle.llamar_pasos_cd()
                            }
                            /*else{
                                pasos_gradle = gradle.llamar_pasos()
                            }*/
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
                            maven.call(pasos_maven,ci_cd)
 
                        } else {
                            gradle.call(pasos_gradle,ci_cd)
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