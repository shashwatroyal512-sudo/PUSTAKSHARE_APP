package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.Book
import com.example.data.BorrowRequest
import com.example.data.User

// Define Bottom Navigation tab indices
enum class AppTab(val title: String, val icon: ImageVector) {
    BROWSE("Browse", Icons.Outlined.MenuBook),
    MY_BOOKS("My Books", Icons.Outlined.LibraryBooks),
    BORROWS("Borrows", Icons.Outlined.HistoryEdu),
    PROFILE("Profile", Icons.Outlined.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookShareMainScreen(viewModel: BookShareViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(AppTab.BROWSE) }
    var selectedBookForDetails by remember { mutableStateOf<Book?>(null) }
    var showAddBookDialog by remember { mutableStateOf(false) }

    // Authentication screens if no active user session
    if (currentUser == null) {
        var isRegisterScreen by remember { mutableStateOf(false) }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                if (isRegisterScreen) {
                    RegisterScreen(
                        viewModel = viewModel,
                        errorMsg = authError,
                        isLoading = isLoading,
                        onLoginSelected = { isRegisterScreen = false }
                    )
                } else {
                    LoginScreen(
                        viewModel = viewModel,
                        errorMsg = authError,
                        isLoading = isLoading,
                        onRegisterSelected = { isRegisterScreen = true }
                    )
                }
            }
        }
    } else {
        // Main App Layout
        val user = currentUser!!
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Logo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Book Share",
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    },
                    actions = {
                        // Fast user accounts switcher option
                        var showTesterDropdown by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { showTesterDropdown = !showTesterDropdown },
                            modifier = Modifier.testTag("accounts_switcher_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapCalls,
                                contentDescription = "Quick Switch Testing Account"
                            )
                        }
                        DropdownMenu(
                            expanded = showTesterDropdown,
                            onDismissRequest = { showTesterDropdown = false }
                        ) {
                            Text(
                                text = " Quick Admin Simulation",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            DropdownMenuItem(
                                text = { Text("As Alice Johnson (Lender / Owner)") },
                                onClick = {
                                    viewModel.switchLoggedMockUser("alice@example.com")
                                    showTesterDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("As Bob Smith (Owner / Borrower)") },
                                onClick = {
                                    viewModel.switchLoggedMockUser("bob@example.com")
                                    showTesterDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("As Charlie Davis (Owner)") },
                                onClick = {
                                    viewModel.switchLoggedMockUser("charlie@example.com")
                                    showTesterDropdown = false
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    AppTab.values().forEach { tab ->
                        val selected = currentTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (selected) {
                                        when (tab) {
                                            AppTab.BROWSE -> Icons.Filled.MenuBook
                                            AppTab.MY_BOOKS -> Icons.Filled.LibraryBooks
                                            AppTab.BORROWS -> Icons.Filled.HistoryEdu
                                            AppTab.PROFILE -> Icons.Filled.Person
                                        }
                                    } else tab.icon,
                                    contentDescription = tab.title
                                )
                            },
                            label = { Text(tab.title) },
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            },
            floatingActionButton = {
                if (currentTab == AppTab.MY_BOOKS) {
                    ExtendedFloatingActionButton(
                        onClick = { showAddBookDialog = true },
                        icon = { Icon(Icons.Default.Add, contentDescription = "List Book") },
                        text = { Text("List Book") },
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = Color.White,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .testTag("add_book_fab")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    AppTab.BROWSE -> BrowseBooksTab(
                        viewModel = viewModel,
                        onBookSelected = { selectedBookForDetails = it }
                    )
                    AppTab.MY_BOOKS -> MyBooksTab(
                        viewModel = viewModel,
                        onBookSelected = { selectedBookForDetails = it }
                    )
                    AppTab.BORROWS -> BorrowsListTab(
                        viewModel = viewModel
                    )
                    AppTab.PROFILE -> ProfileTab(
                        viewModel = viewModel,
                        currentUser = user
                    )
                }
            }
        }
    }

    // Detail Dialog Overlay
    selectedBookForDetails?.let { book ->
        BookDetailsDialog(
            book = book,
            viewModel = viewModel,
            onDismiss = { selectedBookForDetails = null }
        )
    }

    // Add Book Overlay
    if (showAddBookDialog) {
        AddBookDialog(
            viewModel = viewModel,
            onDismiss = { showAddBookDialog = false }
        )
    }
}

