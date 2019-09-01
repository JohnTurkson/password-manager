package com.johnturkson.password_manager

import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import kotlin.system.exitProcess

fun createEntry(): Entry {
    val account = getVisibleConsoleInput(prompt = "Account: ")
    val username = getVisibleConsoleInput(prompt = "Username: ")
    val password = getHiddenConsoleInputIfPossible(prompt = "Password: ")
    return Entry(account, username, password)
}

fun generateRandomPassword(length: Int, vararg parameters: RandomPasswordCharset): String {
    require(length > 0) { "Length must be greater than 0." }
    
    val pool = mutableSetOf<Char>()
    if (parameters.isEmpty()) {
        val default = listOf(
            RandomPasswordCharset.LOWERCASE_LETTERS,
            RandomPasswordCharset.UPPERCASE_LETTERS,
            RandomPasswordCharset.NUMBERS
        )
        for (parameter in default) {
            parameter.characters.forEach { pool.add(it) }
        }
    } else {
        for (parameter in parameters) {
            parameter.characters.forEach { pool.add(it) }
        }
    }
    
    var password = ""
    for (i in 0..length) {
        password += pool.random()
    }
    
    return password
}

fun copyToClipboard(text: String) {
    val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
    clipboard.setContents(StringSelection(text), null)
}

fun save() {
    User.save(loggedInUser, saveFolderLocation.resolve("${loggedInUser.name}.$EXTENSION_TYPE"))
    println("All changes saved.")
}

fun load() {
    val previous = User.load(saveFolderLocation.resolve("${loggedInUser.name}.$EXTENSION_TYPE"))
    loggedInUser.entries.clear()
    loggedInUser.entries.addAll(previous)
    println("Previous entries loaded.")
}

fun exit(status: Int = 0, reason: String = "") {
    if (reason.isNotBlank()) {
        println(reason)
    }
    save()
    exitProcess(status)
}

enum class Commands(val action: () -> Unit) {
    VIEW(::handleViewingAllEntries),
    SEARCH(::handleSearchingForEntries),
    NEW(::handleNewEntry),
    UPDATE(::handleUpdatedEntry),
    GENERATE(::handleGeneratedPassword),
    SAVE(::handleSaving),
    LOAD(::handleLoading),
    EXIT(::handleExit)
}

fun handleViewingAllEntries() {
    loggedInUser.entries.forEach { println(it) }
}

fun handleSearchingForEntries() {
    val search = getVisibleConsoleInput("Search: ")
    loggedInUser.entries
        .filter {
            it.service.contains(search, true) || it.username.contains(search, true)
        }
        .sortedWith(Comparator { e1, e2 ->
            when {
                (e1.service.startsWith(search) || e1.username.startsWith(search)) &&
                        (e2.service.startsWith(search) || e2.username.startsWith(search)) -> 0
                e1.service.startsWith(search) || e1.username.startsWith(search) -> 1
                e2.service.startsWith(search) || e2.username.startsWith(search) -> -1
                else -> 0
            }
        })
        .forEach { println(it) }
}

fun handleNewEntry() {
    val entry = createEntry()
    if (loggedInUser.entries.contains(entry)) {
        val prompt = "An existing entry the same name already exists. Update anyway?"
        val response = getVisibleConsoleInput(prompt)
        when {
            response.toLowerCase().matches(Regex("y|yes")) -> {
                loggedInUser.entries.add(entry)
                println("Added ${entry.service} to ${loggedInUser.name}")
            }
            else -> {
                println("No changes were made.")
            }
        }
    } else {
        loggedInUser.entries.add(entry)
    }
}

fun handleUpdatedEntry() {
    val entry = createEntry()
    when {
        loggedInUser.entries.contains(entry) -> {
            loggedInUser.entries.add(entry)
            println("Updated ${entry.service} in ${loggedInUser.name}")
        }
        else -> {
            println("The entry to update does not exist.")
            println("No changes were made.")
        }
    }
}

fun handleGeneratedPassword() {
    val length: Int
    while (true) {
        val input = getVisibleConsoleInput("Length: ")
        if (input.matches(Regex("[1-9]\\d*"))) {
            length = Integer.parseInt(input)
            break
        }
        println("Invalid length.")
    }
    copyToClipboard(generateRandomPassword(length))
}

fun handleSaving() {
    save()
}

fun handleLoading() {
    load()
}

fun handleExit() {
    exit()
}

enum class RandomPasswordCharset(val characters: CharRange) {
    LOWERCASE_LETTERS(CharRange('a', 'z')),
    UPPERCASE_LETTERS(CharRange('A', 'Z')),
    NUMBERS(CharRange('0', '9')),
    SYMBOLS(CharRange('!', '+'))
}
