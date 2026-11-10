package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class StaffJobsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val allAcceptedJobs = mutableListOf<BookingModel>()
    private var currentFilter = "All"
    private var searchQuery = ""

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var etSearch: EditText

    private lateinit var btnAll: Button
    private lateinit var btnAssigned: Button
    private lateinit var btnInProgress: Button
    private lateinit var btnCompleted: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_jobs)

        recyclerView = findViewById(R.id.rvStaffMyJobsList)
        tvEmpty = findViewById(R.id.tvNoMyJobsEmpty)
        etSearch = findViewById(R.id.etStaffJobSearch)

        btnAll = findViewById(R.id.filterBtnAll)
        btnAssigned = findViewById(R.id.filterBtnAssigned)
        btnInProgress = findViewById(R.id.filterBtnInProgress)
        btnCompleted = findViewById(R.id.filterBtnCompleted)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val btnNavDash = findViewById<Button>(R.id.navBtnDashboard)
        val btnNavJobs = findViewById<Button>(R.id.navBtnJobs)
        val btnNavProf = findViewById<Button>(R.id.navBtnProfile)

        // Highlight active Jobs tab in light blue
        btnNavDash?.setTextColor(0xFFFFFFFF.toInt())
        btnNavJobs?.setTextColor(0xFF93C5FD.toInt())
        btnNavProf?.setTextColor(0xFFFFFFFF.toInt())

        // Bottom Navigation Redirection
        btnNavDash?.setOnClickListener {
            val intent = Intent(this, StaffDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        btnNavJobs?.setOnClickListener { /* Already here */ }
        btnNavProf?.setOnClickListener {
            val intent = Intent(this, StaffProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // Filter button click listeners
        btnAll.setOnClickListener { updateFilter("All", btnAll) }
        btnAssigned.setOnClickListener { updateFilter("Staff Assigned", btnAssigned) }
        btnInProgress.setOnClickListener { updateFilter("Cleaning In Progress", btnInProgress) }
        btnCompleted.setOnClickListener { updateFilter("Completed", btnCompleted) }

        // Live search bar watcher
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString().trim().lowercase()
                filterAndDisplayJobs()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Real-Time Firestore Synchronization for Jobs Feed
        db.collection("bookings").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener

            allAcceptedJobs.clear()
            if (snapshot != null && !snapshot.isEmpty) {
                for (doc in snapshot.documents) {
                    val id = doc.id
                    val status = doc.getString("status") ?: "Pending"
                    val service = doc.getString("serviceName") ?: doc.getString("service") ?: "Cleaning Service"
                    val client = doc.getString("fullName") ?: doc.getString("customerName") ?: doc.getString("customerEmail") ?: "Client"
                    val date = doc.getString("preferredDate") ?: doc.getString("date") ?: "Today"
                    val address = doc.getString("serviceAddress") ?: doc.getString("address") ?: "Address on record"

                    allAcceptedJobs.add(BookingModel(id, service, client, date, status, address))
                }
            }
            filterAndDisplayJobs()
        }
    }

    private fun updateFilter(filter: String, selectedButton: Button) {
        currentFilter = filter

        val buttons = listOf(btnAll, btnAssigned, btnInProgress, btnCompleted)
        for (btn in buttons) {
            btn.setBackgroundColor(0xFFE2E8F0.toInt())
            btn.setTextColor(0xFF0F172A.toInt())
        }
        selectedButton.setBackgroundColor(0xFFDC2626.toInt())
        selectedButton.setTextColor(0xFFFFFFFF.toInt())

        filterAndDisplayJobs()
    }

    private fun filterAndDisplayJobs() {
        val filteredList = allAcceptedJobs.filter { job ->
            val matchesFilter = when (currentFilter) {
                "All" -> true
                "Staff Assigned" -> job.status.equals("Staff Assigned", true) || job.status.equals("Assigned", true)
                "Cleaning In Progress" -> job.status.equals("Cleaning In Progress", true) || job.status.equals("In Progress", true) || job.status.equals("Ready for Review", true)
                "Completed" -> job.status.equals("Completed", true) || job.status.equals("Done", true)
                else -> job.status.equals(currentFilter, true)
            }
            val matchesSearch = searchQuery.isEmpty() ||
                    job.serviceName.lowercase().contains(searchQuery) ||
                    job.clientName.lowercase().contains(searchQuery) ||
                    job.status.lowercase().contains(searchQuery)

            matchesFilter && matchesSearch
        }

        if (filteredList.isNotEmpty()) {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = StaffAcceptedJobsAdapter(
                list = filteredList,
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

// Internal Adapter for My Jobs accepted list feed
class StaffAcceptedJobsAdapter(
    private val list: List<BookingModel>,
    private val onDetailsClick: (String) -> Unit,
    private val onUpdateClick: (String) -> Unit,
    private val onDeleteClick: (String, String) -> Unit
) : RecyclerView.Adapter<StaffAcceptedJobsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvService: TextView = view.findViewById(R.id.tvItemService)
        val tvClient: TextView = view.findViewById(R.id.tvItemClient)
        val tvDateTime: TextView = view.findViewById(R.id.tvItemDateTime)
        val tvStatus: TextView = view.findViewById(R.id.tvItemStatus)
        val btnDetails: Button = view.findViewById(R.id.btnItemDetails)
        val btnUpdateStatus: Button = view.findViewById(R.id.btnItemUpdateStatus)
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
        holder.btnUpdateStatus.setOnClickListener { onUpdateClick(item.id) }
        holder.btnDelete.setOnClickListener { onDeleteClick(item.id, item.serviceName) }
        holder.itemView.setOnClickListener { onDetailsClick(item.id) }
    }

    override fun getItemCount() = list.size
}