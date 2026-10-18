package com.example.masandaza.models


data class Payment(
    val paymentId: String = "",
    val bookingId: String = "",
    val customerId: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "Card Payment",
    val status: String = "Paid",
    val timestamp: Long = System.currentTimeMillis()
)
