plugins {
  id("java")
}

group = "com.dulno"
version = "1.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11

repositories {
  mavenCentral()
  maven {
    url = uri("https://git.dulno.com/api/v4/projects/26/packages/maven")
    credentials(HttpHeaderCredentials::class) {
      name = "Private-Token"
      value = System.getenv("DULNO_GITLAB_PRIVATE_TOKEN") ?:
        findProperty("dulnoGitlabPrivateToken") as String?
    }
    authentication {
      create("header", HttpHeaderAuthentication::class)
    }
  }
}

dependencies {
  testImplementation(platform("org.junit:junit-bom:5.11.3"))
  testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")

  implementation("com.dulno:server-service:1.0.0-SNAPSHOT")

  implementation("com.google.inject:guice:7.0.0")

  implementation("com.google.guava:guava:33.3.1-jre")

  implementation("org.projectlombok:lombok:1.18.36")
  annotationProcessor("org.projectlombok:lombok:1.18.36")
  testImplementation("org.projectlombok:lombok:1.18.36")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.36")

  implementation("org.json:json:20240303")
  implementation("commons-io:commons-io:2.18.0")
}

tasks.test {
  useJUnitPlatform()
}

tasks.jar {
  manifest.attributes["Main-Class"] = "com.dulno.server.cli.ServerCLIApplication"
  val dependencies = configurations
    .runtimeClasspath
    .get()
    .map(::zipTree)
  from(dependencies)
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}