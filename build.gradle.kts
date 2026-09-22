/*
 * Mantle - NeoForge 1.21.1 port build file
 * Original ForgeGradle build by Sunstrike, ProgWML6 (Slime Knights).
 * Converted to ModDevGradle for NeoForge 1.21.1.
 */

val modVersion = providers.gradleProperty("mod_version")
val forkVersion = providers.gradleProperty("fork_version")

val mcVersion = providers.gradleProperty("minecraft_version")
val mcRange = providers.gradleProperty("minecraft_range")

val loaderRange = providers.gradleProperty("loader_range")
val neoForgeVersion = providers.gradleProperty("neoforge_version")
val neoForgeRange = providers.gradleProperty("neoforge_range")

val parchmentMinecraft = providers.gradleProperty("parchment_minecraft")
val parchmentVersion = providers.gradleProperty("parchment_version")

val jeiVersion = providers.gradleProperty("jei_version")
val jeiRange = providers.gradleProperty("jei_range")

plugins {
    idea
    eclipse
    `maven-publish`
    id("net.neoforged.moddev") version "2.0.147"
    id("io.freefair.lombok") version "8.10"
}

group = "slimeknights.mantle"
// version = <mc>-<upstream Mantle version>-v<fork version>, e.g. 1.21.1-1.21.0-v1.0
version = "${mcVersion.get()}-${modVersion.get()}-v${forkVersion.get()}"
base {
    archivesName = "NeoMantle"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
}

println("Java: ${System.getProperty("java.version")}, JVM: ${System.getProperty("java.vm.version")} (${System.getProperty("java.vendor")}), Arch: ${System.getProperty("os.arch")}, Version: ${version}")

repositories {
    mavenCentral()
    maven("https://maven.blamejared.com/") {
        name = "BlameJared (JEI, CraftTweaker, etc.)"
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

neoForge {
    version = neoForgeVersion.get()

    parchment {
        minecraftVersion = parchmentMinecraft.get()
        mappingsVersion = parchmentVersion.get()
    }

    // Mantle widens access to a few vanilla members.
    accessTransformers.from("src/main/resources/META-INF/accesstransformer.cfg")

    runs {
        configureEach {
            gameDirectory = file("run")
            logLevel = org.slf4j.event.Level.DEBUG
        }
        create("client") {
            client()
        }
        create("server") {
            server()
            programArgument("--nogui")
        }
        create("data") {
            data()
            programArguments.addAll(
                "--mod", "mantle",
                "--all",
                "--output", file("src/generated/resources/").absolutePath,
                "--existing", file("src/main/resources/").absolutePath
            )
        }
    }

    mods {
        create("mantle") {
            sourceSet(sourceSets.main.get())
        }
    }
}

// Generated datagen output is packed into the jar.
sourceSets.main {
    resources {
        srcDir("src/generated/resources")
        exclude(".cache")
    }
}

dependencies {
    // JEI integration lives in slimeknights.mantle.plugin (ported to the 1.21.1 JEI 19.x API).
    compileOnly("mezz.jei:jei-${mcVersion.get()}-neoforge-api:${jeiVersion.get()}")
    runtimeOnly("mezz.jei:jei-${mcVersion.get()}-neoforge:${jeiVersion.get()}")
}

tasks.processResources {
    var replaceProperties = mapOf(
        "version"         to version,
        "mod_version"     to modVersion.get(),
        "fork_version"    to forkVersion.get(),
        "loader_range"    to loaderRange.get(),
        "minecraft_range" to mcRange.get(),
        "neoforge_range"  to neoForgeRange.get(),
        "jei_range"       to jeiRange.get()
    )
    inputs.properties(replaceProperties)

    filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) {
        expand(replaceProperties)
    }
}

tasks.jar {
    manifest {
        attributes(mapOf(
                "Specification-Title"    to "Mantle",
                "Specification-Vendor"   to "Slime Knights",
                "Specification-Version"  to "1",
                "Implementation-Title"   to name,
                "Implementation-Version" to "${version}",
                "Implementation-Vendor"  to "Slime Knights"
        ))
    }
}

tasks.named<Jar>("sourcesJar") {
    exclude(
        "assets/**",
        "data/**",
        "pack.png",
        "pack.mcmeta",
        "META-INF/neoforge.mods.toml"
    )
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        if (hasProperty("DEPLOY_DIR")) {
            maven(uri(property("DEPLOY_DIR")!!))
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
