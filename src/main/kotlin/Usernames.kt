/*
*Copyright 2019 Kyle Dahlin
*
*Licensed under the Apache License, Version 2.0 (the "License");
*you may not use this file except in compliance with the License.
*You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing, software
*distributed under the License is distributed on an "AS IS" BASIS,
*WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*See the License for the specific language governing permissions and
*limitations under the License.
*/
import java.io.BufferedReader
import java.io.InputStreamReader

//Username files are formatted so each line is <username>,<snowflake>

internal data class Username(val username: String, val snowflake: Long)

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

