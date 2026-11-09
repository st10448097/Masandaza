package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.masandaza.models.Payment
import com.example.masandaza.utils.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class PaymentActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val firebaseRepo = FirebaseRepository()

    // Paystack Test Public Key
    // Replace with your actual key from https://dashboard.paystack.com (Settings -> API Keys & Webhooks)
    private var paystackPublicKey = "pk_test_3029eb8129aa5bc6a68e7dcb9db0d06e573747d9"

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

        val webView = findViewById<WebView>(R.id.payfastWebView)

        fun updateUI() {
            tvRef.text = if (bookingId.isNotBlank()) "#$bookingId" else "#BK-PENDING"
            val amountString = String.format(Locale.US, "%.2f", if (totalAmount > 0) totalAmount else 450.0)
            tvAmount.text = "R $amountString"
            btnPay.text = "Pay Now — R $amountString"
        }

        updateUI()

        // If accessed directly with empty bookingId, load user's active pending booking
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
            val amountInCents = (finalAmount * 100).toLong()
            val userEmail = auth.currentUser?.email ?: "customer@masandaza.co.za"
            val cleanBookingRef = bookingId.replace("#", "").trim().ifBlank { "BK-${(1000..9999).random()}" }

            btnPay.visibility = View.GONE
            webView.visibility = View.VISIBLE

            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true

            webView.webViewClient = object : WebViewClient() {
                @Suppress("DEPRECATION")
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url != null) {
                        if (url.contains("standard.paystack.co/close") || url.contains("trxref=")) {
                            onPaymentSuccessful(cleanBookingRef, finalAmount)
                            return true
                        } else if (url.contains("standard.paystack.co/cancel")) {
                            btnPay.visibility = View.VISIBLE
                            webView.visibility = View.GONE

                            // Prompt user allowing simulated test payment if key isn't setup
                            showPaymentOptionsDialog(cleanBookingRef, finalAmount)
                            return true
                        }
                    }
                    return super.shouldOverrideUrlLoading(view, url)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (url != null && (url.contains("standard.paystack.co/close") || url.contains("trxref="))) {
                        onPaymentSuccessful(cleanBookingRef, finalAmount)
                    }
                }
            }

            val paystackHtml = buildPaystackHtml(userEmail, amountInCents, cleanBookingRef, paystackPublicKey)
            webView.loadDataWithBaseURL("https://checkout.paystack.com", paystackHtml, "text/html", "UTF-8", null)
        }

        btnNavHome?.setOnClickListener {
            startActivity(Intent(this, DasboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
        btnNavBook?.setOnClickListener {
            startActivity(Intent(this, ServicesActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
        btnNavTrack?.setOnClickListener {
            startActivity(Intent(this, TrackBookingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
        btnNavProfile?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
    }

    private fun showPaymentOptionsDialog(bookingRef: String, amount: Double) {
        AlertDialog.Builder(this)
            .setTitle("Payment Notice")
            .setMessage("Paystack requires an active Test Public Key from your Paystack Dashboard.\n\nWould you like to simulate a successful payment for testing?")
            .setPositiveButton("Simulate Successful Payment") { _, _ ->
                onPaymentSuccessful(bookingRef, amount)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun buildPaystackHtml(
        email: String,
        amountInCents: Long,
        reference: String,
        publicKey: String
    ): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <script src="https://js.paystack.co/v1/inline.js"></script>
              <style>
                body {
                  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                  background-color: #F8FAFC;
                  display: flex;
                  flex-direction: column;
                  align-items: center;
                  justify-content: center;
                  height: 100vh;
                  margin: 0;
                  color: #0F172A;
                }
                .loader {
                  border: 4px solid #E2E8F0;
                  border-top: 4px solid #2563EB;
                  border-radius: 50%;
                  width: 36px;
                  height: 36px;
                  animation: spin 1s linear infinite;
                  margin-bottom: 16px;
                }
                @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
              </style>
            </head>
            <body>
              <div class="loader"></div>
              <p style="font-weight: bold;">Connecting to Paystack...</p>
              <script>
                function payWithPaystack() {
                  var handler = PaystackPop.setup({
                    key: '$publicKey',
                    email: '$email',
                    amount: $amountInCents,
                    currency: 'ZAR',
                    ref: '$reference',
                    callback: function(response) {
                      window.location.href = 'https://standard.paystack.co/close?trxref=' + response.reference;
                    },
                    onClose: function() {
                      window.location.href = 'https://standard.paystack.co/cancel';
                    }
                  });
                  handler.openIframe();
                }
                window.onload = payWithPaystack;
              </script>
            </body>
            </html>
        """.trimIndent()
    }

    private fun onPaymentSuccessful(bookingId: String, totalAmount: Double) {
        val userId = auth.currentUser?.uid ?: ""
        val receiptRef = "RCP-2025-" + (1000..9999).random()

        val newPayment = Payment(
            paymentId = receiptRef,
            bookingId = bookingId,
            customerId = userId,
            amount = totalAmount,
            paymentMethod = "Paystack ZAR",
            status = "Paid"
        )

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

                findViewById<Button>(R.id.btnSubmitPayment).visibility = View.VISIBLE
                findViewById<WebView>(R.id.payfastWebView).visibility = View.GONE
            }
        )
    }
}