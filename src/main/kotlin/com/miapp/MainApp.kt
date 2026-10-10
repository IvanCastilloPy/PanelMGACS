package com.miapp

import javafx.application.Application
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import javafx.scene.control.ScrollPane
import javafx.scene.layout.StackPane

class MainApp : Application() {
    private val panelPrincipal = BorderPane().apply {
    style = "-fx-background-color: #A4CF4A; -fx-border-color : #A4CF4A" 
    }

    override fun start(stage: Stage) {
        DatabaseManager.crearTablaSiNoExiste()

        val barraNavegacion = BarraNavegacion(Orientation.HORIZONTAL) { destino ->
            cambiarVista(destino)
        }

        panelPrincipal.top = barraNavegacion

        cambiarVista("Semáforo")

        stage.scene = Scene(panelPrincipal, 800.0, 600.0)
        stage.title = "Panel de Gestión Diaria — MGACS"
        stage.show()
    }

    private fun cambiarVista(destino: String) {
        val nuevaVista = when (destino) {
            "Semáforo" -> VistaSemaforo()
            "Bitácora" -> VistaBitacora()
            "RSE" -> VistaRse()
            "Agenda" -> VistaAgenda()
            "Kit digital" -> VistaKitDigital()
            else -> VistaAgenda()
        }
        
        // 1. Envolvemos la vista en un StackPane para centrarla horizontalmente siempre
        val contenedorCentrado = StackPane(nuevaVista).apply {
            padding = javafx.geometry.Insets(20.0)
        }
        
        // 2. Colocamos el contenedor centrado dentro del ScrollPane en lugar de la vista directa
        val panelDeslizable = ScrollPane(contenedorCentrado).apply {
            isFitToWidth = true 
            style = "-fx-background: #EDF7BE; ;-fx-background-color: #EDF7BE -fx-control-inner-background: transparent; -fx-border-color: #EDF7BE"
        }
        
        panelPrincipal.center = panelDeslizable
    }
}

fun main() {
    Application.launch(MainApp::class.java)
}