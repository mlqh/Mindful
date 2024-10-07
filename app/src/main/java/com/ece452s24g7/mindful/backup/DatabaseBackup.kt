package com.ece452s24g7.mindful.backup

import android.content.Context
import android.os.Environment
import com.ece452s24g7.mindful.database.EntryDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

const val BACKUP_DIR = "Documents/MindfulBackups"
const val CIPHER_ALGORITHM = "AES"
const val CIPHER_MODE = "CBC"
const val CIPHER_PADDING = "PKCS5Padding"

class DatabaseBackup (private val context: Context) {

    private val cipher =
        Cipher.getInstance("$CIPHER_ALGORITHM/$CIPHER_MODE/$CIPHER_PADDING")

    fun createBackup(name: String, key: String) {
        val backupName = "$name.db"
        val backupDir = File(Environment.getExternalStorageDirectory(), BACKUP_DIR)
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }

        val dbFile = context.getDatabasePath("entry.db")
        val outputFile = File(backupDir, backupName)
        encrypt(dbFile, outputFile, stringToKey(key))
    }

    fun restoreFromBackup(fileName: String, key: String): Boolean {
        val backupFile = File(
            Environment.getExternalStorageDirectory(),
            "$BACKUP_DIR/$fileName"
        )
        val backupDir = File(Environment.getExternalStorageDirectory(), BACKUP_DIR)
        val tempFile = File(backupDir, "temp.db")

        try {
            decrypt(backupFile, tempFile, stringToKey(key))
        } catch (e: Exception) {
            return false
        }

        EntryDatabase.overwriteDatabase(context, tempFile)
        return true
    }

    fun generateKey(): String {
        val key = KeyGenerator.getInstance(CIPHER_ALGORITHM).generateKey()
        return keyToString(key)
    }

    private fun keyToString(key: SecretKey): String = Base64.getEncoder().encodeToString(key.encoded)

    private fun stringToKey(s: String): SecretKey {
        val decodedKey = Base64.getDecoder().decode(s)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, CIPHER_ALGORITHM)
    }

    // Encrypts byte array to file
    private fun encrypt(inputFile: File, outputFile: File, key: SecretKey) {
        val content = Files.readAllBytes(inputFile.toPath())
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv

        FileOutputStream(outputFile).use { fileOut ->
            CipherOutputStream(fileOut, cipher).use { cipherOut ->
                fileOut.write(iv)
                cipherOut.write(content)
            }
        }
    }


    private fun decrypt(inputFile: File, outputFile: File, key: SecretKey) {
        FileInputStream(inputFile).use { fileIn ->
            val fileIv = ByteArray(16)
            fileIn.read(fileIv)
            cipher.init(
                Cipher.DECRYPT_MODE,
                key,
                IvParameterSpec(fileIv)
            )
            val cipherIn = CipherInputStream(fileIn, cipher)
            Files.copy(cipherIn, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }
}