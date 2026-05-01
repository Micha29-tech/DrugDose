package it.uninsubria.drugdose;

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import it.uninsubria.drugdose.DrugDoseViewModel
import it.uninsubria.drugdose.calculator.DosageCalculator
import it.uninsubria.drugdose.model.Farmaco
import it.uninsubria.drugdose.repository.FarmaciRepository

class MainActivity : AppCompatActivity() {

    // ── VIEW ──
    private lateinit var tilPeso: TextInputLayout
    private lateinit var tilAltezza: TextInputLayout
    private lateinit var tilEta: TextInputLayout
    private lateinit var etPeso: TextInputEditText
    private lateinit var etAltezza: TextInputEditText
    private lateinit var etEta: TextInputEditText
    private lateinit var spinnerFarmaco: Spinner
    private lateinit var txtFarmacoInfo: TextView
    private lateinit var btnCalcola: MaterialButton
    private lateinit var cardRisultato: View
    private lateinit var txtDoseTotale: TextView
    private lateinit var txtDoseSomm: TextView
    private lateinit var txtMetodo: TextView
    private lateinit var txtFonte: TextView
    private lateinit var cardAlert: View
    private lateinit var txtAlert: TextView
    private lateinit var txtErrore: TextView

    // ── DATI ──
    private lateinit var listaFarmaci: List<Farmaco>
    private var farmacoSelezionato: Farmaco? = null

    // ── VIEWMODEL ──
    private lateinit var viewModel: DrugDoseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[DrugDoseViewModel::class.java]

        inizializzaView()
        caricaFarmaci()
        ripristinaDatiRotazione()

