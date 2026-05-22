package com.example.data

import kotlinx.coroutines.flow.Flow

class BookShareRepository(
    private val userDao: UserDao,
    private val bookDao: BookDao,
    private val borrowRequestDao: BorrowRequestDao
) {
    // --- USER ACTIONS ---
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    fun getUserById(id: Long): Flow<User?> {
        return userDao.getUserById(id)
    }

    suspend fun registerUser(user: User): Long {
        return userDao.insertUser(user)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    // --- BOOK ACTIONS ---
    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()

    fun getBookById(id: Long): Flow<Book?> {
        return bookDao.getBookById(id)
    }

    fun getBooksByOwner(ownerId: Long): Flow<List<Book>> {
        return bookDao.getBooksByOwner(ownerId)
    }

    suspend fun insertBook(book: Book): Long {
        return bookDao.insertBook(book)
    }

    suspend fun updateBook(book: Book) {
        bookDao.updateBook(book)
    }

    suspend fun deleteBook(book: Book) {
        bookDao.deleteBook(book)
    }

    // --- REQUEST ACTIONS ---
    val allRequests: Flow<List<BorrowRequest>> = borrowRequestDao.getAllRequests()

    fun getRequestsByOwner(ownerId: Long): Flow<List<BorrowRequest>> {
        return borrowRequestDao.getRequestsByOwner(ownerId)
    }

    fun getRequestsByBorrower(borrowerId: Long): Flow<List<BorrowRequest>> {
        return borrowRequestDao.getRequestsByBorrower(borrowerId)
    }

    fun getRequestById(id: Long): Flow<BorrowRequest?> {
        return borrowRequestDao.getRequestById(id)
    }

    suspend fun insertRequest(request: BorrowRequest): Long {
        return borrowRequestDao.insertRequest(request)
    }

    suspend fun updateRequest(request: BorrowRequest) {
        borrowRequestDao.updateRequest(request)
    }
}
