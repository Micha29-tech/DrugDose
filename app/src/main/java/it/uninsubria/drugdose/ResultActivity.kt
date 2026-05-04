package it.uninsubria.drugdose

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // Riceve i dati dal MainActivity
        val nomeFarmaco     = intent.getStringExtra("nomeFarmaco") ?: ""
        val principioAttivo = intent.getStringExtra("principioAttivo") ?: ""
        val indicazione     = intent.getStringExtra("indicazione") ?: ""
        val doseTotale      = intent.getDoubleExtra("doseTotale", 0.0)
        val doseSomm        = intent.getDoubleExtra("doseSomm", 0.0)
        val numSomm         = intent.getIntExtra("numSomm", 1)
        val metodo          = intent.getStringExtra("metodo") ?: ""
        val fonte           = intent.getStringExtra("fonte") ?: ""
        val alert           = intent.getStringExtra("alert") ?: ""
        val bsa             = intent.getDoubleExtra("bsa", -1.0)

        // Collega le view
        findViewById<TextView>(R.id.txtResultFarmaco).text = nomeFarmaco
        findViewById<TextView>(R.id.txtResultPrincipio).text = principioAttivo
        findViewById<TextView>(R.id.txtResultIndicazione).text = indicazione
        findViewById<TextView>(R.id.txtResultDoseTotale).text =
            "%.1f mg".format(doseTotale)
        findViewById<TextView>(R.id.txtResultDoseSomm).text =
            "%.1f mg × $numSomm somm./die".format(doseSomm)
        findViewById<TextView>(R.id.txtResultMetodo).text = metodo

        // BSA se presente
        val tvBsa = findViewById<TextView>(R.id.txtResultBsa)
        if (bsa > 0) {
            tvBsa.text = "BSA: ${"%.2f".format(bsa)} m²"
            tvBsa.visibility = android.view.View.VISIBLE
        }

        findViewById<TextView>(R.id.txtResultFonte).text = "📚 $fonte"

        // Alert
        val cardAlert = findViewById<androidx.cardview.widget.CardView>(
            R.id.cardResultAlert
        )
        if (alert.isNotEmpty()) {
            findViewById<TextView>(R.id.txtResultAlert).text = alert
            cardAlert.visibility = android.view.View.VISIBLE
        }

        // Bottone torna indietro
        findViewById<MaterialButton>(R.id.btnTornaIndietro).setOnClickListener {
            finish()
        }
    }
}