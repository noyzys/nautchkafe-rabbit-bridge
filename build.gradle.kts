plugins {
    java
}

group = "dev.nautchkafe.rabbit.bridge"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.vavr:vavr:0.11.0")

    implementation("com.rabbitmq:amqp-client:5.28.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:6.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:6.0.0")

}

tasks.test {
    useJUnitPlatform() 
}
