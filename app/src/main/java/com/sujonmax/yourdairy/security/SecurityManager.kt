package com.sujonmax.yourdairy.security

import android.content.Context
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class SecurityManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val random = SecureRandom()

    val isConfigured: Boolean get() = prefs.getBoolean(KEY_CONFIGURED, false)
    val biometricEnabled: Boolean get() = prefs.getBoolean(KEY_BIOMETRIC, false)

    fun configurePin(pin: String): String {
        require(pin.length == 4 && pin.all(Char::isDigit))
        val salt = ByteArray(SALT_SIZE).also(random::nextBytes)
        val recoveryCode = generateRecoveryCode()
        val recoverySalt = ByteArray(SALT_SIZE).also(random::nextBytes)
        prefs.edit()
            .putString(KEY_PIN_SALT, encode(salt))
            .putString(KEY_PIN_HASH, encode(hash(pin, salt)))
            .putString(KEY_RECOVERY_SALT, encode(recoverySalt))
            .putString(KEY_RECOVERY_HASH, encode(hash(recoveryCode, recoverySalt)))
            .putBoolean(KEY_CONFIGURED, true)
            .putBoolean(KEY_BIOMETRIC, true)
            .apply()
        return recoveryCode
    }

    fun verifyPin(pin: String): Boolean = verify(pin, KEY_PIN_SALT, KEY_PIN_HASH)

    fun changePin(currentPin: String, newPin: String): Boolean {
        if (!verifyPin(currentPin)) return false
        require(newPin.length == 4 && newPin.all(Char::isDigit))
        val salt = ByteArray(SALT_SIZE).also(random::nextBytes)
        prefs.edit().putString(KEY_PIN_SALT, encode(salt)).putString(KEY_PIN_HASH, encode(hash(newPin, salt))).apply()
        return true
    }

    fun resetPinWithRecovery(recoveryCode: String, newPin: String): Boolean {
        if (!verify(recoveryCode, KEY_RECOVERY_SALT, KEY_RECOVERY_HASH)) return false
        require(newPin.length == 4 && newPin.all(Char::isDigit))
        val salt = ByteArray(SALT_SIZE).also(random::nextBytes)
        prefs.edit().putString(KEY_PIN_SALT, encode(salt)).putString(KEY_PIN_HASH, encode(hash(newPin, salt))).apply()
        return true
    }

    fun setBiometricEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_BIOMETRIC, enabled).apply()

    private fun verify(value: String, saltKey: String, hashKey: String): Boolean {
        val salt = prefs.getString(saltKey, null)?.let(::decode) ?: return false
        val expected = prefs.getString(hashKey, null)?.let(::decode) ?: return false
        return java.security.MessageDigest.isEqual(hash(value, salt), expected)
    }

    private fun hash(value: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(value.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded }
        finally { spec.clearPassword() }
    }

    private fun generateRecoveryCode(): String = buildString { repeat(12) { append(random.nextInt(10)) } }
    private fun encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
    private fun decode(value: String): ByteArray = Base64.decode(value, Base64.NO_WRAP)

    companion object {
        private const val PREFS = "dream_diary_security"
        private const val KEY_CONFIGURED = "configured"
        private const val KEY_BIOMETRIC = "biometric_enabled"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_RECOVERY_SALT = "recovery_salt"
        private const val KEY_RECOVERY_HASH = "recovery_hash"
        private const val SALT_SIZE = 16
        private const val ITERATIONS = 120_000
        private const val KEY_LENGTH_BITS = 256
    }
}
