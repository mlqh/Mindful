package com.ece452s24g7.mindful.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.ece452s24g7.mindful.R
import java.util.concurrent.Executor

class LockScreenActivity : AppCompatActivity(){
    private var enteredPin = ""
    private var pin = ""
    private var usePinLock = false
    private var useBioLock = false
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var bioLockLayout: ConstraintLayout
    private lateinit var pinLockLayout: ConstraintLayout
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.lock_screen)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        pin = sharedPreferences.getString("pin_lock", "").toString()

        usePinLock = sharedPreferences.getString("lock_type", "").toString() == "pin_custom"
        useBioLock = sharedPreferences.getString("lock_type", "").toString() == "pin_biometrics"

        bioLockLayout = findViewById(R.id.bioLock)
        pinLockLayout = findViewById(R.id.pinLock)

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    finish()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    if (usePinLock && pin.isNotBlank()) {
                        showPinLock()
                    } else {
                        forwardToMainActivity()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    finish()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle("Log in to Mindful")
            .setConfirmationRequired(false)
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()

        if (useBioLock) {
            bioLockLayout.isVisible = true
            biometricPrompt.authenticate(promptInfo)
        } else if (usePinLock && pin.isNotBlank()) {
            showPinLock()
        } else {
            forwardToMainActivity()
        }
    }

    private fun showPinLock() {
        pinLockLayout.isVisible = true

        // Pin lock logic
        val buttonList = mutableListOf<Button>()
        buttonList.add(findViewById(R.id.pin_button_0))
        buttonList.add(findViewById(R.id.pin_button_1))
        buttonList.add(findViewById(R.id.pin_button_2))
        buttonList.add(findViewById(R.id.pin_button_3))
        buttonList.add(findViewById(R.id.pin_button_4))
        buttonList.add(findViewById(R.id.pin_button_5))
        buttonList.add(findViewById(R.id.pin_button_6))
        buttonList.add(findViewById(R.id.pin_button_7))
        buttonList.add(findViewById(R.id.pin_button_8))
        buttonList.add(findViewById(R.id.pin_button_9))

        for (i in 0..9) {
            buttonList[i].setOnClickListener {
                val promptTextView = findViewById<TextView>(R.id.pinPrompt)
                val shakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake)

                enteredPin += i.toString()
                if (enteredPin == pin) {
                    forwardToMainActivity()
                } else if (enteredPin.length == 4) {
                    enteredPin = ""
                    promptTextView.text = getString(R.string.incorrect_pin)
                    promptTextView.animation = shakeAnim
                }
                updatePinTextView()
            }
        }
    }

    private fun forwardToMainActivity() {
        val i = Intent(this, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    private fun updatePinTextView() {
        val pinTextView = findViewById<TextView>(R.id.pinText)
        var pinStr = ""
        for (j in enteredPin.indices) {
            pinStr += if (j == 0) "*" else " *"
        }
        pinTextView.text = pinStr
    }
}