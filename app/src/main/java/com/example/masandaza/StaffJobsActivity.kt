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

        // Bottom Navigation Redirection
        findViewById<Button>(R.id.navBtnDashboard).setOnClickListener {
            val intent = Intent(this, StaffDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        findViewById<Button>(R.id.navBtnJobs).setOnClickListener { /* Already here */ }
        findViewById<Button>(R.id.navBtnProfile).setOnClickListener {
            val intent = Intent(this, StaffDashboardActivity::class.java)
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

        // Real-Time Firestore Synchronization for Accepted Jobs Only
        db.collection("bookings").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener

            allAcceptedJobs.clear()
            if (snapshot != null && !snapshot.isEmpty) {
                for (doc in snapshot.documents) {
                    val status = doc.getString("status") ?: "Pending"

                    // ONLY pull jobs that have been accepted/assigned (exclude raw pending requests)
                    if (!status.equals("Pending", true)) {
                        val id = doc.id
                        val service = doc.getString("serviceName") ?: doc.getString("service") ?: "Cleaning Service"
                        val client = doc.getString("fullName") ?: doc.getString("customerName") ?: "Client"
                        val date = doc.getString("preferredDate") ?: doc.getString("date") ?: "Today"
                        val address = doc.getString("serviceAddress") ?: doc.getString("address") ?: "Address on record"

                        allAcceptedJobs.add(BookingModel(id, service, client, date, status, address))
                    }
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
                else -> job.status.equals(currentFilter, true)
            }
            val matchesSearch = searchQuery.isEmpty() ||
                    job.serviceName.lowercase().contains(searchQuery) ||
                    job.clientName.lowercase().contains(searchQuery)

            matchesFilter && matchesSearch
        }

        if (filteredList.isNotEmpty()) {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = StaffAcceptedJobsAdapter(filteredList) { bookingId: String ->
                val intent = Intent(this, StaffJobDetailsActivity::class.java)
                intent.putExtra("bookingId", bookingId)
                startActivity(intent)
            }
        } else {
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
    }
}

// Internal Adapter for My Jobs accepted list feed
class StaffAcceptedJobsAdapter(
    private val list: List<BookingModel>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<StaffAcceptedJobsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvService: TextView = view.findViewById(R.id.tvItemService)
        val tvClient: TextView = view.findViewById(R.id.tvItemClient)
        val tvDateTime: TextView = view.findViewById(R.id.tvItemDateTime)
        val tvStatus: TextView = view.findViewById(R.id.tvItemStatus)
        val btnUpdateStatus: Button = view.findViewById(R.id.btnItemUpdateStatus)
        val btnDetails: Button = view.findViewById(R.id.btnItemDetails)
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
            status.equals("Staff Assigned", true) -> {
                holder.tvStatus.setTextColor(0xFF2563EB.toInt())
                holder.tvStatus.setBackgroundColor(0xFFDBEAFE.toInt())
            }
            status.equals("Cleaning In Progress", true) -> {
                holder.tvStatus.setTextColor(0xFF7C3AED.toInt())
                holder.tvStatus.setBackgroundColor(0xFFF3E8FF.toInt())
            }
            status.equals("Completed", true) -> {
                holder.tvStatus.setTextColor(0xFF16A34A.toInt())
                holder.tvStatus.setBackgroundColor(0xFFDCFCE7.toInt())
            }
            else -> {
                holder.tvStatus.setTextColor(0xFF475569.toInt())
                holder.tvStatus.setBackgroundColor(0xFFF1F5F9.toInt())
            }
        }

        holder.btnDetails.setOnClickListener { onClick(item.id) }
        holder.btnUpdateStatus.setOnClickListener { onClick(item.id) }
        holder.itemView.setOnClickListener { onClick(item.id) }
    }

    override fun getItemCount() = list.size
}
