plugins {
    java
}

group = "dev.nautchkafe.rabbit.bridge"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.vavr:vavr:0.10.6")

    implementation("com.rabbitmq:amqp-client:5.25.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.4")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.13.4")

}

tasks.test {
    useJUnitPlatform() 
}
