package com.reviling.filamentandroid

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.log

class WelcomeActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private var isStarted:Int=0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val welcomeLayout = findViewById<RelativeLayout>(R.id.activity_welcome)
        val welcomeText = findViewById<TextView>(R.id.welcome_text)
        FirebaseApp.initializeApp(this)
        // Add a ValueEventListener to the reference
        val reference=database.getReference("sessionActive")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get the bendAngleData value from the snapshot
//                 value = dataSnapshot.getValue(String::class.java) as Float
                isStarted = (dataSnapshot.getValue(Int::class.java)!!)
                if(isStarted==1){
                    val intent = Intent(this@WelcomeActivity, CustomViewerActivity::class.java)
                    startActivity(intent)        }
                else{
                    println("$isStarted-=-=-=-=-=-=-=-=-")
                }

                // Update the UI with the bendAngleData value
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })
        connectFirebase()

}
//    }
private fun connectFirebase() {
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
//    val myRef: DatabaseReference =
    database.getReference("sessionActive")
//    myRef.setValue("Hello!")
    println("Connected with Firebase...")
}
//    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
////        if (glassGestureDetector.onTouchEvent(ev)) {
////            return true
////        }
//        return super.dispatchTouchEvent(ev)
//    }

}
