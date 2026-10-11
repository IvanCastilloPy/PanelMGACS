plugins {
    kotlin("jvm") version "1.9.22"
    id("org.openjfx.javafxplugin") version "0.1.0"
    application
    id("org.beryx.runtime") version "1.13.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("com.github.librepdf:openpdf:1.3.35")
    implementation("org.apache.poi:poi-ooxml:5.2.5")
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.graphics")
}

application {
    mainClass.set("com.miapp.MainAppKt")
}

runtime {
    // 1. Quitamos la compresión agresiva que daña los archivos en Windows
    options.set(listOf("--strip-debug", "--no-header-files", "--no-man-pages"))
    
    // 2. Incluimos el paquete completo 'java.se' y utilidades vitales para Apache POI (Excel)
    modules.set(listOf(
        "java.se",
        "jdk.unsupported",
        "jdk.unsupported.desktop",
        "jdk.charsets",
        "jdk.localedata",
        "jdk.crypto.ec",
        "jdk.zipfs" 
    ))

    launcher {
        noConsole = true 
    }
    jpackage {
        imageName = "PanelMGACS"
        installerName = "Instalador_PanelMGACS"
        
        if (System.getProperty("os.name").lowercase().contains("windows")) {
            installerOptions = listOf("--win-dir-chooser", "--win-shortcut", "--win-menu")
        }
    }
}