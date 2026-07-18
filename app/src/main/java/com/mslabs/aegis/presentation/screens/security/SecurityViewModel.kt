package com.mslabs.aegis.presentation.screens.security

import androidx.lifecycle.ViewModel
import com.mslabs.aegis.security.PasswordAuditor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val passwordAuditor: PasswordAuditor,
) : ViewModel() {
    fun score(password: String): Int = passwordAuditor.score(password)
}
