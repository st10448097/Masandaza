package com.example.masandaza.models

data class Booking(
    val bookingId: String = "",
    val customerId: String = "",
    val serviceName: String = "",
    val serviceAddress: String = "",
    val preferredDate: String = "",
    val preferredTime: String = "",
    val totalAmount: Double = 0.0,
    val depositPaid: Double = 0.0,
    val status: String = "Booked",
    val assignedStaffId: String = ""
)