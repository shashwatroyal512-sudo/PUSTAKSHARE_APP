package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [User::class, Book::class, BorrowRequest::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bookDao(): BookDao
    abstract fun borrowRequestDao(): BorrowRequestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "book_share_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed data inside a coroutine scope
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        seedDatabase(database)
                    }
                }
            }
        }

        private suspend fun seedDatabase(db: AppDatabase) {
            val userDao = db.userDao()
            val bookDao = db.bookDao()

            // 1. Seed standard users
            val aliceId = userDao.insertUser(
                User(
                    email = "alice@example.com",
                    passwordHash = "password123", // simple for local prototype
                    name = "Alice Johnson",
                    phone = "+1 (555) 123-4567",
                    homeAddress = "123 Maple Avenue, Greenfield, NY"
                )
            )

            val bobId = userDao.insertUser(
                User(
                    email = "bob@example.com",
                    passwordHash = "bobsafe",
                    name = "Bob Smith",
                    phone = "+1 (555) 987-6543",
                    homeAddress = "456 Oak Street, Springfield, WA"
                )
            )

            val charlieId = userDao.insertUser(
                User(
                    email = "charlie@example.com",
                    passwordHash = "charlie1",
                    name = "Charlie Davis",
                    phone = "+1 (555) 444-5555",
                    homeAddress = "789 Pine Road, Metrocity, CA"
                )
            )

            // 2. Seed some classic books belonging to those owners
            bookDao.insertBook(
                Book(
                    title = "To Kill a Mockingbird",
                    author = "Harper Lee",
                    description = "A powerful, Pulitzer Prize-winning classic novel of warmth and humor, dealing with serious themes of racial injustice and loss of innocence.",
                    category = "Fiction",
                    coverUri = "placeholder_cover_1", // custom code tags we'll render as rich graphic covers in Compose
                    ownerId = aliceId,
                    ownerName = "Alice Johnson",
                    ownerAddress = "123 Maple Avenue, Greenfield, NY",
                    ownerPhone = "+1 (555) 123-4567"
                )
            )

            bookDao.insertBook(
                Book(
                    title = "Educated: A Memoir",
                    author = "Tara Westover",
                    description = "An unforgettable memoir about a young girl who, kept out of school, leaves her survivalist family and goes on to earn a PhD from Cambridge University.",
                    category = "Biography",
                    coverUri = "placeholder_cover_2",
                    ownerId = aliceId,
                    ownerName = "Alice Johnson",
                    ownerAddress = "123 Maple Avenue, Greenfield, NY",
                    ownerPhone = "+1 (555) 123-4567"
                )
            )

            bookDao.insertBook(
                Book(
                    title = "Atomic Habits",
                    author = "James Clear",
                    description = "An extremely practical guide to breaking bad habits and forming good ones through tiny, 1% everyday adjustments.",
                    category = "Self-Help",
                    coverUri = "placeholder_cover_3",
                    ownerId = bobId,
                    ownerName = "Bob Smith",
                    ownerAddress = "456 Oak Street, Springfield, WA",
                    ownerPhone = "+1 (555) 987-6543"
                )
            )

            bookDao.insertBook(
                Book(
                    title = "Dune",
                    author = "Frank Herbert",
                    description = "The classic science fiction masterpiece set in the far future amidst a sprawling interstellar empire where planetary dynasties fight for control.",
                    category = "Sci-Fi",
                    coverUri = "placeholder_cover_4",
                    ownerId = bobId,
                    ownerName = "Bob Smith",
                    ownerAddress = "456 Oak Street, Springfield, WA",
                    ownerPhone = "+1 (555) 987-6543"
                )
            )

            bookDao.insertBook(
                Book(
                    title = "Sapiens: A Brief History of Humankind",
                    author = "Yuval Noah Harari",
                    description = "An intellectual journey exploration tracing the development of Homo sapiens from ancient evolutionary ancestors down to the high-tech 21st century.",
                    category = "History",
                    coverUri = "placeholder_cover_5",
                    ownerId = charlieId,
                    ownerName = "Charlie Davis",
                    ownerAddress = "789 Pine Road, Metrocity, CA",
                    ownerPhone = "+1 (555) 444-5555"
                )
            )
        }
    }
}
