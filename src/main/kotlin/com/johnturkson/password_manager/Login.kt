package com.johnturkson.password_manager

import java.io.Console
import kotlin.system.exitProcess

fun handleInitialLogin(
    approvedUsers: Collection<LoginCredentials>,
    console: Console? = System.console(),
    prompt: String = "",
    maxAttempts: Int = 5
): LoginCredentials {
    require(maxAttempts >= 0) { "Max attempts must be greater than 0" }
    
    if (prompt.isNotBlank()) {
        println(prompt)
    }
    
    for (i in 1..maxAttempts) {
        val loginUsername = getLoginUsername(console)
        val loginPassword = getLoginPassword(console)
        val credentials = LoginCredentials(loginUsername, loginPassword)
        if (credentials in approvedUsers) {
            return credentials
        }
        when (maxAttempts - i) {
            0 -> println("Maximum incorrect attempts reached. Program will now exit.")
            1 -> println("Invalid login attempt. 1 attempt remaining.")
            else -> println("Invalid login attempt. ${maxAttempts - i} attempts remaining.")
        }
    }
    
    exitProcess(0)
}

fun getLoginUsername(console: Console? = System.console(), prompt: String = "Username: "): String {
    return getHiddenConsoleInputIfPossible(console, prompt)
}

fun getLoginPassword(console: Console? = System.console(), prompt: String = "Password: "): String {
    return getHiddenConsoleInputIfPossible(console, prompt)
}

data class LoginCredentials(val username: String, val password: String)
