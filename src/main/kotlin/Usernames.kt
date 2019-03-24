import java.io.BufferedReader
import java.io.InputStreamReader

//Username files are formatted so each line is <username>,<snowflake>

internal data class Username(val username: String, val id: Long)

internal val regularUsernames by lazy {
    getPairsFromResourceFile("usernames.txt")
}

internal val adminUsernames by lazy {
    getPairsFromResourceFile("adminusernames.txt")
}

internal val bot by lazy {
    getPairsFromResourceFile("botusername.txt").first()
}

private fun getPairsFromResourceFile(filename: String): List<Username> {
    val classloader = Thread.currentThread().contextClassLoader
    val inputStream = classloader.getResourceAsStream(filename)
    val reader = BufferedReader(InputStreamReader(inputStream))
    return reader.readLines().map { line ->
        val condensed = line.trim()
        val pieces = condensed.split(",")
        Username(pieces.first(), pieces[1].toLong())
    }
}

