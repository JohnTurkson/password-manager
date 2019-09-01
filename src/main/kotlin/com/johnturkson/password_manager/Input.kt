package com.johnturkson.password_manager

import java.io.Console


fun warnOfUnsafeConsole() {
    println("WARNING: THE CURRENT CONSOLE DOES NOT SUPPORT HIDDEN USERNAME/PASSWORD INPUT. ALL ENTERED USERNAMES AND PASSWORDS WILL BE VISIBLE ON SCREEN.")
    println("Press enter to continue.")
    readLine()
}

fun getVisibleConsoleInput(prompt: String = ""): String {
    print(prompt)
    return readLine()!!
}

fun getHiddenConsoleInput(console: Console, prompt: String = ""): String {
    return String(console.readPassword(prompt))
}

fun getHiddenConsoleInputIfPossible(
    console: Console? = System.console(),
    prompt: String = ""
): String {
    return if (console != null) {
        getHiddenConsoleInput(console, prompt)
    } else {
        getVisibleConsoleInput(prompt)
    }
}
