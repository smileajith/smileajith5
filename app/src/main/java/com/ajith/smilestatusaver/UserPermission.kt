package com.ajith.smilestatusaver

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class UserPermission : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.userpermission)

        sharedPref = getSharedPreferences("myPrefs", MODE_PRIVATE)
        val buttonShown = sharedPref.getBoolean("buttonShown", false)

            if (!buttonShown) {

                Log.d("buttonshown", buttonShown.toString())

                val grantAccessButton = findViewById<FloatingActionButton>(R.id.grant_access_button)

                grantAccessButton.setOnClickListener {
                    with(sharedPref.edit()) {
                        putBoolean("buttonShown", true)
                        apply()
                    }
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

    override fun onBackPressed() {
        // call the super implementation to handle the default behavior of the back button
        super.onBackPressed()
        // finish or kill the activity
        finish()
    }
}


