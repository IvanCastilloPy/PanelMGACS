package com.miapp

import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.FlowPane

class BarraNavegacion(
    orientacion: Orientation,
    private val alNavegar: (String) -> Unit
) : FlowPane(orientacion) {

    private val botones = mutableListOf<Button>()

    init {
        padding = Insets(15.0)
        hgap = 10.0
        vgap = 10.0
        alignment = Pos.CENTER

        // Crear los botones basados en la imagen
        crearBoton("Semáforo")
        crearBoton("Bitácora")
        crearBoton("RSE")
        crearBoton("Agenda")
        crearBoton("Kit digital")
        crearBoton("Ambiental")
    }

    private fun crearBoton(texto: String) {
        val btn = Button(texto).apply {
            prefWidth = 120.0
            prefHeight = 60.0
            style = estiloInactivo()
            
            setOnAction {
                actualizarEstiloActivo(this)
                alNavegar(texto)
            }
        }
        botones.add(btn)
        children.add(btn)

        // Marcar "Agenda" como activo por defecto, simulando la imagen
        if (texto == "Agenda") {
            actualizarEstiloActivo(btn)
        }
    }

    private fun actualizarEstiloActivo(botonActivo: Button) {
        botones.forEach { it.style = estiloInactivo() }
        botonActivo.style = estiloActivo()
    }

    // Estilos CSS integrados para simular el diseño de la imagen
    private fun estiloInactivo() = """
        -fx-background-color: #f3f4f6;
        -fx-text-fill: #374151;
        -fx-border-color: #e5e7eb;
        -fx-border-radius: 8;
        -fx-background-radius: 8;
        -fx-font-weight: bold;
    """.trimIndent()

    private fun estiloActivo() = """
        -fx-background-color: #1f2937;
        -fx-text-fill: white;
        -fx-border-radius: 8;
        -fx-background-radius: 8;
        -fx-font-weight: bold;
    """.trimIndent()
}