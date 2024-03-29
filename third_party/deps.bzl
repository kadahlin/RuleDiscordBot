DAGGER_BASE = "com.google.dagger:dagger"
DAGGER_VERSION = "2.40.5"

COROUTINES_BASE = "org.jetbrains.kotlinx:kotlinx-coroutines"
COROUTINES_VERSION = "1.6.1"

DAGGER_COMPILER = "%s-compiler:%s" % (DAGGER_BASE, DAGGER_VERSION)

_COMPOSE_VERSION = "1.4.0"

ARROW_VERSION = "1.0.1"
ARROW_BASE = "io.arrow-kt:arrow"

KTOR_BASE = "io.ktor:ktor"
KTOR_SERVER_BASE = "%s-server" % KTOR_BASE
KTOR_CLIENT_BASE = "%s-client" % KTOR_BASE
KTOR_VERSION = "1.6.7"

KTOR_TEST = "%s-test-host" % KTOR_SERVER_BASE

COMMON = [
    "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0",
    "com.discord4j:discord4j-core:3.2.0",
    "%s-core:%s" % (COROUTINES_BASE, COROUTINES_VERSION),
    "%s-reactor:%s" % (COROUTINES_BASE, COROUTINES_VERSION),
    "%s:%s" % (DAGGER_BASE, DAGGER_VERSION),
    DAGGER_COMPILER,
    "%s-core-jvm:%s" % (ARROW_BASE, ARROW_VERSION),
    "%s-core:%s" % (KTOR_CLIENT_BASE, KTOR_VERSION),
    "%s-json:%s" % (KTOR_CLIENT_BASE, KTOR_VERSION),
    "%s-json-jvm:%s" % (KTOR_CLIENT_BASE, KTOR_VERSION),
    "%s-apache:%s" % (KTOR_CLIENT_BASE, KTOR_VERSION),
    "%s-serialization-jvm:%s" % (KTOR_CLIENT_BASE, KTOR_VERSION),
    "%s-auth-jvm:%s" % (KTOR_CLIENT_BASE, KTOR_VERSION),
    "%s-serialization:%s" % (KTOR_BASE, KTOR_VERSION),
    "%s-network-tls-certificates:%s" % (KTOR_BASE, KTOR_VERSION),
    "javax.inject:javax.inject:1",
    "it.skrape:skrapeit:1.2.0"
]

KTOR_SERVER = [
    "%s-core:%s" % (KTOR_SERVER_BASE, KTOR_VERSION),
    "%s-netty:%s" % (KTOR_SERVER_BASE, KTOR_VERSION)
]

FIREBASE = [
     "com.google.firebase:firebase-admin:9.0.0"
]

TEST = [
    "io.strikt:strikt-core:0.32.0",
    "io.mockk:mockk:1.12.0",
    "org.junit.jupiter:junit-jupiter:5.8.2",
    "org.junit.jupiter:junit-jupiter-engine:5.8.2",
    "org.junit.platform:junit-platform-console:1.8.2",
    "org.junit.jupiter:junit-jupiter-api:5.8.2",
    "%s:%s" % (KTOR_TEST, KTOR_VERSION)
]

COMPOSE = [
    "androidx.core:core-ktx:1.8.0",
    "androidx.appcompat:appcompat:1.5.0",
    "androidx.activity:activity-compose:1.5.1",
    "androidx.compose.material:material:{}".format(_COMPOSE_VERSION),
    "androidx.compose.ui:ui:{}".format(_COMPOSE_VERSION),
    "androidx.compose.ui:ui-tooling:{}".format(_COMPOSE_VERSION),
    "androidx.compose.compiler:compiler:{}".format("1.2.0"),
    "androidx.compose.runtime:runtime:{}".format(_COMPOSE_VERSION)
]