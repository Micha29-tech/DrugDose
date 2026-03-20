package it.uninsubria.drugdose.model

enum class TipoFormula {
    per_kg, per_m2, fissa, fasce
}

data class FasciaPeso(
    val pesoMin : Double,
    val pesoMax: Double,
    val doseMg : Double
)

data class Farmaco(
    val nomeFarmaco : String,
    val principioAttivo: String,
    val indicazioneClinica: String,
    val tipoFormula : TipoFormula,
    val doseUnitaria: Double?,
    val unitaMisura : String,
    val doseMax : Double,
    val etaMin : Int?,
    val pesoMinimo: Double?,
    val numeroSomministrazioni : Int,
    val alert : String?,
    val fonte : String,
    val fasce : List<FasciaPeso>? = null
)