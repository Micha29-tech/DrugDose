package it.uninsubria.drugdose.repository

import android.content.Context
import com.google.gson.Gson
import android.util.Log
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import it.uninsubria.drugdose.model.Farmaco

class FarmaciRepository(private val context : Context) {

    companion object {
        private const val FILE_NAME = "farmaci.json"
        private const val TAG = "FarmaciRepository"
    }

    fun getFarmaci(): List<Farmaco> {
        return try {
            val json = context.assets
                .open(FILE_NAME)
                .bufferedReader()
                .use { it.readText() }

            val type = object : TypeToken<Map<String, List<Farmaco>>>() {}.type
            val map: Map<String, List<Farmaco>> = Gson().fromJson(json, type)
            return map["farmaci"] ?: emptyList<Farmaco>().also {
                Log.w(TAG, "Chiave 'farmaci' non trovata nel JSON.") //
            }
        } catch (e: JsonSyntaxException) {
            Log.e(
                TAG,
                "Errore nel caricamento di $FILE_NAME: ${e.message}"
            ) //scrivono l'errore nel Logcat di Android Studio così puoi vedere cosa è andato storto durante lo sviluppo.
            emptyList()
        }
    }
}

