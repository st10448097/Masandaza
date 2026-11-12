package com.example.masandaza.utils

object ValidationUtils {

    sealed class EmailValidationResult {
        object Valid : EmailValidationResult()
        data class Invalid(val reason: String) : EmailValidationResult()
    }

    /**
     * Normalizes email input: trims leading/trailing whitespace and converts to lowercase.
     */
    fun normalizeEmail(emailInput: String?): String {
        return emailInput?.trim()?.lowercase() ?: ""
    }

    /**
     * Validates an email address against strict security rules:
     * 1. Normalize: trim whitespace, convert to lowercase
     * 2. Reject empty, null, or blank input
     * 3. Total length: min 6 characters, max 254 characters (RFC 5321)
     * 4. Must contain exactly ONE @ symbol
     * 5. Local part (before @):
     *    - Min 3 characters, Max 64 characters
     *    - Must NOT start or end with a dot '.'
     *    - Must NOT contain consecutive dots '..'
     *    - Allowed characters only: a-z, 0-9, . _ % + -
     *    - Reject: spaces, newlines, < > ( ) [ ] , ; :
     * 6. Domain part (after @):
     *    - Must contain valid domain name and TLD of at least 2 characters
     *    - Must NOT start or end with '.' or '-'
     *    - Must NOT contain consecutive dots '..'
     */
    fun validateEmailDetailed(emailInput: String?): EmailValidationResult {
        // 1 & 2: Reject null, empty, or blank input
        if (emailInput.isNullOrBlank()) {
            return EmailValidationResult.Invalid("Email address cannot be empty")
        }

        // 1. Normalize: trim whitespace, convert to lowercase
        val normalized = normalizeEmail(emailInput)

        // 3. Total length check: min 6, max 254
        if (normalized.length < 6) {
            return EmailValidationResult.Invalid("Email must be at least 6 characters long")
        }
        if (normalized.length > 254) {
            return EmailValidationResult.Invalid("Email exceeds maximum length of 254 characters")
        }

        // 4. Must contain exactly ONE @ symbol
        val atCount = normalized.count { it == '@' }
        if (atCount != 1) {
            return EmailValidationResult.Invalid("Email must contain exactly one '@' symbol")
        }

        val atIndex = normalized.indexOf('@')
        val localPart = normalized.substring(0, atIndex)
        val domainPart = normalized.substring(atIndex + 1)

        // 5. Local part (before @): Min 3 chars, Max 64 chars
        if (localPart.length < 3) {
            return EmailValidationResult.Invalid("Username before '@' must be at least 3 characters")
        }
        if (localPart.length > 64) {
            return EmailValidationResult.Invalid("Username before '@' cannot exceed 64 characters")
        }

        // Must NOT start or end with a dot .
        if (localPart.startsWith(".") || localPart.endsWith(".")) {
            return EmailValidationResult.Invalid("Username before '@' cannot start or end with a dot '.'")
        }

        // Must NOT contain consecutive dots ..
        if (localPart.contains("..")) {
            return EmailValidationResult.Invalid("Username cannot contain consecutive dots '..'")
        }

        // Reject: spaces, newlines, < > ( ) [ ] , ; :
        val forbiddenChars = charArrayOf(' ', '\n', '\r', '\t', '<', '>', '(', ')', '[', ']', ',', ';', ':')
        for (c in forbiddenChars) {
            if (localPart.contains(c) || domainPart.contains(c)) {
                return EmailValidationResult.Invalid("Email contains forbidden character '${if (c == ' ') "space" else c}'")
            }
        }

        // Allowed characters only: a-z, 0-9, . _ % + -
        for (i in 0 until localPart.length) {
            val ch = localPart[i]
            val isAllowed = (ch in 'a'..'z') || (ch in '0'..'9') || ch == '.' || ch == '_' || ch == '%' || ch == '+' || ch == '-'
            if (!isAllowed) {
                return EmailValidationResult.Invalid("Username contains invalid character '$ch'")
            }
        }

        // Domain part checks (after @)
        if (domainPart.isBlank() || !domainPart.contains(".")) {
            return EmailValidationResult.Invalid("Email domain must be valid (e.g. domain.com)")
        }

        if (domainPart.startsWith(".") || domainPart.endsWith(".") || domainPart.startsWith("-") || domainPart.endsWith("-")) {
            return EmailValidationResult.Invalid("Domain cannot start or end with a dot or hyphen")
        }

        if (domainPart.contains("..")) {
            return EmailValidationResult.Invalid("Domain cannot contain consecutive dots '..'")
        }

        val lastDotIndex = domainPart.lastIndexOf('.')
        val tld = domainPart.substring(lastDotIndex + 1)
        if (tld.length < 2) {
            return EmailValidationResult.Invalid("Domain TLD (extension) must be at least 2 characters")
        }

        for (i in 0 until domainPart.length) {
            val ch = domainPart[i]
            val isAllowed = (ch in 'a'..'z') || (ch in '0'..'9') || ch == '.' || ch == '-'
            if (!isAllowed) {
                return EmailValidationResult.Invalid("Domain contains invalid character '$ch'")
            }
        }

        return EmailValidationResult.Valid
    }

    /**
     * Convenient boolean check using strict email validation rules.
     */
    fun isValidEmail(emailInput: String?): Boolean {
        return validateEmailDetailed(emailInput) is EmailValidationResult.Valid
    }
}
