package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.masandaza.models.Payment
import com.example.masandaza.utils.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

//removed import android.widget.EditText because we are no longer collecting card details manually in the app.

import com.google.firebase.firestore.FirebaseFirestore

class PaymentActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val firebaseRepo = FirebaseRepository()

    // added PayFast Sandbox credentials because these are required to build the URL that opens the PayFast payment page.
    private val merchantId = "10056173"
    private val merchantKey = "r2p8we7llawsa"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        var bookingId = intent.getStringExtra("bookingId") ?: ""
        var totalAmount = intent.getDoubleExtra("totalAmount", 0.0)

        val tvRef = findViewById<TextView>(R.id.tvPaymentBookingRef)
        val tvAmount = findViewById<TextView>(R.id.tvPaymentAmount)
        val btnPay = findViewById<Button>(R.id.btnSubmitPayment)

        val btnNavHome = findViewById<Button>(R.id.navBtnHome)
        val btnNavBook = findViewById<Button>(R.id.navBtnBook)
        val btnNavTrack = findViewById<Button>(R.id.navBtnTrack)
        val btnNavProfile = findViewById<Button>(R.id.navBtnProfile)

        // Finding the WebView so we can load PayFast into it
        val webView = findViewById<WebView>(R.id.payfastWebView)

        fun updateUI() {
            tvRef.text = if (bookingId.isNotBlank()) "#$bookingId" else "#BK-PENDING"
            val amountString = String.format(Locale.US, "%.2f", if (totalAmount > 0) totalAmount else 450.0)
            tvAmount.text = "R $amountString"
            btnPay.text = "Pay Now — R $amountString"
        }

        updateUI()

        // If accessed directly from navigation with empty bookingId, load user's active pending booking
        if (bookingId.isBlank() && auth.currentUser != null) {
            db.collection("bookings")
                .whereEqualTo("customerId", auth.currentUser!!.uid)
                .get()
                .addOnSuccessListener { query ->
                    if (!query.isEmpty) {
                        val pendingDoc = query.documents.lastOrNull { doc ->
                            val s = doc.getString("status") ?: ""
                            s.equals("Pending", true) || s.equals("Booked", true)
                        } ?: query.documents.last()

                        bookingId = pendingDoc.getString("bookingId") ?: pendingDoc.id
                        totalAmount = pendingDoc.getDouble("totalAmount") ?: 450.0
                        updateUI()
                    }
                }
        }

        btnPay.setOnClickListener {
            val finalAmount = if (totalAmount > 0) totalAmount else 450.0
            val amountString = String.format(Locale.US, "%.2f", finalAmount)

            // Hide the button and show the WebView when they click Pay
            btnPay.visibility = View.GONE
            webView.visibility = View.VISIBLE

            // Build the PayFast URL with the booking details
            val paymentUrl = "https://sandbox.payfast.co.za/eng/process?" +
                    "merchant_id=$merchantId" +
                    "&merchant_key=$merchantKey" +
                    "&amount=$amountString" +
                    "&item_name=Cleaning+Service+$bookingId"

            // Setup WebView settings
            webView.settings.javaScriptEnabled = true // PayFast page needs JavaScript to run

            //WebViewClient to monitor when the user finishes paying
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    // We check if the current URL is the PayFast success page.
                    if (url != null && url.contains("sandbox.payfast.co.za") && url.contains("successful")) {
                        onPaymentSuccessful(bookingId, finalAmount)
                    }
                }
            }

            // Actually load the PayFast page
            webView.loadUrl(paymentUrl)
        }
        btnNavHome?.setOnClickListener { startActivity(Intent(this, DasboardActivity::class.java)) }
        btnNavBook?.setOnClickListener { startActivity(Intent(this, ServicesActivity::class.java)) }
        btnNavTrack?.setOnClickListener { startActivity(Intent(this, TrackBookingActivity::class.java)) }
        btnNavProfile?.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
    }

    // This function is separated from the button click to keep the code clean.
    // It only runs when PayFast confirms the payment was successful.
    private fun onPaymentSuccessful(bookingId: String, totalAmount: Double) {
        val userId = auth.currentUser?.uid ?: ""
        val receiptRef = "RCP-2025-" + (1000..9999).random()

        val newPayment = Payment(
            paymentId = receiptRef,
            bookingId = bookingId,
            customerId = userId,
            amount = totalAmount,
            paymentMethod = "PayFast Sandbox", // Changed from "Card Payment" to reflect the API used
            status = "Paid"
        )

        // The old "val newPayment = ..." block from inside the button click.
        // Reason: We moved it here so it only saves AFTER PayFast says the payment is successful.

        firebaseRepo.recordPayment(newPayment,
            onSuccess = {
                Toast.makeText(this@PaymentActivity, "Payment Successful!", Toast.LENGTH_SHORT).show()

                val successIntent = Intent(this@PaymentActivity, PaymentSuccessActivity::class.java)
                successIntent.putExtra("receiptRef", receiptRef)
                successIntent.putExtra("bookingId", bookingId)
                successIntent.putExtra("amount", totalAmount)

                startActivity(successIntent)
                finish()
            },
            onFailure = { e ->
                Toast.makeText(this@PaymentActivity, "Error saving payment: ${e?.message}", Toast.LENGTH_LONG).show()

                // If Firebase fails, we reset the UI so the user can try again
                findViewById<Button>(R.id.btnSubmitPayment).visibility = View.VISIBLE
                findViewById<WebView>(R.id.payfastWebView).visibility = View.GONE
            }
        )
    }
}