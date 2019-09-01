package com.johnturkson.password_manager

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

const val EXTENSION_TYPE = "txt"

var saveFolderLocation: Path = Paths.get(System.getProperty("user.home"))
    .resolve("password-manager")

lateinit var loggedInUser: User
    private set

fun main() {
    val approvedUsersLocation = saveFolderLocation
        .resolve("approvedUsers.$EXTENSION_TYPE")
    
    if (Files.notExists(approvedUsersLocation)) {
        Files.createDirectories(approvedUsersLocation.parent)
        Files.createFile(approvedUsersLocation)
    }
    
    val approvedUsers = loadApprovedUsers(approvedUsersLocation)
    
    if (System.console() == null) {
        warnOfUnsafeConsole()
    }
    
    val approvedCredentials = handleInitialLogin(approvedUsers)
    val userProfileLocation = saveFolderLocation
        .resolve("${approvedCredentials.username}.$EXTENSION_TYPE")
    
    if (Files.notExists(userProfileLocation)) {
        println("Existing user profile not found. A new one has been created.")
        Files.createDirectories(userProfileLocation).parent
        Files.createFile(userProfileLocation)
    }
    
    loggedInUser = User(approvedCredentials.username, User.load(userProfileLocation))
    
    println("Enter a command.")
    while (true) {
        val input = readLine()!!.toUpperCase().trim().replace(" ", "_")
        try {
            Commands.valueOf(input).action.invoke()
        } catch (e: IllegalArgumentException) {
            println("Unknown command.")
            continue
        }
        println("Enter a command.")
    }
}

fun loadApprovedUsers(location: Path): Collection<LoginCredentials> {
    val approvedUsers: MutableSet<LoginCredentials> = mutableSetOf()
    
    Files.readAllLines(location)
        .map { it.split(", ") }
        .map { LoginCredentials(it[0], it[1]) }
        .forEach { approvedUsers.add(it) }
    
    return approvedUsers
}

class User(val name: String, val entries: MutableSet<Entry> = sortedSetOf()) {
    companion object {
        fun save(user: User, location: Path) {
            Files.writeString(
                location,
                user.entries.joinToString(separator = "\n") { "${it.service}, ${it.username}, ${it.password}" }
            )
        }
        
        fun load(location: Path): MutableSet<Entry> {
            return Files.readAllLines(location)
                .map { it.split(", ") }
                .map { Entry(it[0], it[1], it[2]) }
                .toMutableSet()
        }
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as User
        
        if (name != other.name) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        return name.hashCode()
    }
    
    override fun toString(): String {
        return name
    }
    
}

class Entry(val service: String, val username: String, val password: String) :
    Comparable<Entry> {
    override fun compareTo(other: Entry): Int {
        return compareValuesBy(this, other, { it.service }, { it.username })
    }
    
    override fun toString(): String {
        return "$service - $username"
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as Entry
        
        if (service != other.service) return false
        if (username != other.username) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = service.hashCode()
        result = 31 * result + username.hashCode()
        return result
    }
}
