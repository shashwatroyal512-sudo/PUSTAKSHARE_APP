package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val passwordHash: String,
    val name: String,
    val phone: String,
    val homeAddress: String
)

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String,
    val description: String,
    val category: String,
    val coverUri: String?, // Stores dynamic file URI or custom local placeholder identifier
    val ownerId: Long,
    val ownerName: String,
    val ownerAddress: String,
    val ownerPhone: String,
    val isAvailable: Boolean = true,
    val listedDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "borrow_requests")
data class BorrowRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val bookTitle: String,
    val bookCoverUri: String?,
    val ownerId: Long,
    val ownerName: String,
    val borrowerId: Long,
    val borrowerName: String,
    val borrowerPhone: String,
    val requestDate: Long = System.currentTimeMillis(),
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED, RECEIVED (Taken), RETURNED (Completed)
    val pickupAddress: String
)
