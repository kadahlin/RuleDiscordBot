plugins {
    id("com.kyledahlin.lifecycle")
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}