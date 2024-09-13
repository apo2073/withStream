plugins {
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "kr.apo2073"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://jitpack.io") {
        name = "jitpack"
    }
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}


dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation(files("libs/chzzk4j-0.0.9.jar"))
    implementation(files("libs/DonationAlertAPI-1.1.0.jar"))
    //implementation(files("libs/aLib-1.0.2.jar"))

    implementation("com.github.apo2073:ApoLib:1.0.2")

    implementation("org.slf4j:slf4j-api:2.0.7")


    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    implementation("me.clip:placeholderapi:2.11.6")

    implementation("io.socket:socket.io-client:2.0.1")
    implementation("io.reactivex.rxjava2:rxjava:2.1.16")
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("org.java-websocket:Java-WebSocket:1.5.2")

    implementation("com.google.api-client:google-api-client:1.33.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.23.0")
    implementation("com.google.apis:google-api-services-youtube:v3-rev20230816-2.0.0")
    implementation("com.google.http-client:google-http-client-jackson2:1.39.2")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

}

val targetJavaVersion = 17
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    archiveClassifier.set("all")
    mergeServiceFiles()
    dependencies {
        include(dependency(files("libs/chzzk4j-0.0.9.jar")))
    }
}
