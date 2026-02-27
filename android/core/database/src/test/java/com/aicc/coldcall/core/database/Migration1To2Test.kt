package com.aicc.coldcall.core.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Migration1To2Test {

    private lateinit var db: SupportSQLiteDatabase
    private val dbName = "migration_test.db"

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Delete any stale test database
        context.deleteDatabase(dbName)

        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    // Recreate v1 schema (contacts + call_logs only)
                    db.execSQL(
                        """CREATE TABLE IF NOT EXISTS `contacts` (
                            `id` TEXT NOT NULL PRIMARY KEY,
                            `name` TEXT NOT NULL,
                            `phone` TEXT NOT NULL,
                            `business` TEXT,
                            `city` TEXT,
                            `industry` TEXT,
                            `deal_stage` TEXT NOT NULL,
                            `last_called` TEXT,
                            `call_count` INTEGER NOT NULL,
                            `last_call_summary` TEXT,
                            `recording_link` TEXT,
                            `next_follow_up` TEXT,
                            `notes` TEXT,
                            `created_at` TEXT
                        )"""
                    )
                    db.execSQL(
                        """CREATE TABLE IF NOT EXISTS `call_logs` (
                            `id` TEXT NOT NULL PRIMARY KEY,
                            `contact_id` TEXT NOT NULL,
                            `timestamp` TEXT NOT NULL,
                            `duration_seconds` INTEGER NOT NULL,
                            `disposition` TEXT NOT NULL,
                            `summary` TEXT,
                            `deal_stage` TEXT NOT NULL,
                            `recording_url` TEXT,
                            `transcript` TEXT
                        )"""
                    )
                }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int,
                ) {}
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        db = helper.writableDatabase
    }

    @After
    fun tearDown() {
        db.close()
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(dbName)
    }

    @Test
    fun `migration creates recordings table with expected columns`() {
        // Insert v1 data
        insertSampleContact()
        insertSampleCallLog()

        // Run migration
        MIGRATION_1_2.migrate(db)

        // Verify recordings table exists with correct columns
        val cursor = db.query("PRAGMA table_info(recordings)")
        val columns = mutableMapOf<String, String>()
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val type = cursor.getString(cursor.getColumnIndexOrThrow("type"))
            columns[name] = type
        }
        cursor.close()

        assertEquals("INTEGER", columns["id"])
        assertEquals("TEXT", columns["contact_id"])
        assertEquals("TEXT", columns["file_path"])
        assertEquals("TEXT", columns["remote_url"])
        assertEquals("INTEGER", columns["is_uploaded"])
        assertEquals("INTEGER", columns["created_at"])
        assertEquals(6, columns.size)
    }

    @Test
    fun `migration preserves existing contacts data`() {
        insertSampleContact()

        MIGRATION_1_2.migrate(db)

        val cursor = db.query("SELECT * FROM contacts WHERE id = 'c1'")
        assertTrue(cursor.moveToFirst())
        assertEquals("Alice", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        assertEquals("555-1234", cursor.getString(cursor.getColumnIndexOrThrow("phone")))
        assertEquals("Qualified", cursor.getString(cursor.getColumnIndexOrThrow("deal_stage")))
        cursor.close()
    }

    @Test
    fun `migration preserves existing call_logs data`() {
        insertSampleCallLog()

        MIGRATION_1_2.migrate(db)

        val cursor = db.query("SELECT * FROM call_logs WHERE id = 'log1'")
        assertTrue(cursor.moveToFirst())
        assertEquals("c1", cursor.getString(cursor.getColumnIndexOrThrow("contact_id")))
        assertEquals("Connected", cursor.getString(cursor.getColumnIndexOrThrow("disposition")))
        assertEquals(120, cursor.getInt(cursor.getColumnIndexOrThrow("duration_seconds")))
        cursor.close()
    }

    @Test
    fun `recordings table accepts inserts after migration`() {
        MIGRATION_1_2.migrate(db)

        val values = ContentValues().apply {
            put("contact_id", "c1")
            put("file_path", "/data/recordings/call_c1_1000.m4a")
            put("is_uploaded", 0)
            put("created_at", 1000L)
        }
        val rowId = db.insert("recordings", SQLiteDatabase.CONFLICT_NONE, values)
        assertTrue(rowId > 0)

        val cursor = db.query("SELECT * FROM recordings WHERE id = $rowId")
        assertTrue(cursor.moveToFirst())
        assertEquals("c1", cursor.getString(cursor.getColumnIndexOrThrow("contact_id")))
        assertEquals("/data/recordings/call_c1_1000.m4a", cursor.getString(cursor.getColumnIndexOrThrow("file_path")))
        assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("is_uploaded")))
        assertEquals(1000L, cursor.getLong(cursor.getColumnIndexOrThrow("created_at")))
        cursor.close()
    }

    @Test
    fun `is_uploaded defaults to 0`() {
        MIGRATION_1_2.migrate(db)

        db.execSQL(
            "INSERT INTO recordings (contact_id, file_path, created_at) VALUES ('c1', '/path/file.m4a', 500)"
        )

        val cursor = db.query("SELECT is_uploaded FROM recordings")
        assertTrue(cursor.moveToFirst())
        assertEquals(0, cursor.getInt(0))
        cursor.close()
    }

    @Test
    fun `remote_url is nullable`() {
        MIGRATION_1_2.migrate(db)

        db.execSQL(
            "INSERT INTO recordings (contact_id, file_path, created_at) VALUES ('c1', '/path/file.m4a', 500)"
        )

        val cursor = db.query("SELECT remote_url FROM recordings")
        assertTrue(cursor.moveToFirst())
        assertTrue(cursor.isNull(0))
        cursor.close()
    }

    @Test
    fun `migration is idempotent`() {
        MIGRATION_1_2.migrate(db)
        // Running again should not throw (CREATE TABLE IF NOT EXISTS)
        MIGRATION_1_2.migrate(db)

        val cursor = db.query("PRAGMA table_info(recordings)")
        assertTrue(cursor.count > 0)
        cursor.close()
    }

    private fun insertSampleContact() {
        val values = ContentValues().apply {
            put("id", "c1")
            put("name", "Alice")
            put("phone", "555-1234")
            put("deal_stage", "Qualified")
            put("call_count", 3)
        }
        db.insert("contacts", SQLiteDatabase.CONFLICT_NONE, values)
    }

    private fun insertSampleCallLog() {
        val values = ContentValues().apply {
            put("id", "log1")
            put("contact_id", "c1")
            put("timestamp", "2026-02-27T10:00:00")
            put("duration_seconds", 120)
            put("disposition", "Connected")
            put("deal_stage", "Contacted")
        }
        db.insert("call_logs", SQLiteDatabase.CONFLICT_NONE, values)
    }
}
