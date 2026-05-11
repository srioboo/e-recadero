plugins {
	java
	id("org.springframework.boot") version "3.4.3"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.flywaydb.flyway") version "9.22.3"
	id("jacoco")
	id("com.github.spotbugs") version "6.0.9"
	id("checkstyle")
}

group = "org.sirantar"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
	maven { url = uri("https://repo.spring.io/snapshot") }
}

extra["springCloudVersion"] = "2023.0.0"
extra["springModulithVersion"] = "1.1.5"

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

dependencies {
	// Spring Boot Starters
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	
	// Spring Modulith
	implementation("org.springframework.modulith:spring-modulith-starter-core:${property("springModulithVersion")}")
	implementation("org.springframework.modulith:spring-modulith-events-core:${property("springModulithVersion")}")
	
	// Spring Cloud Stream & Kafka
	implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka")
	
	// Database & Migrations
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")
	
	// Redis
	runtimeOnly("io.lettuce:lettuce-core")
	
	// Mapping & Utilities
	implementation("org.projectlombok:lombok")
	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
	annotationProcessor("org.projectlombok:lombok")
	
	// API Documentation
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
	
	// JWT Token Generation & Validation
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
	
	// Development Tools
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")
	
	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.modulith:spring-modulith-starter-test:${property("springModulithVersion")}")
	testImplementation("org.junit.jupiter:junit-jupiter-api")
	testImplementation("org.junit.jupiter:junit-jupiter-engine")
	testImplementation("org.mockito:mockito-core:5.7.0")
	testImplementation("org.mockito:mockito-junit-jupiter:5.7.0")
	testImplementation("org.assertj:assertj-core:3.24.1")
	testImplementation("com.h2database:h2")
	testImplementation("org.testcontainers:testcontainers:1.19.7")
	testImplementation("org.testcontainers:postgresql:1.19.7")
	testImplementation("org.testcontainers:kafka:1.19.7")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Testing Configuration
tasks.withType<Test> {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

// Code Coverage with JaCoCo
tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		html.required.set(true)
		csv.required.set(false)
	}
}

tasks.register("codeCoverageReport") {
	dependsOn(tasks.jacocoTestReport)
}

// Quality Gate: All Checks
tasks.register("qualityGate") {
	dependsOn("check", "codeCoverageReport")
	description = "Run all quality checks: checkstyle, spotbugs, tests, coverage"
}

// Checkstyle Configuration
checkstyle {
	maxWarnings = 0
	toolVersion = "10.12.5"
	configFile = file("config/checkstyle.xml")
}

// SpotBugs Configuration
spotbugs {
	excludeFilter.set(file("config/spotbugs-exclude.xml"))
}

// Flyway Configuration
flyway {
	url = System.getenv("FLYWAY_URL") ?: "jdbc:postgresql://localhost:5432/recadero"
	user = System.getenv("FLYWAY_USER") ?: "recadero"
	password = System.getenv("FLYWAY_PASSWORD") ?: "recadero123"
	baselineOnMigrate = true
	cleanDisabled = true
	locations = arrayOf("filesystem:src/main/resources/db/migration")
	sqlMigrationPrefix = "V"
	sqlMigrationSeparator = "__"
	sqlMigrationSuffixes = arrayOf(".sql")
}
