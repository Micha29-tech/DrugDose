package it.uninsubria.drugdose

import android.content.Intent
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
import it.uninsubria.drugdose.calculator.DosageCalculator
import it.uninsubria.drugdose.model.Farmaco
import it.uninsubria.drugdose.model.TipoFormula
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
    private lateinit var btnCalcola: MaterialButton
    private lateinit var txtErrore: TextView

    // Nuovi campi info farmaco
    private lateinit var txtIndicazione: TextView
    private lateinit var layoutVincoli: View
    private lateinit var txtFormula: TextView
    private lateinit var txtEtaMin: TextView

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
        btnCalcola     = findViewById(R.id.btnCalcola)
        txtErrore      = findViewById(R.id.txtErrore)

        // Nuovi campi layout aggiornato
        txtIndicazione = findViewById(R.id.txtIndicazione)
        layoutVincoli  = findViewById(R.id.layoutVincoli)
        txtFormula     = findViewById(R.id.txtFormula)
        txtEtaMin      = findViewById(R.id.txtEtaMin)
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
            R.layout.spinner_item,//layout personalizzato
            listaFarmaci.map { it.nomeFarmaco }
        )
        adapter.setDropDownViewResource(
            R.layout.spinner_dropdown_item //dropdown personalizzzato
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
                    farmacoSelezionato         = listaFarmaci[position]
                    viewModel.posizioneSpinner = position

                    farmacoSelezionato?.let { f ->

                        // Indicazione clinica
                        txtIndicazione.visibility = View.VISIBLE
                        txtIndicazione.text = "📋 ${f.indicazioneClinica}"

                        // Formula e vincoli
                        layoutVincoli.visibility = View.VISIBLE
                        txtFormula.text = when (f.tipoFormula) {
                            TipoFormula.per_kg -> "mg/kg"
                            TipoFormula.per_m2 -> "mg/m²"
                            TipoFormula.fissa  -> "Fissa"
                            TipoFormula.fasce  -> "Fasce"
                        }
                        txtEtaMin.text = f.etaMin
                            ?.let { "$it anni" }
                            ?: "Nessun limite"
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

        // ── Validazione campi vuoti ──
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

        // ── Conversione ──
        val peso    = pesoText.toDoubleOrNull()
        val altezza = altezzaText.toDoubleOrNull()
        val eta     = etaText.toIntOrNull()

        // ── Validazione valori ──
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

        // ── Validazione età minima farmaco ──
        farmaco.etaMin?.let { etaMin ->
            if (eta < etaMin) {
                txtErrore.text =
                    "⚠ ${farmaco.nomeFarmaco} è indicato " +
                            "solo per pazienti ≥ $etaMin anni"
                txtErrore.visibility = View.VISIBLE
                return
            }
        }

        // ── Validazione peso minimo farmaco ──
        farmaco.pesoMinimo?.let { pesoMin ->
            if (peso < pesoMin) {
                txtErrore.text =
                    "⚠ ${farmaco.nomeFarmaco} richiede " +
                            "peso minimo di $pesoMin kg"
                txtErrore.visibility = View.VISIBLE
                return
            }
        }

        // ── Calcolo ──
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

    // Apre ResultActivity con i dati calcolati
    private fun mostraRisultato(
        farmaco: Farmaco,
        successo: DosageCalculator.Risultato.Successo,
        eta: Int
    ) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("nomeFarmaco",     farmaco.nomeFarmaco)
            putExtra("principioAttivo", farmaco.principioAttivo)
            putExtra("indicazione",     farmaco.indicazioneClinica)
            putExtra("doseTotale",      successo.doseTotaleMg)
            putExtra("doseSomm",        successo.doseSomministrazioneMg)
            putExtra("numSomm",         farmaco.numeroSomministrazioni)
            putExtra("metodo",          successo.metodo)
            putExtra("fonte",           farmaco.fonte)
            putExtra("alert",           farmaco.alert ?: "")
            successo.bsa?.let { putExtra("bsa", it) }
        }
        startActivity(intent)
    }

    private fun nascondiRisultato() {
        txtErrore.visibility = View.GONE
        txtIndicazione.visibility = View.GONE
        layoutVincoli.visibility  = View.GONE
    }

    private fun ripristinaDatiRotazione() {
        if (viewModel.peso > 0)
            etPeso.setText(viewModel.peso.toString())
        if (viewModel.altezza > 0)
            etAltezza.setText(viewModel.altezza.toString())
        if (viewModel.eta > 0)
            etEta.setText(viewModel.eta.toString())

        spinnerFarmaco.setSelection(viewModel.posizioneSpinner)

        if (viewModel.risultatoVisibile) {
            calcolaDosaggio()
        }
    }
}