// --- SUB SCREEN: LOGIN ---
@Composable
fun LoginScreen(
    viewModel: BookShareViewModel,
    errorMsg: String?,
    isLoading: Boolean,
    onRegisterSelected: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "Books Logo",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome to Book Share",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Local book-lending community platform",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (errorMsg != null) {
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.testTag("auth_error_tag")
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_email"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Button(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            viewModel.login(email.trim(), password) { success -> }
                        }
                    },
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_btn")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Sign In")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Don't have an account? ")
            Text(
                text = "Register Now",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onRegisterSelected() }
                    .testTag("goto_register_btn")
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(modifier = Modifier.height(16.dp))

        // Easy Simulator Quick Login Helpers
        Text(
            text = "Simulator Quick Login:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SuggestionChip(
                onClick = {
                    email = "alice@example.com"
                    password = "password123"
                },
                label = { Text("Alice") },
                modifier = Modifier.testTag("quick_fill_alice")
            )
            SuggestionChip(
                onClick = {
                    email = "bob@example.com"
                    password = "bobsafe"
                },
                label = { Text("Bob") }
            )
            SuggestionChip(
                onClick = {
                    email = "charlie@example.com"
                    password = "charlie1"
                },
                label = { Text("Charlie") }
            )
        }
    }
}

// --- SUB SCREEN: REGISTER ---
@Composable
fun RegisterScreen(
    viewModel: BookShareViewModel,
    errorMsg: String?,
    isLoading: Boolean,
    onLoginSelected: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var homeAddress by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Text(
                text = "Create Account",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = "Register to start borrowing and lending physical books",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (errorMsg != null) {
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("reg_name")
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("reg_email"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
                    singleLine = true,
                    placeholder = { Text("e.g. +1 555-0199") },
                    modifier = Modifier.fillMaxWidth().testTag("reg_phone"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                OutlinedTextField(
                    value = homeAddress,
                    onValueChange = { homeAddress = it },
                    label = { Text("Home Address (For Book Pick-up)") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = "Home Pick-up Address") },
                    maxLines = 3,
                    placeholder = { Text("Enter exact street, block & city for lending coordination.") },
                    modifier = Modifier.fillMaxWidth().testTag("reg_address")
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("reg_password"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Button(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank() && name.isNotBlank() && phone.isNotBlank() && homeAddress.isNotBlank()) {
                            viewModel.register(
                                email = email.trim(),
                                passwordHash = password,
                                name = name.trim(),
                                phone = phone.trim(),
                                homeAddress = homeAddress.trim()
                            ) { success -> }
                        }
                    },
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && name.isNotBlank() && phone.isNotBlank() && homeAddress.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("reg_submit_btn")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Register")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already registered? ")
            Text(
                text = "Sign In",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onLoginSelected() }
                    .testTag("goto_login_btn")
            )
        }
    }
}

// --- TAB 1: BROWSE BOOKS ---
@Composable
fun BrowseBooksTab(
    viewModel: BookShareViewModel,
    onBookSelected: (Book) -> Unit
) {
    val books by viewModel.allBooks.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Fiction", "Biography", "Sci-Fi", "Self-Help", "History", "HistoryEdu", "Other")

    val filteredBooks = books.filter { book ->
        val matchesSearch = book.title.contains(searchQuery, ignoreCase = true) ||
                book.author.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "All" || book.category == selectedCategory
        matchesSearch && matchesCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by title, author...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("search_books_input"),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
            )
        )

        // Horizontal Categories List
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
            edgePadding = 0.dp,
            divider = {},
            containerColor = Color.Transparent,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            categories.forEach { category ->
                Tab(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    text = {
                        Text(
                            text = category,
                            fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredBooks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentPasteOff,
                        contentDescription = "No books",
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No books available",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Check search parameters or switch users to add a book!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("books_grid")
            ) {
                items(filteredBooks) { book ->
                    val isOwnedByMe = book.ownerId == currentUser?.id
                    BookCard(
                        book = book,
                        isMyListedBook = isOwnedByMe,
                        onClick = { onBookSelected(book) }
                    )
                }
            }
        }
    }
}

