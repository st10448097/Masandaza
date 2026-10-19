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

class PaymentActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firebaseRepo = FirebaseRepository()

    // added PayFast Sandbox credentials because these are required to build the URL that opens the PayFast payment page.
    private val merchantId = "10056173"
    private val merchantKey = "r2p8we7llawsa"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val bookingId = intent.getStringExtra("bookingId") ?: ""
        val totalAmount = intent.getDoubleExtra("totalAmount", 0.0)

        val tvRef = findViewById<TextView>(R.id.tvPaymentBookingRef)
        val tvAmount = findViewById<TextView>(R.id.tvPaymentAmount)
        val btnPay = findViewById<Button>(R.id.btnSubmitPayment)

        // Finding the WebView so we can load PayFast into it
        val webView = findViewById<WebView>(R.id.payfastWebView)

        // removed all the EditText findViewById lines (etCardName, etCardNum, etc.)
        // the reason to this is because we are letting PayFast's secure website handle the card inputs, not our app.

        tvRef.text = if (bookingId.isNotBlank()) "#$bookingId" else "#BK-PENDING"

        //Locale.US formatting. PayFast strictly requires amounts to be formatted like "450.00" (no commas, 2 decimals).
        val amountString = String.format(Locale.US, "%.2f", totalAmount)

        tvAmount.text = "R $amountString"
        btnPay.text = "Pay Now — R $amountString"

        btnPay.setOnClickListener {
            // removed the entire 'when' block that validated card numbers, CVVs, and expiry dates because PayFast's secure page handles all validation now.

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
                    // Note: For a school project, this simple check is enough.
                    // In a real app, you would use a server webhook.
                    if (url != null && url.contains("sandbox.payfast.co.za") && url.contains("successful")) {
                        onPaymentSuccessful(bookingId, totalAmount)
                    }
                }
            }

            // Actually load the PayFast page
            webView.loadUrl(paymentUrl)
        }
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