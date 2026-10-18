package com.example.masandaza.models



data class User(
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = "customer", // options: "customer", "staff", "admin"
    val phoneNumber: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
