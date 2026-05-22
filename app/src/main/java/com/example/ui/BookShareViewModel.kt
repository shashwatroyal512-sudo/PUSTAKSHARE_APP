package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookShareViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BookShareRepository

    // Current State Management
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Database Reactive Flows
    private val _allBooks = MutableStateFlow<List<Book>>(emptyList())
    val allBooks: StateFlow<List<Book>> = _allBooks.asStateFlow()

    private val _myBooks = MutableStateFlow<List<Book>>(emptyList())
    val myBooks: StateFlow<List<Book>> = _myBooks.asStateFlow()

    private val _incomingRequests = MutableStateFlow<List<BorrowRequest>>(emptyList())
    val incomingRequests: StateFlow<List<BorrowRequest>> = _incomingRequests.asStateFlow()

    private val _sentRequests = MutableStateFlow<List<BorrowRequest>>(emptyList())
    val sentRequests: StateFlow<List<BorrowRequest>> = _sentRequests.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = BookShareRepository(
            database.userDao(),
            database.bookDao(),
            database.borrowRequestDao()
        )

        // Keep local memory caches sync'd with database flow queries
        observeDatabaseData()
    }

    private fun observeDatabaseData() {
        viewModelScope.launch {
            repository.allBooks.collect { list ->
                _allBooks.value = list
                updateUserDependentLists()
            }
        }

        viewModelScope.launch {
            currentUser.collect { user ->
                updateUserDependentLists()
            }
        }
    }

    private fun updateUserDependentLists() {
        val user = _currentUser.value
        if (user == null) {
            _myBooks.value = emptyList()
            _incomingRequests.value = emptyList()
            _sentRequests.value = emptyList()
            return
        }

        // Filter user owned books
        _myBooks.value = _allBooks.value.filter { it.ownerId == user.id }

        // Observe borrower requests sent by current user
        viewModelScope.launch {
            repository.getRequestsByBorrower(user.id).collect { requests ->
                _sentRequests.value = requests
            }
        }

        // Observe incoming borrower requests received by current user (as book owner)
        viewModelScope.launch {
            repository.getRequestsByOwner(user.id).collect { requests ->
                _incomingRequests.value = requests
            }
        }
    }

    // --- AUTHENTICATION ---
    fun login(email: String, passwordHash: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            val user = withContext(Dispatchers.IO) {
                repository.getUserByEmail(email)
            }
            if (user != null && user.passwordHash == passwordHash) {
                _currentUser.value = user
                onResult(true)
            } else {
                _authError.value = "Invalid email or password."
                onResult(false)
            }
            _isLoading.value = false
        }
    }

    fun register(
        email: String,
        passwordHash: String,
        name: String,
        phone: String,
        homeAddress: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            
            // Check existing user
            val existingUser = withContext(Dispatchers.IO) {
                repository.getUserByEmail(email)
            }
            if (existingUser != null) {
                _authError.value = "An account with this email already exists."
                _isLoading.value = false
                onResult(false)
                return@launch
            }

            val newUser = User(
                email = email,
                passwordHash = passwordHash,
                name = name,
                phone = phone,
                homeAddress = homeAddress
            )

            try {
                val newId = withContext(Dispatchers.IO) {
                    repository.registerUser(newUser)
                }
                _currentUser.value = newUser.copy(id = newId)
                onResult(true)
            } catch (e: Exception) {
                _authError.value = "Registration failed. Please try again."
                onResult(false)
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        _currentUser.value = null
        _authError.value = null
    }

    // Fast switch simulation for testing
    fun switchLoggedMockUser(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val user = withContext(Dispatchers.IO) {
                repository.getUserByEmail(email)
            }
            if (user != null) {
                _currentUser.value = user
            }
            _isLoading.value = false
        }
    }

    // --- BOOK OPERATIONS ---
    fun uploadBook(
        title: String,
        author: String,
        description: String,
        category: String,
        coverUri: String?,
        onComplete: (Boolean) -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val newBook = Book(
                title = title,
                author = author,
                description = description,
                category = category,
                coverUri = coverUri,
                ownerId = user.id,
                ownerName = user.name,
                ownerAddress = user.homeAddress,
                ownerPhone = user.phone,
                isAvailable = true
            )
            withContext(Dispatchers.IO) {
                repository.insertBook(newBook)
            }
            _isLoading.value = false
            onComplete(true)
        }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBook(book)
        }
    }

    // --- BORROW REQUESTS ---
    fun sendBorrowRequest(book: Book, onComplete: (Boolean) -> Unit) {
        val borrower = _currentUser.value ?: return
        if (borrower.id == book.ownerId) {
            onComplete(false)
            return
        }

        viewModelScope.launch {
            val request = BorrowRequest(
                bookId = book.id,
                bookTitle = book.title,
                bookCoverUri = book.coverUri,
                ownerId = book.ownerId,
                ownerName = book.ownerName,
                borrowerId = borrower.id,
                borrowerName = borrower.name,
                borrowerPhone = borrower.phone,
                status = "PENDING",
                pickupAddress = book.ownerAddress
            )
            
            withContext(Dispatchers.IO) {
                repository.insertRequest(request)
            }
            onComplete(true)
        }
    }

    fun updateRequestStatus(request: BorrowRequest, nextStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Update request status
            repository.updateRequest(request.copy(status = nextStatus))

            // Sync book availability if necessary
            if (nextStatus == "ACCEPTED") {
                // If lender accepts borrow, temporarily mark book unavailable
                repository.getBookById(request.bookId).firstOrNull()?.let { book ->
                    repository.updateBook(book.copy(isAvailable = false))
                }
            } else if (nextStatus == "RETURNED" || nextStatus == "REJECTED") {
                // Mark book available again
                repository.getBookById(request.bookId).firstOrNull()?.let { book ->
                    repository.updateBook(book.copy(isAvailable = true))
                }
            }
        }
    }
}