// --- TAB 2: MY BOOKS TAB ---
@Composable
fun MyBooksTab(
    viewModel: BookShareViewModel,
    onBookSelected: (Book) -> Unit
) {
    val myBooks by viewModel.myBooks.collectAsStateWithLifecycle()
    val incomingRequests by viewModel.incomingRequests.collectAsStateWithLifecycle()

    var showActiveRequestsState by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Toggle tabs within My Books: Books listed vs Incoming Requests received
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!showActiveRequestsState) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (!showActiveRequestsState) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp),
                onClick = { showActiveRequestsState = false },
                modifier = Modifier
                    .weight(1f)
                    .testTag("listed_books_tab_btn")
            ) {
                Text("Books Listed (${myBooks.size})")
            }

            // Highlighting active requests number
            val hasPending = incomingRequests.any { it.status == "PENDING" }
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showActiveRequestsState) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (showActiveRequestsState) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp),
                onClick = { showActiveRequestsState = true },
                modifier = Modifier
                    .weight(1f)
                    .testTag("incoming_requests_tab_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Requests (${incomingRequests.size})")
                    if (hasPending) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Red)
                        )
                    }
                }
            }
        }

        if (!showActiveRequestsState) {
            // Listed Books Subview
            if (myBooks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Empty",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No books listed yet",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Click 'List Book' FAB below to share your books!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .testTag("my_listed_books_grid")
                ) {
                    items(myBooks) { book ->
                        BookCard(
                            book = book,
                            isMyListedBook = true,
                            onClick = { onBookSelected(book) }
                        )
                    }
                }
            }
        } else {
            // Incoming Requests Subview (Requests from other borrowers to My Books)
            if (incomingRequests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SwapCalls,
                            contentDescription = "Empty",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No borrow requests yet",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Borrow requests from other users will appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .testTag("incoming_requests_list")
                ) {
                    items(incomingRequests) { req ->
                        IncomingRequestCard(
                            request = req,
                            onAccept = { viewModel.updateRequestStatus(req, "ACCEPTED") },
                            onReject = { viewModel.updateRequestStatus(req, "REJECTED") },
                            onReceived = { viewModel.updateRequestStatus(req, "RECEIVED") },
                            onReturned = { viewModel.updateRequestStatus(req, "RETURNED") }
                        )
                    }
                }
            }
        }
    }
}

// --- TAB 3: BORROWS HISTORY TAB ---
@Composable
fun BorrowsListTab(
    viewModel: BookShareViewModel
) {
    val sentRequests by viewModel.sentRequests.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "My Borrow Requests",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        if (sentRequests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.HistoryEdu,
                        contentDescription = "Empty history",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No borrowing requests",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Browse books and click 'Request to Borrow' to start!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("borrows_history_list")
            ) {
                items(sentRequests) { req ->
                    SentRequestCard(request = req)
                }
            }
        }
    }
}

