package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StaffDashboardActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_dashboard)

        val tvStaffName = findViewById<TextView>(R.id.tvStaffProfileName)
        val tvTotalJobs = findViewById<TextView>(R.id.tvTotalJobsCount)
        val tvPendingJobs = findViewById<TextView>(R.id.tvPendingJobsCount)
        val tvCompletedJobs = findViewById<TextView>(R.id.tvCompletedJobsCount)
        val recyclerView = findViewById<RecyclerView>(R.id.rvStaffJobsList)
        val tvEmpty = findViewById<TextView>(R.id.tvNoJobsEmptyState)

        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.navBtnDashboard).setOnClickListener { /* Already here */ }
        findViewById<Button>(R.id.navBtnJobs).setOnClickListener {
            val intent = Intent(this, StaffJobsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        findViewById<Button>(R.id.navBtnProfile).setOnClickListener {
            val intent = Intent(this, StaffProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    tvStaffName.text = doc.getString("fullName") ?: "System Admin"
                }
            }
        }

        db.collection("bookings").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener

            if (snapshot != null && !snapshot.isEmpty) {
                tvEmpty.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                val jobList = mutableListOf<BookingModel>()
                var pending = 0
                var completed = 0

                for (doc in snapshot.documents) {
                    val id = doc.id
                    val service = doc.getString("serviceName") ?: doc.getString("service") ?: "Cleaning Service"
                    val client = doc.getString("fullName") ?: doc.getString("customerName") ?: "Client"
                    val date = doc.getString("preferredDate") ?: doc.getString("date") ?: "Today"
                    val status = doc.getString("status") ?: "Pending"
                    val address = doc.getString("serviceAddress") ?: doc.getString("address") ?: "Address on record"

                    jobList.add(BookingModel(id, service, client, date, status, address))

                    if (status.equals("Pending", true) || status.equals("Staff Assigned", true)) {
                        pending++
                    } else if (status.equals("Completed", true)) {
                        completed++
                    }
                }

                tvTotalJobs.text = jobList.size.toString()
                tvPendingJobs.text = pending.toString()
                tvCompletedJobs.text = completed.toString()

                recyclerView.adapter = StaffJobsAdapter(jobList) { bookingId ->
                    val intent = Intent(this, StaffJobDetailsActivity::class.java)
                    intent.putExtra("bookingId", bookingId)
                    startActivity(intent)
                }
            } else {
                tvEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                tvTotalJobs.text = "0"
                tvPendingJobs.text = "0"
                tvCompletedJobs.text = "0"
            }
        }
    }
}

data class BookingModel(
    val id: String,
    val serviceName: String,
    val clientName: String,
    val date: String,
    val status: String,
    val address: String
)

class StaffJobsAdapter(
    private val list: List<BookingModel>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<StaffJobsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvService: TextView = view.findViewById(R.id.tvItemService)
        val tvClient: TextView = view.findViewById(R.id.tvItemClient)
        val tvStatus: TextView = view.findViewById(R.id.tvItemStatus)
        val btnDetails: Button = view.findViewById(R.id.btnItemDetails)
        val btnUpdate: Button = view.findViewById(R.id.btnItemUpdateStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_staff_job_figma, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvService.text = item.serviceName
        holder.tvClient.text = item.clientName
        holder.tvStatus.text = item.status

        holder.btnDetails.setOnClickListener { onClick(item.id) }
        holder.btnUpdate.setOnClickListener { onClick(item.id) }
        holder.itemView.setOnClickListener { onClick(item.id) }
    }

    override fun getItemCount() = list.size
}