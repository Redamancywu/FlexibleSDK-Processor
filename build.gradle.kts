plugins {
    kotlin("jvm") version "1.9.20"
    `java-gradle-plugin`
    `maven-publish`
    // JitPack发布不需要Gradle Plugin Portal
    // id("com.gradle.plugin-publish") version "1.2.1"
}

group = "com.flexiblesdk"
version = "0.0.3"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
    withJavadocJar()
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    // Gradle API
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    
    // KSP API
    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.21-1.0.25")
    compileOnly("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.0.21-1.0.25")
    
    // Code generation
    implementation("com.squareup:kotlinpoet:1.14.2")
    implementation("com.squareup:kotlinpoet-ksp:1.14.2")
    
    // Utilities
    implementation("com.google.guava:guava:32.1.3-jre")
    
    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.mockito:mockito-core:5.6.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.20")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.20")
    testImplementation("com.google.truth:truth:1.1.5")
    testImplementation(gradleTestKit())
}

// JitPack发布不需要Gradle Plugin Portal配置
// gradlePlugin {
//     website.set("https://github.com/Redamancywu/FlexibleSDK-Processor")
//     vcsUrl.set("https://github.com/Redamancywu/FlexibleSDK-Processor")
//     plugins {
//         create("flexibleSdkProcessor") {
//             id = "com.flexiblesdk.processor"
//             implementationClass = "com.flexiblesdk.processor.FlexibleSDKProcessorPlugin"
//             displayName = "FlexibleSDK Processor"
//             description = "A Gradle plugin for processing FlexibleSDK annotations and generating code"
//             tags.set(listOf("kotlin", "annotation-processing", "code-generation"))
//         }
//     }
// }

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name.set("FlexibleSDK Processor")
                description.set("A flexible SDK processor for Kotlin annotation processing and code generation")
                url.set("https://github.com/Redamancywu/FlexibleSDK-Processor")
                
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                
                developers {
                    developer {
                        id.set("Redamancywu")
                        name.set("Redamancywu")
                        email.set("redamancywu@example.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/Redamancywu/FlexibleSDK-Processor.git")
                    developerConnection.set("scm:git:ssh://github.com:Redamancywu/FlexibleSDK-Processor.git")
                    url.set("https://github.com/Redamancywu/FlexibleSDK-Processor/tree/main")
                }
            }
        }
    }
    
    // JitPack发布不需要GitHub Packages仓库配置
    // repositories {
    //     maven {
    //         name = "GitHubPackages"
    //         url = uri("https://maven.pkg.github.com/Redamancywu/FlexibleSDK-Processor")
    //         credentials {
    //             username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
    //             password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
    //         }
    //     }
    // }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

// 处理重复文件
tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
