// This file is ONLY used to define global lifecycle tasks
val taskGroup = "Rulebot build"
tasks.named<TaskReportTask>("tasks") {
    displayGroup = taskGroup
}

val sourceFolders = listOf("discord4k", "rulebot", "myrulebot")

val checkAll = tasks.register("checkAll") {
    group = taskGroup
    description = "Run all tests"
    dependsOn(gradle.includedBuilds.filter { sourceFolders.contains(it.name) }.map { it.task(":checkAll") })
}
val assembleMyRulebot = tasks.register("assembleMyRulebot") {
    group = taskGroup
    description = "Assemble my rule bot"
    dependsOn(gradle.includedBuild("myrulebot").task(":myrulebot:assemble"))
}

val packageMyRuleBot = tasks.register("packageMyRulebot") {
    group = taskGroup
    description = "Create the rulebot distribution for docker"
    dependsOn(assembleMyRulebot, gradle.includedBuild("myrulebot").task(":myrulebot:installDist"))
}

tasks.register("build") {
    group = taskGroup
    description = "Run all tests and package the bot"
    dependsOn(checkAll, packageMyRulebot)
}

tasks.register("cleanAll") {
    group = taskGroup
    description = "Project wide clean"
    dependsOn(gradle.includedBuilds.map { it.task(":cleanAll") })
}