package com.mslabs.aegis.data.backup

import android.content.Context
import android.net.Uri
import com.mslabs.aegis.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Reads and writes backup payloads through the Storage Access Framework.
 *
 * The OS owns the file picker, so the app never needs storage permissions and the
 * user chooses the destination - Drive, Dropbox, local - themselves (tdd.md §3).
 * Every stream is wrapped in `use { }` so the descriptor closes even on failure
 * (dg.md §3).
 */
@Singleton
class BackupFileDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    suspend fun write(uri: Uri, payload: String) = withContext(ioDispatcher) {
        // "wt" truncates: without it, overwriting an existing backup with a shorter
        // one would leave the tail of the previous file behind and corrupt it.
        context.contentResolver.openOutputStream(uri, "wt")
            ?.use { stream -> stream.write(payload.encodeToByteArray()) }
            ?: throw IOException("Could not open the selected file for writing.")
    }

    suspend fun read(uri: Uri): String = withContext(ioDispatcher) {
        // AES-GCM authenticates the whole payload, so a backup has to be fully in
        // memory to be verified - it cannot be decrypted in a streaming fashion. The
        // cap is what keeps that bounded: it refuses an absurd file rather than
        // letting a mis-picked 2 GB video OOM the process (dg.md §3).
        val size = context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
        if (size > MAX_BACKUP_BYTES) {
            throw IOException("That file is too large to be an Aegis backup.")
        }

        context.contentResolver.openInputStream(uri)
            ?.use { stream -> stream.readBytes().decodeToString() }
            ?: throw IOException("Could not open the selected backup file.")
    }

    companion object {
        /** Passed to ACTION_CREATE_DOCUMENT / ACTION_OPEN_DOCUMENT. */
        const val BACKUP_MIME_TYPE = "application/octet-stream"

        private const val MAX_BACKUP_BYTES = 64L * 1024 * 1024

        fun defaultFileName(epochMillis: Long = System.currentTimeMillis()): String {
            val stamp = java.text.SimpleDateFormat("yyyyMMdd-HHmm", java.util.Locale.US)
                .format(java.util.Date(epochMillis))
            return "aegis-vault-$stamp.aegisbackup"
        }
    }
}
