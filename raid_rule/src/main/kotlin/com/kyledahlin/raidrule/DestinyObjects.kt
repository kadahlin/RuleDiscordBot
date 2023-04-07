package com.kyledahlin.raidrule

import com.kyledahlin.models.Username

interface DestinyActivity {
    val id: String
    val name: String
}

interface DestinyRoleActivity : DestinyActivity {
    fun assignRoles(users: List<Username>): String
}

interface DestinyEncounterActivity : DestinyActivity {
    val encounters: List<Encounter>
}

data class Raid(override val name: String, override val id: String, override val encounters: List<Encounter>) :
    DestinyEncounterActivity

data class Encounter(
    override val name: String,
    override val id: String,
    val roles: List<EncounterRole>
) : DestinyRoleActivity {
    override fun assignRoles(users: List<Username>): String {
        val roleString = users.createRoleAssignmentString(name, roles)
        roles.forEach { it.users.clear() }
        return roleString
    }
}

data class EncounterRole(
    val name: String, val size: Int, val isRequired: Boolean, var users: MutableList<Username> = mutableListOf()
)

data class ActivityType(
    val name: String, val id: String, val activities: List<DestinyActivity>
)