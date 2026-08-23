package com.sujonmax.yourdairy.ui.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sujonmax.yourdairy.security.SecurityManager

@Composable
fun PinSetupScreen(security: SecurityManager, onConfigured: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var recovery by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Secure your diary", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Create a 4-digit PIN. A recovery code will be generated once.")
        Spacer(Modifier.height(20.dp))
        PinField("PIN", pin) { if (it.length <= 4) pin = it.filter(Char::isDigit) }
        Spacer(Modifier.height(12.dp))
        PinField("Confirm PIN", confirm) { if (it.length <= 4) confirm = it.filter(Char::isDigit) }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            error = when {
                pin.length != 4 -> "PIN must contain 4 digits."
                pin != confirm -> "PINs do not match."
                else -> null
            }
            if (error == null) recovery = security.configurePin(pin)
        }, modifier = Modifier.fillMaxWidth()) { Text("Create secure PIN") }
    }
    recovery?.let { code ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Save your recovery code") },
            text = { Text("$code\n\nStore this code somewhere safe. It is required if you forget your PIN.") },
            confirmButton = { TextButton(onClick = onConfigured) { Text("I saved it") } }
        )
    }
}

@Composable
fun PinLockScreen(security: SecurityManager, onUnlocked: () -> Unit, onBiometric: (() -> Unit)?, onRecovery: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Dream Diry is locked", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        PinField("Enter PIN", pin) { if (it.length <= 4) pin = it.filter(Char::isDigit) }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            if (security.verifyPin(pin)) onUnlocked() else { error = "Incorrect PIN."; pin = "" }
        }, modifier = Modifier.fillMaxWidth()) { Text("Unlock") }
        if (onBiometric != null) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onBiometric, modifier = Modifier.fillMaxWidth()) { Text("Use biometric") }
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onRecovery, modifier = Modifier.fillMaxWidth()) { Text("Forgot PIN?") }
    }
}

@Composable
fun RecoveryScreen(security: SecurityManager, onRecovered: () -> Unit, onCancel: () -> Unit) {
    var code by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Recover your diary", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = code,
            onValueChange = { if (it.length <= 12) code = it.filter(Char::isDigit) },
            label = { Text("12-digit recovery code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(12.dp))
        PinField("New PIN", newPin) { if (it.length <= 4) newPin = it.filter(Char::isDigit) }
        Spacer(Modifier.height(12.dp))
        PinField("Confirm new PIN", confirm) { if (it.length <= 4) confirm = it.filter(Char::isDigit) }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            error = when {
                code.length != 12 -> "Enter the complete recovery code."
                newPin.length != 4 -> "New PIN must contain 4 digits."
                newPin != confirm -> "PINs do not match."
                !security.resetPinWithRecovery(code, newPin) -> "Invalid recovery code."
                else -> null
            }
            if (error == null) onRecovered()
        }, modifier = Modifier.fillMaxWidth()) { Text("Reset PIN") }
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
    }
}

@Composable
private fun PinField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}
