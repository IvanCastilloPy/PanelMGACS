package com.miapp

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.control.Button
import javafx.stage.FileChooser
import javafx.scene.control.Alert
import javafx.scene.control.Separator
import javafx.scene.layout.HBox
import javafx.scene.text.FontWeight
import java.time.LocalDate
import com.miapp.ExportManager


// Vistas predeterminadas para cada sección
class VistaSemaforo : VBox(15.0) {
    private val estadoFactores = mutableMapOf<String, String>()
    private val reportesCargados = mutableListOf<ReporteSemaforoMensual>() // Aquí acumulamos los meses
    
    private val lblResumen = Label("0 de 5 factores marcados este mes. Todavía no hay datos del mes anterior.")
    private val contenedorLista = VBox(5.0) // UI para ver los meses acumulados

    init {
        padding = Insets(30.0)
        style = "-fx-background-color: #f9fafb; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;"
        maxWidth = 700.0

        val titulo = Label("Semáforo de Diagnóstico Mensual").apply { font = Font.font("System", FontWeight.BOLD, 22.0) }
        val subtitulo = Label("Marcá cómo está cada factor este mes. Un vistazo de 5 minutos, comparable mes a mes.").apply { textFill = Color.GRAY }

        val meses = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        val cbMes = ComboBox<String>().apply { 
            items.addAll(meses)
            selectionModel.select(LocalDate.now().monthValue - 1)
        }
        val txtAnio = TextField(LocalDate.now().year.toString()).apply { prefWidth = 80.0 }
        
        // Botón para cargar el mes actual a la lista
        val btnAñadir = Button("Añadir a la lista").apply {
            style = "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-border-radius: 6; -fx-background-radius: 6;"
            setOnAction {
                // Guardamos una copia exacta del estado actual
                val nuevoReporte = ReporteSemaforoMensual(cbMes.value, txtAnio.text, estadoFactores.toMap())
                reportesCargados.add(nuevoReporte)
                actualizarListaVisual()
            }
        }

        // Botón para exportar TODO lo acumulado
        val btnExportar = Button("Exportar a PDF").apply {
            style = "-fx-background-color: #1f2937; -fx-text-fill: white; -fx-border-radius: 6; -fx-background-radius: 6;"
            setOnAction {
                if (reportesCargados.isEmpty()) {
                    Alert(Alert.AlertType.WARNING, "Debes añadir al menos un mes a la lista antes de exportar.").showAndWait()
                    return@setOnAction
                }

                val fileChooser = FileChooser().apply {
                    title = "Guardar Semáforo PDF"
                    extensionFilters.add(FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"))
                    initialFileName = "Reporte_Semaforos.pdf"
                }
                val archivo = fileChooser.showSaveDialog(this@VistaSemaforo.scene.window)
                if (archivo != null) {
                    ExportManager.exportarSemaforoAPdf(reportesCargados, archivo)
                    Alert(Alert.AlertType.INFORMATION, "PDF guardado correctamente con ${reportesCargados.size} mes(es).").showAndWait()
                }
            }
        }

        val panelFiltros = HBox(10.0, Label("Mes:"), cbMes, Label("Año:"), txtAnio, btnAñadir, btnExportar).apply {
            alignment = Pos.CENTER_LEFT
            padding = Insets(10.0, 0.0, 20.0, 0.0)
        }

        val contenedorFactores = VBox(15.0)
        val listaFactores = listOf("Ventas / producción", "Costos de insumos", "Cumplimiento normativo", "Uso de tecnología", "Clima laboral")

        listaFactores.forEach { factor ->
            estadoFactores[factor] = "Sin evaluar"
            contenedorFactores.children.add(crearFilaSemaforo(factor))
            contenedorFactores.children.add(Separator())
        }

        lblResumen.apply { textFill = Color.GRAY; padding = Insets(10.0, 0.0, 10.0, 0.0) }

        // Sección visual de la "lista de espera"
        val tituloLista = Label("Meses listos para exportar:").apply { font = Font.font("System", FontWeight.BOLD, 14.0) }
        val seccionLista = VBox(10.0, tituloLista, contenedorLista).apply {
            padding = Insets(15.0)
            style = "-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-radius: 8;"
        }
        actualizarListaVisual()

        children.addAll(titulo, subtitulo, panelFiltros, contenedorFactores, lblResumen, seccionLista)
    }

    private fun crearFilaSemaforo(factor: String): HBox {
        val lblFactor = Label(factor).apply { 
            prefWidth = 300.0
            font = Font.font("System", FontWeight.BOLD, 14.0)
            textFill = Color.web("#374151")
        }
        
        val cVerde = crearCirculo(Color.web("#10b981"), factor, "Óptimo")
        val cAmarillo = crearCirculo(Color.web("#d97706"), factor, "Regular")
        val cRojo = crearCirculo(Color.web("#ef4444"), factor, "Crítico")

        val circulos = listOf(cVerde, cAmarillo, cRojo)

        circulos.forEach { circulo ->
            circulo.setOnMouseClicked {
                circulos.forEach { c -> c.opacity = 0.2 }
                circulo.opacity = 1.0
                estadoFactores[factor] = circulo.userData.toString()
                actualizarResumen()
            }
        }

        return HBox(20.0, lblFactor, cVerde, cAmarillo, cRojo).apply { alignment = Pos.CENTER_LEFT }
    }

    private fun crearCirculo(color: Color, factor: String, valor: String): Circle {
        return Circle(14.0, color).apply {
            opacity = 0.2
            userData = valor
            style = "-fx-cursor: hand;"
        }
    }

    private fun actualizarResumen() {
        val marcados = estadoFactores.values.count { it != "Sin evaluar" }
        val rojos = estadoFactores.values.count { it.contains("Rojo") }
        val textoRojos = if (rojos == 0) "Ningún factor en rojo." else "$rojos factor(es) en rojo."
        lblResumen.text = "$marcados de 5 factores marcados este mes. $textoRojos"
    }

    // Actualiza la cajita inferior donde ves qué meses vas a imprimir
    private fun actualizarListaVisual() {
        contenedorLista.children.clear()
        if (reportesCargados.isEmpty()) {
            contenedorLista.children.add(Label("Ningún mes añadido aún."))
            return
        }

        reportesCargados.forEachIndexed { index, reporte ->
            val lbl = Label("• ${reporte.mes} ${reporte.anio}")
            val btnBorrar = Button("Eliminar").apply {
                style = "-fx-text-fill: red; -fx-background-color: transparent; -fx-cursor: hand; -fx-underline: true;"
                setOnAction {
                    reportesCargados.removeAt(index)
                    actualizarListaVisual() // Refresca la vista al borrar
                }
            }
            val fila = HBox(15.0, lbl, btnBorrar).apply { alignment = Pos.CENTER_LEFT }
            contenedorLista.children.add(fila)
        }
    }
}

class VistaBitacora : VBox() { init { configurar("Menú: Bitácora") } }
class VistaRse : VBox() { init { configurar("Menú: RSE") } }
class VistaKitDigital : VBox() { init { configurar("Menú: Kit digital") } }
class VistaAmbiental : VBox() { init { configurar("Menú: Ambiental") } }

// La vista de la Agenda puede contener la tabla que hicimos en el paso anterior
class VistaAgenda : VBox() { 
    init { 
        configurar("Menú: Agenda de Contactos Institucionales") 
        // Aquí moveremos la tabla y el formulario más adelante
    } 
}

// Función auxiliar para centrar y dar estilo al texto predeterminado
private fun VBox.configurar(titulo: String) {
    alignment = Pos.CENTER
    val label = Label(titulo).apply { font = Font(24.0) }
    children.add(label)
}