package com.vocalize.app.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun backup(dbFile: File, recordingsDir: File, onProgress: (String) -> Unit): Boolean {
        onProgress("Drive backup is disabled.")
        return false
    }

    suspend fun restore(recordingsDir: File, onProgress: (String) -> Unit): Boolean {
        onProgress("Drive restore is disabled.")
        return false
    }

    fun getLastBackupTime(): Long {
        val prefs = context.getSharedPreferences("vocalize_prefs", Context.MODE_PRIVATE)
        return prefs.getLong(Constants.PREFS_LAST_BACKUP, 0L)
    }

    fun saveLastBackupTime(time: Long) {
        context.getSharedPreferences("vocalize_prefs", Context.MODE_PRIVATE)
            .edit().putLong(Constants.PREFS_LAST_BACKUP, time).apply()
    }
}
