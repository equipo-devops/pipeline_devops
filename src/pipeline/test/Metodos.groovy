package pipeline.test


def getValidaStages(String stageseleccionados,Arraylist  stagevalidos){

figlet 'Metodos'
//figlet stageseleccionados
//figlet stagevalidos

def stages = []

if (stageseleccionados?.trim()){
	stageseleccionados.split(';').each {

		if (it in stagevalidos){

			stages.add(it)

			}else{

				error "${it} no existe como Stage. Stage validos son : ${stagevalidos}"

			}
		}

			println "parametro stage vacio . Se ejecutaran todos los  stages: ${stages}"
	}
	else{

			stages= stagevalidos
			println "validación correcta . Se ejecutaran los siguientes stages: ${stages}"


	}

	return stages


}

return this;

