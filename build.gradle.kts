import gg.meza.stonecraft.mod

plugins {
    id("gg.meza.stonecraft")
}

base {
    archivesName.set("critical-orientation-${mod.loader}")
}

modSettings {
    clientOptions {
        fov = 90
        guiScale = 2
        narrator = false
        darkBackground = true
        musicVolume = 0.0
    }
}

dependencies {
    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

// Publishing configuration
publishMods {
    modrinth {
        if (mod.isFabric) requires("fabric-api")
    }

    curseforge {
        clientRequired = true
        serverRequired = false
        if (mod.isFabric) requires("fabric-api")
    }
}
