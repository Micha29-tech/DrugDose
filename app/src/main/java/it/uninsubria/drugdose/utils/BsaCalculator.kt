package it.uninsubria.drugdose.utils

import kotlin.math.sqrt
object BsaCalculator{

    // Calcola la superficie corporea (BSA) usando la formula di Mosteller.
    // BSA (m²) = √( altezza(cm) × peso(kg) / 3600 )

    fun calcola(altezzaCm: Double,pesoKg:Double) : Double{
        require(altezzaCm > 0) { " Altezza deve essere maggiore di 0"}
        require(pesoKg > 0) {" Peso deve essere maggiore di 0"}
        return sqrt((altezzaCm * pesoKg) / 3600.0)
    }
}