package com.mslabs.aegis.services.autofill

class AutofillParser {
    fun looksLikePasswordField(hint: String?): Boolean {
        return hint?.contains("password", ignoreCase = true) == true
    }
}
