package it.uninsubria.drugdose

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etPeso = findViewById<EditText>(R.id.etPeso)
        val etAltezza = findViewById<EditText>(R.id.etAltezza)
        val etEta = findViewById<EditText>(R.id.etEta)
        val btnCalcola = findViewById<Button>(R.id.btnCalcola)
        val txtRisultato = findViewById<TextView>(R.id.txtDoseTotale)

        btnCalcola.setOnClickListener {
            val pesoText = etPeso.text.toString().trim()
            val altezzaText = etAltezza.text.toString().trim()
            val etaText = etEta.text.toString().trim()

            if(pesoText.isEmpty()){
                etPeso.error="Inserire il peso"
                return@setOnClickListener
            }
            if(altezzaText.isEmpty()){
                etAltezza.error="Inserire l'altezza"
                return@setOnClickListener
            }

            if(etaText.isEmpty()){
                etEta.error="Inserire l'età"
                return@setOnClickListener
            }

            val peso=pesoText.toDoubleOrNull()
            val altezza= altezzaText.toDoubleOrNull()
            val eta= etaText.toIntOrNull()

            if(peso == null || peso <= 0 ){
                etPeso.error = "Peso non valido"
                return@setOnClickListener

            }
            if(altezza == null || altezza <= 0 ){
                etAltezza.error="Altezza non valida"
                return@setOnClickListener
            }
            if(eta == null || eta < 0){
                etEta.error = "Età non valida"
                return@setOnClickListener
            }
            txtRisultato.text = "Peso: $peso kg, Altezza: $altezza cm, Età: $eta"
        }

    }
}