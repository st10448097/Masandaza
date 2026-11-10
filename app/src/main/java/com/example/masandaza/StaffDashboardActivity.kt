package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

        val btnNavDash = findViewById<Button>(R.id.navBtnDashboard)
        val btnNavJobs = findViewById<Button>(R.id.navBtnJobs)
        val btnNavProf = findViewById<Button>(R.id.navBtnProfile)

        // Highlight active Dashboard tab in light blue
        btnNavDash?.setTextColor(0xFF93C5FD.toInt())
        btnNavJobs?.setTextColor(0xFFFFFFFF.toInt())
        btnNavProf?.setTextColor(0xFFFFFFFF.toInt())

        btnNavDash?.setOnClickListener { /* Already here */ }
        btnNavJobs?.setOnClickListener {
            val intent = Intent(this, StaffJobsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        btnNavProf?.setOnClickListener {
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
                    val client = doc.getString("fullName") ?: doc.getString("customerName") ?: doc.getString("customerEmail") ?: "Client"
                    val date = doc.getString("preferredDate") ?: doc.getString("date") ?: "Today"
                    val status = doc.getString("status") ?: "Pending"
                    val address = doc.getString("serviceAddress") ?: doc.getString("address") ?: "Address on record"

                    jobList.add(BookingModel(id, service, client, date, status, address))

                    if (status.equals("Completed", true) || status.equals("Done", true)) {
                        completed++
                    } else if (!status.equals("Cancelled", true)) {
                        pending++
                    }
                }

                tvTotalJobs.text = (pending + completed).toString()
                tvPendingJobs.text = pending.toString()
                tvCompletedJobs.text = completed.toString()

                recyclerView.adapter = StaffJobsAdapter(
                    list = jobList,
                    onDetailsClick = { bookingId ->
                        val intent = Intent(this, StaffJobDetailsActivity::class.java)
                        intent.putExtra("bookingId", bookingId)
                        startActivity(intent)
                    },
                    onUpdateClick = { bookingId ->
                        val intent = Intent(this, UpdateJobStatusActivity::class.java)
                        intent.putExtra("bookingId", bookingId)
                        startActivity(intent)
                    },
                    onDeleteClick = { bookingId, serviceName ->
                        confirmAndDeleteJob(bookingId, serviceName)
                    }
                )
            } else {
                tvEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                tvTotalJobs.text = "0"
                tvPendingJobs.text = "0"
                tvCompletedJobs.text = "0"
            }
        }
    }

    private fun confirmAndDeleteJob(bookingId: String, serviceName: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Job")
            .setMessage("Are you sure you want to delete '$serviceName'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteJobFromFirestore(bookingId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteJobFromFirestore(bookingId: String) {
        db.collection("bookings").document(bookingId).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Job deleted successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Fallback check by bookingId field
                db.collection("bookings").whereEqualTo("bookingId", bookingId).get()
                    .addOnSuccessListener { query ->
                        for (doc in query.documents) {
                            doc.reference.delete()
                        }
                        Toast.makeText(this, "Job deleted successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to delete job: ${e.message}", Toast.LENGTH_SHORT).show()
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
    private val onDetailsClick: (String) -> Unit,
    private val onUpdateClick: (String) -> Unit,
    private val onDeleteClick: (String, String) -> Unit
) : RecyclerView.Adapter<StaffJobsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvService: TextView = view.findViewById(R.id.tvItemService)
        val tvClient: TextView = view.findViewById(R.id.tvItemClient)
        val tvDateTime: TextView = view.findViewById(R.id.tvItemDateTime)
        val tvStatus: TextView = view.findViewById(R.id.tvItemStatus)
        val btnDetails: Button = view.findViewById(R.id.btnItemDetails)
        val btnUpdate: Button = view.findViewById(R.id.btnItemUpdateStatus)
        val btnDelete: Button = view.findViewById(R.id.btnItemDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_staff_job_figma, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvService.text = item.serviceName
        holder.tvClient.text = "Client: ${item.clientName}"
        holder.tvDateTime.text = "📅 ${item.date}"

        val status = item.status
        holder.tvStatus.text = status

        when {
            status.equals("Pending", true) || status.equals("Booked", true) -> {
                holder.tvStatus.setTextColor(0xFFD97706.toInt())
                holder.tvStatus.setBackgroundColor(0xFFFEF3C7.toInt())
            }
            status.equals("Staff Assigned", true) -> {
                holder.tvStatus.setTextColor(0xFF2563EB.toInt())
                holder.tvStatus.setBackgroundColor(0xFFDBEAFE.toInt())
            }
            status.equals("Cleaning In Progress", true) -> {
                holder.tvStatus.setTextColor(0xFF7C3AED.toInt())
                holder.tvStatus.setBackgroundColor(0xFFF3E8FF.toInt())
            }
            status.equals("Completed", true) || status.equals("Done", true) -> {
                holder.tvStatus.setTextColor(0xFF16A34A.toInt())
                holder.tvStatus.setBackgroundColor(0xFFDCFCE7.toInt())
            }
            else -> {
                holder.tvStatus.setTextColor(0xFF475569.toInt())
                holder.tvStatus.setBackgroundColor(0xFFF1F5F9.toInt())
            }
        }

        holder.btnDetails.setOnClickListener { onDetailsClick(item.id) }
        holder.btnUpdate.setOnClickListener { onUpdateClick(item.id) }
        holder.btnDelete.setOnClickListener { onDeleteClick(item.id, item.serviceName) }
        holder.itemView.setOnClickListener { onDetailsClick(item.id) }
    }

    override fun getItemCount() = list.size
}