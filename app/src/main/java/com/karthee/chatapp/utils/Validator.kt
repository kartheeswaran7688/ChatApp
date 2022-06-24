package com.karthee.chatapp.utils

import java.util.regex.Matcher
import java.util.regex.Pattern

object Validator {

    fun isValidEmail(email: String): Boolean {
        try {
            val pattern: Pattern
            val matcher: Matcher
            val EMAIL_PATTERN =
                "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
            pattern = Pattern.compile(EMAIL_PATTERN)
            matcher = pattern.matcher(email)
            return matcher.matches()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun isEmailEmpty(email: String?): Boolean{
        return email.isNullOrEmpty()
    }
}