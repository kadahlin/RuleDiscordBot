package com.kyledahlin.raidrule

import com.kyledahlin.models.Username

/**
 * Bucket the given list of users into the given roles
 */
fun List<Username>.createRoleAssignmentString(name: String, roles: List<EncounterRole>): String {
    val q = ArrayDeque<EncounterRole>()
    val (required, optional) = roles.partition { it.isRequired }
    q.addAll(required)
    var optionalPending = true
    forEach {
        val role = q.removeFirst()
        role.users.add(it)
        if (!role.isRequired || (role.users.size != role.size)) {
            q.add(role)
        }
        if (q.isEmpty() && optionalPending) {
            optionalPending = false
            q.addAll(optional)
        }
    }
    return name + "\n\n" + roles.joinToString("\n") { role -> "${role.name}: ${role.users.joinToString(",") { it.value }}" }
}