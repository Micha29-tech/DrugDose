package it.uninsubria.drugdose

import androidx.lifecycle.ViewModel

class DrugDoseViewModel : ViewModel() {
    var peso: Double            = 0.0
    var altezza: Double         = 0.0
    var eta: Int                = 0
    var posizioneSpinner: Int   = 0
    var risultatoVisibile: Boolean = false
}