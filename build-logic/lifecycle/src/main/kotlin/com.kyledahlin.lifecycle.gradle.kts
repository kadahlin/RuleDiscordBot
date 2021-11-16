val taskGroup = "Rulebot build"
tasks.named<TaskReportTask>("tasks") {
    displayGroup = taskGroup
}

tasks.register("assembleAll") {
    group = taskGroup
    description = "Compile all code of the ${project.name} component"
    dependsOn(subprojects.map { ":${it.name}:assemble" })
}
tasks.register("checkAll") {
    group = taskGroup
    description = "Compile all code and run all tests of the ${project.name} component"
    dependsOn(subprojects.map { ":${it.name}:check" })
}
tasks.register("cleanAll") {
    group = taskGroup
    description = "Clean build folder"
    dependsOn(subprojects.map { ":${it.name}:clean" })
}