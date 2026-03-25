package it.uninsubria.drugdose

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etPeso = findViewById<EditText>(R.id.etPeso)
        val etAltezza = findViewById<EditText>(R.id.etAltezza)
        val etEta = findViewById<EditText>(R.id.etEta)
        val btnCalcola = findViewById<EditText>(R.id.btnCalcola)
        val txtRisultato = findViewById<EditText>(R.id.txtRisultato)

        btnCalcola.setOnClickListener {
            val peso = etPeso.text.toString()
            val altezza = etAltezza.text.toString()
            val eta = etEta.text.toString()
            txtRisultato = "Peso: $peso kg, Altezza: $altezza cm, Età: $eta"
        }
    }
}