// --- TAB 4: PROFILE & LOGOUT ---
@Composable
fun ProfileTab(
    viewModel: BookShareViewModel,
    currentUser: User
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(45.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentUser.name.take(1).uppercase(),
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = currentUser.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = currentUser.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Profile details Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Contact & Pick-up Info",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "phone",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column {
                        Text("Phone Number", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                        Text(currentUser.phone, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "address",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp, top = 2.dp)
                    )
                    Column {
                        Text("Home / Pick-up Address", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                        Text(currentUser.homeAddress, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "(When other users borrow books, this is where they will come to pick them up.)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedButton(
            onClick = { viewModel.logout() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("logout_btn"),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "logout")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout Session", fontWeight = FontWeight.Bold)
        }
    }
}

// --- BOOK CARD COMPONENT ---
@Composable
fun BookCard(
    book: Book,
    isMyListedBook: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("book_card_${book.id}"),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            BookCoverRenderer(
                title = book.title,
                author = book.author,
                coverUri = book.coverUri,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            )

            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = book.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "By ${book.author}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Availability label style
                    if (isMyListedBook) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Mine") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = null
                        )
                    } else if (book.isAvailable) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Available", fontSize = 10.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0xFFE8F5E9),
                                labelColor = Color(0xFF2E7D32)
                            ),
                            border = null,
                            modifier = Modifier.height(24.dp)
                        )
                    } else {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Borrowed", fontSize = 10.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0xFFFFEBEE),
                                labelColor = Color(0xFFC62828)
                            ),
                            border = null,
                            modifier = Modifier.height(24.dp)
                        )
                    }

                    Text(
                        text = book.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// --- GORGEOUS DYNAMIC BOOK COVER RENDERER ---
@Composable
fun BookCoverRenderer(
    title: String,
    author: String,
    coverUri: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 16.dp, topStart = 16.dp, topEnd = 16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        if (coverUri != null && coverUri.startsWith("content://")) {
            // Static/Dynamic dynamic load library files
            AsyncImage(
                model = coverUri,
                contentDescription = "$title Cover Page",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Graphic Custom Studio Editorial Style Gradient Cover Fallback
            val gradientScheme = when (coverUri) {
                "placeholder_cover_1" -> Brush.verticalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))) // Navy/Gold
                "placeholder_cover_2" -> Brush.verticalGradient(listOf(Color(0xFF065F46), Color(0xFF10B981))) // Emerald
                "placeholder_cover_3" -> Brush.verticalGradient(listOf(Color(0xFF7C2D12), Color(0xFFEA580C))) // Rust orange
                "placeholder_cover_4" -> Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF4B5563))) // Dark charcoal
                "placeholder_cover_5" -> Brush.verticalGradient(listOf(Color(0xFF581C87), Color(0xFFA855F7))) // Purple Grape
                else -> Brush.verticalGradient(listOf(Color(0xFF3F4E4F), Color(0xFF2C3639))) // Neutral
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradientScheme)
                    .padding(12.dp)
            ) {
                // Book spine highlight strip
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(6.dp)
                        .background(Color.White.copy(alpha = 0.15f))
                        .align(Alignment.CenterStart)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )

                    Column {
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.25f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = author,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "LEND COPY",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

// --- INCOMING REQUESTS CARD ---
@Composable
fun IncomingRequestCard(
    request: BorrowRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onReceived: () -> Unit,
    onReturned: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Request for: ${request.bookTitle}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "From borrower: ${request.borrowerName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Status chip tag
                val tagColor = when (request.status) {
                    "PENDING" -> Color(0xFFFBF0B2) to Color(0xFF6B4B03)
                    "ACCEPTED" -> Color(0xFFE3F2FD) to Color(0xFF0D47A1)
                    "RECEIVED" -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
                    "RETURNED" -> Color(0xFFECEFF1) to Color(0xFF37474F)
                    else -> Color(0xFFFFEBEE) to Color(0xFFB71C1C)
                }
                Box(
                    modifier = Modifier
                        .background(tagColor.first, shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = request.status,
                        color = tagColor.second,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "Contact Phone: ${request.borrowerPhone}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (request.status) {
                    "PENDING" -> {
                        OutlinedButton(
                            onClick = onReject,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Reject")
                        }
                        Button(
                            onClick = onAccept,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF386641) // green approval
                            )
                        ) {
                            Text("Accept Borrow")
                        }
                    }
                    "ACCEPTED" -> {
                        Text(
                            text = "Awaiting borrower pickup",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )
                        Button(
                            onClick = onReceived
                        ) {
                            Text("Mark Handed Over")
                        }
                    }
                    "RECEIVED" -> {
                        Text(
                            text = "Currently Out Handed Over",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )
                        Button(
                            onClick = onReturned,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("Mark Returned")
                        }
                    }
                    "RETURNED" -> {
                        Text(
                            text = "Transaction completed & book returned.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    "REJECTED" -> {
                        Text(
                            text = "Request rejected.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

// --- SENT REQUEST CARD (BORROWER) ---
@Composable
fun SentRequestCard(request: BorrowRequest) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.bookTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Owned by: ${request.ownerName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                val tagColor = when (request.status) {
                    "PENDING" -> Color(0xFFFBF0B2) to Color(0xFF6B4B03)
                    "ACCEPTED" -> Color(0xFFE3F2FD) to Color(0xFF0D47A1)
                    "RECEIVED" -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
                    "RETURNED" -> Color(0xFFECEFF1) to Color(0xFF37474F)
                    else -> Color(0xFFFFEBEE) to Color(0xFFB71C1C)
                }
                Box(
                    modifier = Modifier
                        .background(tagColor.first, shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = request.status,
                        color = tagColor.second,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider()

            when (request.status) {
                "PENDING" -> {
                    Text(
                        text = "Waiting for owner response. Once approved, the pick-up house location with contact number of owner will be revealed details here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                "ACCEPTED" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "APPROVED!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🏠 Pick-up Location / Home address:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = request.pickupAddress,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "📞 Owner Handphone:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "Please reach out to arrange collection.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                "RECEIVED" -> {
                    Text(
                        text = "Enjoy! You currently have the book in possession. Please return to owner's home when finished.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1B5E20)
                    )
                }
                "RETURNED" -> {
                    Text(
                        text = "Returned successfully! Thanks for sharing books in the community.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                "REJECTED" -> {
                    Text(
                        text = "The request was declined by the owner.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// --- DIALOG DETAIL MODAL ---
@Composable
fun BookDetailsDialog(
    book: Book,
    viewModel: BookShareViewModel,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isMyBook = book.ownerId == currentUser?.id
    var borrowFeedbackMsg by remember { mutableStateOf("") }
    var borrowSuccessful by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 24.dp)
                .testTag("book_details_modal"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header dismiss bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lending Details",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close dialog")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BookCoverRenderer(
                        title = book.title,
                        author = book.author,
                        coverUri = book.coverUri,
                        modifier = Modifier
                            .width(110.dp)
                            .height(160.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = book.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "Author: ${book.author}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = book.category,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (!isMyBook) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Lender: ${book.ownerName}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                // Description
                Column {
                    Text("Synopsis", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = book.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()

                // Request status Feedback messages
                if (borrowFeedbackMsg.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (borrowSuccessful) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = borrowFeedbackMsg,
                            color = if (borrowSuccessful) Color(0xFF2E7D32) else Color(0xFFC62828),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Buttons action section
                if (isMyBook) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.deleteBook(book)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "delete")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Remove Book listing")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                viewModel.sendBorrowRequest(book) { success ->
                                    if (success) {
                                        borrowSuccessful = true
                                        borrowFeedbackMsg = "Borrow request submitted successfully! Notify the owner to coordinate."
                                    } else {
                                        borrowSuccessful = false
                                        borrowFeedbackMsg = "Failed to submit request. You cannot borrow your own record."
                                    }
                                }
                            },
                            enabled = book.isAvailable && !borrowSuccessful,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("request_borrow_btn")
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Borrow")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (book.isAvailable) "Request to Borrow (FREE)" 
                                else "Book currently borrowed / unavailable"
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- DIALOG UPLOAD/LIST NEW BOOK ---
@Composable
fun AddBookDialog(
    viewModel: BookShareViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Fiction") }
    var coverUri by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Fiction", "Biography", "Sci-Fi", "Self-Help", "History", "HistoryEdu", "Other")
    
    // Modern photo picker contract launcher
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coverUri = uri.toString()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 16.dp)
                .testTag("add_book_modal"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lend a Book",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close dialog")
                    }
                }

                Text(
                    text = "Upload the book details below. Other users can then request to borrow this book and pick it up from your house.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                // Book cover uploader display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(110.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (coverUri != null) {
                            if (coverUri!!.startsWith("content://")) {
                                AsyncImage(
                                    model = coverUri,
                                    contentDescription = "Selected Cover Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Gray)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Add cover",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                pickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("upload_cover_btn")
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = "photo picker")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upload JPG/JPEG", fontSize = 12.sp)
                        }

                        Text(
                            text = "Or choose placeholder theme cover:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        // Set custom beautiful placeholder graphics row if they don't have images on simulator
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("1", "2", "3", "4", "5").forEach { num ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when (num) {
                                                "1" -> Color(0xFF1E3A8A)
                                                "2" -> Color(0xFF065F46)
                                                "3" -> Color(0xFF7C2D12)
                                                "4" -> Color(0xFF111827)
                                                else -> Color(0xFF581C87)
                                            }
                                        )
                                        .border(
                                            width = if (coverUri == "placeholder_cover_$num") 2.dp else 0.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .clickable { coverUri = "placeholder_cover_$num" }
                                )
                            }
                        }
                    }
                }

                // Input form
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Book Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_book_title")
                )

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_book_author")
                )

                // Category dropdown simulation with buttons
                Text(
                    text = "Select Book Category:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Synopsis / Condition Details") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth().testTag("add_book_desc")
                )

                Button(
                    onClick = {
                        if (title.isNotBlank() && author.isNotBlank() && description.isNotBlank()) {
                            viewModel.uploadBook(
                                title = title.trim(),
                                author = author.trim(),
                                description = description.trim(),
                                category = category,
                                coverUri = coverUri ?: "placeholder_cover_1"
                            ) { success ->
                                if (success) {
                                    onDismiss()
                                }
                            }
                        }
                    },
                    enabled = title.isNotBlank() && author.isNotBlank() && description.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_book_btn")
                ) {
                    Text("List book for borrowing")
                }
            }
        }
    }
}

// FlowRow layout implementation backport to avoid experimental dependencies block in older gradle.
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val count = 4
        Column {
            Row(horizontalArrangement = horizontalArrangement, verticalAlignment = Alignment.CenterVertically) {
                // Split elements logically inside Box wrappers
                content()
            }
        }
    }
}

@Composable
fun rememberScrollState() = androidx.compose.foundation.rememberScrollState()
@Composable
fun rememberScrollState(initial: Int = 0) = androidx.compose.foundation.rememberScrollState(initial)
