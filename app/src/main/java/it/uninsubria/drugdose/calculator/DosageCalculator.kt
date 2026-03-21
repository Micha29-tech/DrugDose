package it.uninsubria.drugdose.calculator

import it.uninsubria.drugdose.model.Farmaco
import it.uninsubria.drugdose.model.TipoFormula

object DosageCalculator{

    sealed class Risultato{
        data class Successo(
            val doseTotaleMg: Double,
            val doseSomministrazioneMg: Double,
            val metodo : String,
            val bsa: Double? = null
        ) : Risultato()

        data class Errore(val messaggio : String) : Risultato()

    }

    fun calcola(
        farmaco: Farmaco,
        pesoKg: Double,
        altezzaCm : Double? = null,
        eta : Int? = null
    ) : Risultato{

        //Validazione età minima
        farmaco.etaMin?.let{
            if(pesoKg < it)
                return Risultato.Errore("Peso minimo richiesto : $it kg")
        }

        return when (farmaco.tipoFormula){

            TipoFormula.per_kg ->{
            val isUg = farmaco.unitaMisura == "µg/kg"
                val doseMg = if (isUg)
                    farmaco.doseUnitaria!! * pesoKg / 1000.0
                else
                    farmaco.doseUnitaria!! * pesoKg

                val capMg = if (isUg)
                    farmaco.doseMax * pesoKg / 1000.0
                else
                    farmaco.doseMax * pesoKg

                val finale = minOf(doseMg, capMg)
                Risultato.Successo(
                    doseTotaleMg = finale,
                    doseSomministrazioneMg = finale / farmaco.numeroSomministrazioni,
                    metodo = "${farmaco.doseUnitaria} ${farmaco.unitaMisura} × $pesoKg kg"
                )
            }

            TipoFormula.per_m2 -> {
                if (altezzaCm == null)
                    return Risultato.Errore("Altezza richiesta per il calcolo BSA")
                val bsa = Math.sqrt((altezzaCm * pesoKg) / 3600.0)
                val dose = minOf(farmaco.doseUnitaria!! * bsa, farmaco.doseMax)
                Risultato.Successo(
                    doseTotaleMg = dose,
                    doseSomministrazioneMg = dose / farmaco.numeroSomministrazioni,
                    metodo = "BSA = ${"%.2f".format(bsa)} m² × ${farmaco.doseUnitaria} mg/m²",
                    bsa = bsa
                )
            }

            TipoFormula.fissa -> {
                Risultato.Successo(
                    doseTotaleMg = farmaco.doseUnitaria!!,
                    doseSomministrazioneMg = farmaco.doseUnitaria / farmaco.numeroSomministrazioni,
                    metodo = "Dose fissa"
                )
            }

            TipoFormula.fasce -> {
                val fascia = farmaco.fasce?.find {
                    pesoKg >= it.pesoMin && pesoKg < it.pesoMax
                } ?: return Risultato.Errore("Peso fuori range per questo farmaco")

                Risultato.Successo(
                    doseTotaleMg = fascia.doseMg,
                    doseSomministrazioneMg = fascia.doseMg / farmaco.numeroSomministrazioni,
                    metodo = "Fascia ${fascia.pesoMin}–${fascia.pesoMax} kg"
                )
            }
        }
    }
}



