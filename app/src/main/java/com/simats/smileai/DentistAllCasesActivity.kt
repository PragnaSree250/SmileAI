package com.simats.smileai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.simats.smileai.network.Case
import com.simats.smileai.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DentistAllCasesActivity : ComponentActivity() {
    private lateinit var casesContainer: LinearLayout
    private lateinit var staticCasesList: LinearLayout
    private var allCases: List<Case> = emptyList()
    private var initialFilterType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_all_cases)

        casesContainer = findViewById(R.id.casesContainer)
        staticCasesList = findViewById(R.id.staticCasesList)

        val btnNewCase = findViewById<Button>(R.id.btnNewCase)
        val btnFilter = findViewById<LinearLayout>(R.id.btnFilter)
        val etSearch = findViewById<EditText>(R.id.etSearch)
        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        
        val btnMenu = findViewById<ImageView>(R.id.btnMenu)

        // Restore token
        val sharedPref = getSharedPreferences("SmileAI", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null)
        if (token != null) {
            RetrofitClient.authToken = token
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnNewCase.setOnClickListener {
            startActivity(Intent(this, DentistNewCase1Activity::class.java))
        }

        initialFilterType = intent.getStringExtra("FILTER_TYPE")

        btnFilter.setOnClickListener {
            showFilterDialog()
        }
        
        
        btnMenu?.setOnClickListener {
            startActivity(Intent(this, DentistMenuBarActivity::class.java))
        }

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterCases(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        fetchCases()
    }

    private fun fetchCases() {
        RetrofitClient.instance.getDentistCases().enqueue(object : Callback<List<Case>> {
            override fun onResponse(call: Call<List<Case>>, response: Response<List<Case>>) {
                if (response.isSuccessful) {
                    val cases = response.body() ?: emptyList()
                    // Remove duplicates by ID
                    allCases = cases.distinctBy { it.id }
                    if (allCases.isNotEmpty()) {
                        staticCasesList.visibility = android.view.View.GONE
                        if (initialFilterType != null) {
                            applyStatusFilter(initialFilterType!!)
                        } else {
                            updateCasesUi(allCases)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<Case>>, t: Throwable) {
                Toast.makeText(this@DentistAllCasesActivity, "Error fetching cases: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterCases(query: String) {
        val filtered = allCases.filter { caseItem ->
            val fullName = "${caseItem.patient_first_name} ${caseItem.patient_last_name}".lowercase()
            fullName.contains(query.lowercase()) || (caseItem.id?.toString()?.contains(query) ?: false)
        }
        updateCasesUi(filtered)
    }

    private fun updateCasesUi(cases: List<Case>) {
        casesContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        for (case in cases) {
            val itemView = inflater.inflate(R.layout.item_case_all_cases, casesContainer, false)
            
            val tvCaseId = itemView.findViewById<TextView>(R.id.tvCaseId)
            val tvFirstName = itemView.findViewById<TextView>(R.id.tvPatientFirstName)
            val tvLastName = itemView.findViewById<TextView>(R.id.tvPatientLastName)
            val tvType = itemView.findViewById<TextView>(R.id.tvRestorationType)
            val tvTooth = itemView.findViewById<TextView>(R.id.tvToothNumber)
            val tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)

            tvCaseId.text = "C-${case.id ?: "????"}"
            tvFirstName.text = case.patient_first_name
            tvLastName.text = case.patient_last_name
            tvType.text = case.restoration_type ?: "Case"
            tvTooth.text = case.tooth_numbers ?: "-"
            tvStatus.text = case.status ?: "Active"

            // Set status background
            when (case.status?.lowercase()) {
                "completed", "done" -> tvStatus.setBackgroundResource(R.drawable.bg_status_green)
                "pending" -> tvStatus.setBackgroundResource(R.drawable.bg_status_yellow)
                else -> tvStatus.setBackgroundResource(R.drawable.bg_status_blue)
            }

            itemView.setOnClickListener {
                val intent = Intent(this, DentistCasesOverallReportActivity::class.java)
                intent.putExtra("EXTRA_CASE_ID", case.id)
                startActivity(intent)
            }

            casesContainer.addView(itemView)
        }
    }

    private fun showFilterDialog() {
        val options = arrayOf("All", "Active", "Pending", "Done/Completed")
        android.app.AlertDialog.Builder(this)
            .setTitle("Filter by Status")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> updateCasesUi(allCases) // All
                    1 -> applyStatusFilter("Active")
                    2 -> applyStatusFilter("Pending")
                    3 -> applyStatusFilter("Done")
                }
            }
            .show()
    }

    private fun applyStatusFilter(status: String) {
        val filtered = allCases.filter { caseItem ->
            when (status.lowercase()) {
                "active" -> caseItem.status?.lowercase() == "active" || caseItem.status?.lowercase() == "in progress"
                "pending" -> caseItem.status?.lowercase() == "pending"
                "done" -> caseItem.status?.lowercase() == "done" || caseItem.status?.lowercase() == "completed"
                else -> true
            }
        }
        updateCasesUi(filtered)
        findViewById<EditText>(R.id.etSearch).setHint("Filtering by $status...")
    }
}
