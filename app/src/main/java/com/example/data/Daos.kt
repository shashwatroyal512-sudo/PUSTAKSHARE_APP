package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Long): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)
}

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY listedDate DESC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    fun getBookById(id: Long): Flow<Book?>

    @Query("SELECT * FROM books WHERE ownerId = :ownerId ORDER BY listedDate DESC")
    fun getBooksByOwner(ownerId: Long): Flow<List<Book>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)
}

@Dao
interface BorrowRequestDao {
    @Query("SELECT * FROM borrow_requests ORDER BY requestDate DESC")
    fun getAllRequests(): Flow<List<BorrowRequest>>

    @Query("SELECT * FROM borrow_requests WHERE ownerId = :ownerId ORDER BY requestDate DESC")
    fun getRequestsByOwner(ownerId: Long): Flow<List<BorrowRequest>>

    @Query("SELECT * FROM borrow_requests WHERE borrowerId = :borrowerId ORDER BY requestDate DESC")
    fun getRequestsByBorrower(borrowerId: Long): Flow<List<BorrowRequest>>

    @Query("SELECT * FROM borrow_requests WHERE id = :id LIMIT 1")
    fun getRequestById(id: Long): Flow<BorrowRequest?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: BorrowRequest): Long

    @Update
    suspend fun updateRequest(request: BorrowRequest)
}
