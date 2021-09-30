package com.example.firebasetutorial

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_home.*
import java.sql.DriverManager.println
import java.text.DecimalFormat

enum class ProviderType{
    BASIC,
    GOOGLE,
    FACEBOOK
}

class HomeActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    private val chanelID = "ChannelID"
    private val chanelName = "chanelName"
    private val notificationId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //setup
        val bundle = intent.extras
        val email = bundle?.getString("email")
        val provider = bundle?.getString("provider")
        setup(email ?: "", provider ?: "")

        //guardado de datos
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()

        val num = 1325.220
        val formatter = DecimalFormat("#,###")
        println(formatter.format(num))

        createNotificationChannel()

        val notification = NotificationCompat.Builder(this,chanelID).also{
            it.setContentTitle("Titulo de notificacion")
            it.setContentText("Este es el contenido de la notificacion")
            it.setSmallIcon(R.drawable.ic_lineasicono)
            it.priority = NotificationCompat.PRIORITY_HIGH
        }.build()

        val notificationManager = NotificationManagerCompat.from(this)

        btn_newNotification.setOnClickListener{
            notificationManager.notify(notificationId,notification)
        }
    }

    private fun setup(email: String, provider: String){
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

        /*val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Kotlin Progress Bar")
        progressDialog.setMessage("Application is loading, please wait")
        progressDialog.show()*/
    }

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(chanelID,chanelName,importance).apply{
                //color led
                lightColor = Color.GREEN
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(500,1000)
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }
}