        btnCalcola.setOnClickListener {
            calcolaDosaggio()
        }
    }

    private fun inizializzaView() {
        tilPeso        = findViewById(R.id.tilPeso)
        tilAltezza     = findViewById(R.id.tilAltezza)
        tilEta         = findViewById(R.id.tilEta)
        etPeso         = findViewById(R.id.etPeso)
        etAltezza      = findViewById(R.id.etAltezza)
        etEta          = findViewById(R.id.etEta)
        spinnerFarmaco = findViewById(R.id.spinnerFarmaco)
        txtFarmacoInfo = findViewById(R.id.txtFarmacoInfo)
        btnCalcola     = findViewById(R.id.btnCalcola)
        cardRisultato  = findViewById(R.id.cardRisultato)
        txtDoseTotale  = findViewById(R.id.txtDoseTotale)
        txtDoseSomm    = findViewById(R.id.txtDoseSomm)
        txtMetodo      = findViewById(R.id.txtMetodo)
        txtFonte       = findViewById(R.id.txtFonte)
        cardAlert      = findViewById(R.id.cardAlert)
        txtAlert       = findViewById(R.id.txtAlert)
        txtErrore      = findViewById(R.id.txtErrore)
    }

    private fun caricaFarmaci() {
        listaFarmaci = FarmaciRepository(this).getFarmaci()

        if (listaFarmaci.isEmpty()) {
            txtErrore.text       = "Errore: nessun farmaco caricato"
            txtErrore.visibility = View.VISIBLE
            return
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listaFarmaci.map { it.nomeFarmaco }
        )
        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )
        spinnerFarmaco.adapter = adapter

        spinnerFarmaco.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    farmacoSelezionato       = listaFarmaci[position]
                    viewModel.posizioneSpinner = position

                    farmacoSelezionato?.let { f ->
                        txtFarmacoInfo.visibility = View.VISIBLE
                        txtFarmacoInfo.text = buildString {
                            append(f.principioAttivo)
                            append(" · ")
                            append(f.indicazioneClinica)
                            f.etaMin?.let { append(" · Età min: $it anni") }
                        }
                    }
                    nascondiRisultato()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun calcolaDosaggio() {

        // Reset errori
        tilPeso.error        = null
        tilAltezza.error     = null
        tilEta.error         = null
        txtErrore.visibility = View.GONE

        val pesoText    = etPeso.text.toString().trim()
        val altezzaText = etAltezza.text.toString().trim()
        val etaText     = etEta.text.toString().trim()

        //  Validazione campi vuoti
        if (pesoText.isEmpty()) {
            tilPeso.error = "Inserire il peso"
            return
        }
        if (altezzaText.isEmpty()) {
            tilAltezza.error = "Inserire l'altezza"
            return
        }
        if (etaText.isEmpty()) {
            tilEta.error = "Inserire l'età"
            return
        }

        // Conversione
        val peso    = pesoText.toDoubleOrNull()
        val altezza = altezzaText.toDoubleOrNull()
        val eta     = etaText.toIntOrNull()

        //  Validazione valori
        if (peso == null || peso <= 0 || peso > 300) {
            tilPeso.error = "Peso non valido"
            return
        }
        if (altezza == null || altezza <= 0 || altezza > 250) {
            tilAltezza.error = "Altezza non valida"
            return
        }
        if (eta == null || eta < 0 || eta > 120) {
            tilEta.error = "Età non valida"
            return
        }

        val farmaco = farmacoSelezionato
        if (farmaco == null) {
            txtErrore.text       = "Seleziona un farmaco"
            txtErrore.visibility = View.VISIBLE
            return
        }

        // Validazione età minima farmaco
        farmaco.etaMin?.let { etaMin ->
            if (eta < etaMin) {
                txtErrore.text = "⚠ ${farmaco.nomeFarmaco} " +
                        "è indicato solo per pazienti ≥ $etaMin anni"
                txtErrore.visibility = View.VISIBLE
                return
            }
        }

        // Validazione peso minimo farmaco
        farmaco.pesoMinimo?.let { pesoMin ->
            if (peso < pesoMin) {
                txtErrore.text = "⚠ ${farmaco.nomeFarmaco} " +
                        "richiede peso minimo di $pesoMin kg"
                txtErrore.visibility = View.VISIBLE
                return
            }
        }

        // Calcolo
        val risultato = DosageCalculator.calcola(
            farmaco   = farmaco,
            pesoKg    = peso,
            altezzaCm = altezza,
            eta       = eta
        )

        when (risultato) {
            is DosageCalculator.Risultato.Successo -> {
                viewModel.peso              = peso
                viewModel.altezza           = altezza
                viewModel.eta               = eta
                viewModel.risultatoVisibile = true
                mostraRisultato(farmaco, risultato, eta)
            }
            is DosageCalculator.Risultato.Errore -> {
                txtErrore.text       = risultato.messaggio
                txtErrore.visibility = View.VISIBLE
                nascondiRisultato()
            }
        }
    }

    private fun mostraRisultato(
        farmaco: Farmaco,
        successo: DosageCalculator.Risultato.Successo,
        eta: Int
    ) {
        // Dose totale
        txtDoseTotale.text = "%.1f mg".format(successo.doseTotaleMg)

        // Dose per somministrazione
        txtDoseSomm.text = buildString {
            append("%.1f mg".format(successo.doseSomministrazioneMg))
            append(" × ${farmaco.numeroSomministrazioni} somm./die")
        }

        // Metodo di calcolo
        txtMetodo.text = buildString {
            append("Metodo: ${successo.metodo}")
            successo.bsa?.let {
                append("\nBSA: ${"%.2f".format(it)} m²")
            }
        }

        // Fonte
        txtFonte.text = "Fonte: ${farmaco.fonte}"

        // Mostra card risultato
        cardRisultato.visibility = View.VISIBLE

        // Alert clinico
        val alertMsg = buildString {
            if (!farmaco.alert.isNullOrEmpty()) {
                append(farmaco.alert)
            }
            // Avviso aggiuntivo se vicino all'età minima
            farmaco.etaMin?.let { etaMin ->
                if (eta < etaMin + 3) {
                    if (isNotEmpty()) append("\n\n")
                    append("⚠ Paziente vicino al limite " +
                            "di età minima ($etaMin anni)")
                }
            }
        }

        if (alertMsg.isNotEmpty()) {
            txtAlert.text        = alertMsg
            cardAlert.visibility = View.VISIBLE
        } else {
            cardAlert.visibility = View.GONE
        }
    }

    private fun nascondiRisultato() {
        cardRisultato.visibility = View.GONE
        cardAlert.visibility     = View.GONE
        txtErrore.visibility     = View.GONE
    }

    private fun ripristinaDatiRotazione() {
        if (viewModel.peso > 0)
            etPeso.setText(viewModel.peso.toString())
        if (viewModel.altezza > 0)
            etAltezza.setText(viewModel.altezza.toString())
        if (viewModel.eta > 0)
            etEta.setText(viewModel.eta.toString())

        // Ripristina spinner
        spinnerFarmaco.setSelection(viewModel.posizioneSpinner)

        // Ricalcola se il risultato era visibile
        if (viewModel.risultatoVisibile) {
            calcolaDosaggio()
        }
    }
}