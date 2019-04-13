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

package bot

import discord4j.core.`object`.util.Snowflake
import java.io.File

private const val ADMIN_FILE = "adminSnowflakes"
private const val ROLE_SUFFIX = "-r"

//TODO: move these all to a database


internal fun getAdminSnowflakes(): Collection<RoleSnowflake> {
    val file = File(ADMIN_FILE)
    file.createNewFile()
    val admins = file.readLines().map { it.trim() }.map {
        if (it.endsWith(ROLE_SUFFIX)) {
            val snowflake = Snowflake.of(it.dropLast(2))
            RoleSnowflake(snowflake, isRole = true)
        } else {
            RoleSnowflake(Snowflake.of(it))
        }
    }

    return admins
}

internal fun addAdminSnowflakes(snowflakes: Collection<RoleSnowflake>) {
    val adminFile = File(ADMIN_FILE)
    val admins = getAdminSnowflakes()
    admins.toMutableSet().addAll(snowflakes)
    val fileText = admins.map {
        "${it.snowflake.asString()}${if (it.isRole) ROLE_SUFFIX else ""}"
    }
    adminFile.writeText(fileText.joinToString(separator = "\n"))
}

internal fun removeAdminSnowflakes(snowflakes: Collection<Snowflake>) {
    val file = File(ADMIN_FILE)
    var fileLines = file.readLines()
    fileLines = fileLines.filterNot { line ->
        snowflakes.any {
            line.startsWith(it.asString())
        }
    }
    file.writeText(fileLines.joinToString(separator = "\n"))
}

