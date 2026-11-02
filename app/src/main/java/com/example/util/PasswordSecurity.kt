package com.example.util

import java.security.MessageDigest
import java.security.SecureRandom

object PasswordSecurity {

    private val secureRandom = SecureRandom()

    fun generateSalt(): String {
        val bytes = ByteArray(16)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun generateResetCode(): String {
        val code = 100000 + secureRandom.nextInt(900000)
        return code.toString()
    }

    fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        var current = (password + salt).toByteArray(Charsets.UTF_8)
        // Repeat hash rounds to increase work factor
        repeat(1000) {
            md.reset()
            current = md.digest(current)
        }
        return current.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val calculated = hashPassword(password, salt)
        return calculated.equals(expectedHash, ignoreCase = true)
    }
}
