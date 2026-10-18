package com.example.masandaza.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.example.masandaza.models.User
import com.example.masandaza.models.Booking
import com.example.masandaza.models.Payment

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()

    // Save user profile after registration
    fun saveUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(user.userId)
            .set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    // Fetch user role for redirection (Customer vs Staff)
    fun getUserRole(uid: String, onResult: (String?) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role")
                onResult(role)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    // Create a new cleaning service booking
    fun createBooking(
        booking: Booking,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val docRef = db.collection("bookings").document()
        val bookingWithId = booking.copy(bookingId = docRef.id)

        docRef.set(bookingWithId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    // Update job status (used by staff for order tracking and quality control)
    fun updateBookingStatus(bookingId: String, newStatus: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("bookings").document(bookingId)
            .update("status", newStatus)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    // Record payment details
    fun recordPayment(payment: Payment, onSuccess: () -> Unit, onFailure: (Exception?) -> Unit) {
        db.collection("payments")
            .document(payment.paymentId)
            .set(payment)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}

