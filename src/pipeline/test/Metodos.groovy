package pipeline.test


def getValidaStages(string Stageseleccionados,arraylist  Stagevalidos){

def stages = []

if (Stageseleccionados?.trim()){
	Stageseleccionados.split(';').each {

		if (it in Stagevalidos){

			stage.add(it)

			}else{

				error "${it} no existe como Stage. Stage validos son : ${Stagevalidos}"

			}
		}

			println "parametro stage vacio . Se ejecutaran todos los  stages: ${stages}"
	}
	else{

			stages= Stagevalidos
			println "validación correcta . Se ejecutaran los siguientes stages: ${stages}"


	}

	return stages


}

return this;

