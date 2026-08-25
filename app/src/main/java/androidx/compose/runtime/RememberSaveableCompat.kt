package androidx.compose.runtime

import androidx.compose.runtime.saveable.rememberSaveable as runtimeRememberSaveable

@Composable
inline fun <T> rememberSaveable(vararg inputs: Any?, crossinline calculation: () -> T): T =
    runtimeRememberSaveable(*inputs) { calculation() }
