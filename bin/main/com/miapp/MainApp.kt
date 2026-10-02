package com.miapp

import javafx.application.Application
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage

class MainApp : Application() {

    // Contenedor principal que dividirá la pantalla
    private val panelPrincipal = BorderPane()

    override fun start(stage: Stage) {
        DatabaseManager.crearTablaSiNoExiste()

        // 1. Instanciar la barra de navegación
        // Cambia a Orientation.VERTICAL si prefieres los botones en una columna a la izquierda
        val barraNavegacion = BarraNavegacion(Orientation.HORIZONTAL) { destino ->
            cambiarVista(destino)
        }

        // 2. Posicionar la barra. 
        // Si usas vertical, cámbialo a: panelPrincipal.left = barraNavegacion
        panelPrincipal.top = barraNavegacion

        // 3. Cargar la vista predeterminada inicial (Agenda)
        cambiarVista("Agenda")

        stage.scene = Scene(panelPrincipal, 800.0, 600.0)
        stage.title = "Panel de Gestión Diaria — MGACS"
        stage.show()
    }

    // Lógica de enrutamiento: decide qué clase mostrar en el centro
    private fun cambiarVista(destino: String) {
        val nuevaVista = when (destino) {
            "Semáforo" -> VistaSemaforo()
            "Bitácora" -> VistaBitacora()
            "RSE" -> VistaRse()
            "Agenda" -> VistaAgenda()
            "Kit digital" -> VistaKitDigital()
            "Ambiental" -> VistaAmbiental()
            else -> VistaAgenda()
        }
        
        // Coloca la nueva vista en el centro de la pantalla
        panelPrincipal.center = nuevaVista
    }
}

fun main() {
    Application.launch(MainApp::class.java)
}