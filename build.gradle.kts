plugins {
    kotlin("jvm") version "1.9.22"
    id("org.openjfx.javafxplugin") version "0.1.0"
    application
    id("org.beryx.runtime") version "1.13.1"
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}
kotlin {
    jvmToolchain(21)
}

dependencies {   
    //Libreria para utilizar SQLite
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")

    //Libreria para exportar a PDF
    implementation("com.github.librepdf:openpdf:1.3.35")

    //Libreria para exportar a archivo Excel
    implementation("org.apache.poi:poi-ooxml:5.2.5")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")
}

javafx {
    version = "21"
    modules = listOf("javafx.controls","javafx.graphics")
}

application {
    mainClass.set("com.miapp.MainAppKt")
}

runtime {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        noConsole = true 
    }
    jpackage {
        imageName = "PanelMGACS"
        installerName = "Instalador_PanelMGACS"
        
        // Aplica estas reglas solo si el compilador detecta que está corriendo en Windows
        if (System.getProperty("os.name").lowercase().contains("windows")) {
            installerOptions = listOf("--win-dir-chooser", "--win-shortcut", "--win-menu")
        }
    }
}