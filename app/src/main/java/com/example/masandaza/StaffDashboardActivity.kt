package com.example.masandaza

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StaffDashboardActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var scheduleContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_dashboard)

        val tvName = findViewById<TextView>(R.id.tvStaffNameHeader)
        val tvIdBadge = findViewById<TextView>(R.id.tvStaffIdBadge)
        val tvToday = findViewById<TextView>(R.id.tvCountToday)
        val tvPending = findViewById<TextView>(R.id.tvCountPending)
        val tvCompleted = findViewById<TextView>(R.id.tvCountCompleted)

        scheduleContainer = findViewById(R.id.containerStaffSchedule)
        val btnAllJobs = findViewById<Button>(R.id.btnViewAllJobsNav)
        val btnProfile = findViewById<Button>(R.id.btnStaffProfileNav)

        val uid = auth.currentUser?.uid

        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val name = doc.getString("fullName") ?: "Staff Member"
                        tvName.text = name
                        tvIdBadge.text = "ID: STF-" + uid.take(5).uppercase()
                    }
                }

            // Real-time listener for all bookings in Firestore
            db.collection("bookings")
                .addSnapshotListener { query, _ ->
                    if (query != null) {
                        scheduleContainer.removeAllViews()

                        var total = query.size()
                        var pending = 0
                        var completed = 0

                        for (doc in query.documents) {
                            val status = doc.getString("status") ?: "Booked"
                            if (status == "Completed") {
                                completed++
                            } else {
                                pending++
                            }
                        }

                        tvToday.text = total.toString()
                        tvPending.text = pending.toString()
                        tvCompleted.text = completed.toString()

                        if (query.isEmpty) {
                            val emptyTv = TextView(this).apply {
                                text = "No active jobs assigned yet."
                                textSize = 13f
                                setTextColor(Color.GRAY)
                                setPadding(0, 20, 0, 20)
                            }
                            scheduleContainer.addView(emptyTv)
                        } else {
                            for (doc in query.documents) {
                                val bookingId = doc.id
                                val service = doc.getString("serviceName") ?: "Cleaning"
                                val status = doc.getString("status") ?: "Booked"
                                val date = doc.getString("preferredDate") ?: "Today"
                                val time = doc.getString("preferredTime") ?: "09:00 AM"
                                val address = doc.getString("serviceAddress") ?: "Address on record"

                                val card = LayoutInflater.from(this).inflate(R.layout.item_staff_job_card, scheduleContainer, false)
                                val tvServ = card.findViewById<TextView>(R.id.itemTvService)
                                val tvStat = card.findViewById<TextView>(R.id.itemTvStatus)
                                val tvCust = card.findViewById<TextView>(R.id.itemTvCustomerDate)
                                val btnDetails = card.findViewById<Button>(R.id.itemBtnDetails)
                                val btnUpdate = card.findViewById<Button>(R.id.itemBtnUpdate)

                                tvServ.text = service
                                tvStat.text = status
                                tvCust.text = "$date • $time\n$address"

                                btnDetails.setOnClickListener {
                                    val intent = Intent(this, StaffJobDetailsActivity::class.java).apply {
                                        putExtra("bookingId", bookingId)
                                    }
                                    startActivity(intent)
                                }

                                btnUpdate.setOnClickListener {
                                    val intent = Intent(this, UpdateJobStatusActivity::class.java).apply {
                                        putExtra("bookingId", bookingId)
                                    }
                                    startActivity(intent)
                                }

                                scheduleContainer.addView(card)
                            }
                        }
                    }
                }
        }

        btnAllJobs.setOnClickListener { startActivity(Intent(this, StaffJobDetailsActivity::class.java)) }
        btnProfile.setOnClickListener { startActivity(Intent(this, StaffProfileActivity::class.java)) }
    }
}

