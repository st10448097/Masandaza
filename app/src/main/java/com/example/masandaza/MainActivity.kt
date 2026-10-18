package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If user is already authenticated, jump straight to Dashboard
        if (auth.currentUser != null) {
            startActivity(Intent(this, DasboardActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnMainViewServices).setOnClickListener {
            startActivity(Intent(this, ServicesActivity::class.java))
        }

        findViewById<Button>(R.id.btnMainLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        findViewById<Button>(R.id.btnMainRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}