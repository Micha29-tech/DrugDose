package it.uninsubria.drugdose.calculator

import it.uninsubria.drugdose.model.Farmaco
import it.uninsubria.drugdose.model.TipoFormula
import it.uninsubria.drugdose.utils.BsaCalculator

object DosageCalculator {

    sealed class Risultato {
        data class Successo(
            val doseTotaleMg: Double,
            val doseSomministrazioneMg: Double,
            val metodo: String,
            val bsa: Double? = null
        ) : Risultato()

        data class Errore(val messaggio: String) : Risultato()

    }

    fun calcola(
        farmaco: Farmaco,
        pesoKg: Double,
        altezzaCm: Double? = null,
        eta: Int? = null
    ): Risultato {

        //Validazione età minima
        farmaco.etaMin?.let { etaMin ->
            if (eta == null || eta < etaMin)
                return Risultato.Errore(
                    "Età minima richiesta: $etaMin anni"
                )
        }
        //Validazione peso minimo
        farmaco.pesoMinimo?.let { pesoMin ->
            if (pesoKg < pesoMin)
                return Risultato.Errore(
                    "Peso minimo richiesto: $pesoMin kg"
                )

        }
            return when (farmaco.tipoFormula) {

                TipoFormula.per_kg -> {
                    val isUg = farmaco.unitaMisura == "µg/kg"

                    //Cacola dose in mg
                    val doseMg = if (isUg)
                        farmaco.doseUnitaria!! * pesoKg / 1000.0
                    else
                        farmaco.doseUnitaria!! * pesoKg
                    //fix calcolo doseMax
                    val capMg = if (isUg)
                        farmaco.doseMax //dose max totale in mg
                    else
                        farmaco.doseMax * pesoKg //dose max per kg

                    val finale = minOf(doseMg, capMg)
                    Risultato.Successo(
                        doseTotaleMg = finale,
                        doseSomministrazioneMg = finale / farmaco.numeroSomministrazioni,
                        metodo = "${farmaco.doseUnitaria} ${farmaco.unitaMisura} × $pesoKg kg"
                    )
                }


                TipoFormula.per_m2 -> {
                    if (altezzaCm == null)
                        return Risultato.Errore(
                            "Altezza richiesta per il calcolo BSA"
                        )

                    val bsa = BsaCalculator.calcola(altezzaCm, pesoKg)
                    val doseCalcolata = farmaco.doseUnitaria!! * bsa

                    // doseMax per per_m2 è dose TOTALE in mg
                    val finale = minOf(doseCalcolata, farmaco.doseMax)

                    val metodo = buildString {
                        append("BSA = ${"%.2f".format(bsa)} m²")
                        append(" × ${farmaco.doseUnitaria} mg/m²")
                        append(" = ${"%.1f".format(doseCalcolata)} mg")
                        if (doseCalcolata > farmaco.doseMax)
                            append(" → cappata a ${farmaco.doseMax} mg")
                    }

                    Risultato.Successo(
                        doseTotaleMg = finale,
                        doseSomministrazioneMg = finale / farmaco.numeroSomministrazioni,
                        metodo = metodo,
                        bsa = bsa
                    )
                }

                TipoFormula.fissa -> {
                    Risultato.Successo(
                        doseTotaleMg = farmaco.doseUnitaria!!,
                        doseSomministrazioneMg = farmaco.doseUnitaria /
                                farmaco.numeroSomministrazioni,
                        metodo = "Dose fissa: ${farmaco.doseUnitaria} mg"
                    )
                }

                TipoFormula.fasce -> {
                    val fascia = farmaco.fasce?.find {
                        pesoKg >= it.pesoMin && pesoKg < it.pesoMax
                    } ?: return Risultato.Errore(
                        "Peso fuori range per questo farmaco"
                    )

                    val metodo = buildString {
                        append("Fascia ${fascia.pesoMin}–${fascia.pesoMax} kg")
                        append(" → ${fascia.doseMg} mg")
                    }

                    Risultato.Successo(
                        doseTotaleMg = fascia.doseMg,
                        doseSomministrazioneMg = fascia.doseMg /
                                farmaco.numeroSomministrazioni,
                        metodo = metodo
                    )
                }
            }
        }
    }




