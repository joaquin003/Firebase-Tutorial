package com.example.firebasetutorial

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_home.*

enum class ProviderType{
    BASIC,
    GOOGLE,
    FACEBOOK
}

class HomeActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //setup
        val bundle = intent.extras
        val email = bundle?.getString("email")
        val provider = bundle?.getString("provider")
        setup(email?:"",provider?:"")

        //guardado de datos
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email",email)
        prefs.putString("provider",provider)
        prefs.apply()
    }
    private fun setup(email:String,provider:String){
        title = "Inicio"
        emailTextView.text = email
        providerTextView.text = provider

        logOutButton.setOnClickListener{
            //borrar datos
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }

        saveButton.setOnClickListener{
            //creamos la coleccion
            //al poner la variable email, el mismo correo puede ver sus datos en diferentes dispositivos
            db.collection("users").document(email).set(
                hashMapOf("provider" to provider,
                "address" to addressTextView.text.toString(),
                "phone" to phoneTextView.text.toString())
            )
        }

        getButton.setOnClickListener{
            //si encontramos al usuario en nuestra db
            db.collection("users").document(email).get().addOnSuccessListener{
                addressTextView.setText(it.get("address") as String?)
                phoneTextView.setText(it.get("phone") as String?)
            }
        }

        deleteButton.setOnClickListener{
            db.collection("users").document(email).delete()
        }
    }
}