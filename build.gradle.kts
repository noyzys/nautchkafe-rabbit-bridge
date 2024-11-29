plugins {
    java
}

group = "dev.nautchkafe.rabbit.bridge"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.vavr:vavr:0.10.4")

    implementation("com.rabbitmq:amqp-client:5.16.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")

}

tasks.test {
    useJUnitPlatform() 
}
