plugins {
  id("java")
}

group = "net.taskwolf"
version = "1.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11

repositories {
  mavenCentral()
  mavenLocal()
}

dependencies {
  testImplementation(platform("org.junit:junit-bom:6.1.3"))
  testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")

  implementation("net.taskwolf:server-service:1.0.0-SNAPSHOT")

  implementation("com.google.inject:guice:7.0.0")

  implementation("com.google.guava:guava:33.7.2-jre")

  implementation("org.projectlombok:lombok:1.18.48")
  annotationProcessor("org.projectlombok:lombok:1.18.48")
  testImplementation("org.projectlombok:lombok:1.18.48")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.48")

  implementation("org.json:json:20260814")
  implementation("commons-io:commons-io:2.22.0")
}

tasks.test {
  useJUnitPlatform()
}

tasks.jar {
  manifest.attributes["Main-Class"] = "net.taskwolf.server.cli.ServerCLIApplication"
  val dependencies = configurations
    .runtimeClasspath
    .get()
    .map(::zipTree)
  from(dependencies)
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}