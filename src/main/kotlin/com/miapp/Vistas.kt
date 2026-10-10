package com.miapp

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.FileChooser
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import java.awt.Desktop

data class RegistroBitacora(val fecha: LocalDate, val produccion: Int, val mermas: Int, val quejas: Int)

class VistaSemaforo : VBox(15.0) {
    private val estadoFactores = mutableMapOf<String, String>()
    private val reportesCargados = mutableListOf<ReporteSemaforoMensual>()
    private val lblResumen = Label("0 de 5 factores marcados este mes. Todavía no hay datos del mes anterior.")
    private val contenedorLista = VBox(5.0)
    init {
        padding = Insets(30.0)
        style = "-fx-background-color: #C2F2E4; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;"
        maxWidth = 700.0

        val titulo = Label("Semáforo de Diagnóstico Mensual").apply { font = Font.font("System", FontWeight.BOLD, 22.0) }
        val subtitulo = Label("Marcá cómo está cada factor este mes. Un vistazo de 5 minutos, comparable mes a mes.").apply { textFill = Color.GRAY }

        val meses = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        val cbMes = ComboBox<String>().apply { 
            items.addAll(meses)
            selectionModel.select(LocalDate.now().monthValue - 1)
        }
        val txtAnio = TextField(LocalDate.now().year.toString()).apply { prefWidth = 80.0 }
        
        val btnAñadir = Button("Añadir a la lista").apply {
            style = "-fx-background-color: #A4CF4A; -fx-text-fill: #374151; -fx-border-radius: 6; -fx-background-radius: 6;"
            setOnAction {
                val nuevoReporte = ReporteSemaforoMensual(cbMes.value, txtAnio.text, estadoFactores.toMap())
                reportesCargados.add(nuevoReporte)
                actualizarListaVisual()
            }
        }

        val btnExportar = Button("Exportar a PDF").apply {
            style = "-fx-background-color: #A4CF4A; -fx-text-fill: #374151; -fx-border-radius: 6; -fx-background-radius: 6;"
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
        
        val cVerde = crearCirculo(Color.web("#10b981"), factor, "Verde (Óptimo)")
        val cAmarillo = crearCirculo(Color.web("#fbff0a"), factor, "Amarillo (Regular)")
        val cRojo = crearCirculo(Color.web("#ef4444"), factor, "Rojo (Crítico)")

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
                    actualizarListaVisual()
                }
            }
            val fila = HBox(15.0, lbl, btnBorrar).apply { alignment = Pos.CENTER_LEFT }
            contenedorLista.children.add(fila)
        }
    }
}

class VistaBitacora : VBox(15.0) {
    private val registros = mutableListOf<RegistroBitacora>()
    private val tabla = TableView<RegistroBitacora>()
    
    private val lblTotalesSemana = Label("Total semana: 0 | 0 | 0")
    private val lblAvisoMes = Label()

    init {
        padding = Insets(30.0)
        style = "-fx-background-color: #C2F2E4; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;"
        maxWidth = 750.0

        val titulo = Label("Bitácora Diaria de 3 Números").apply { font = Font.font("System", FontWeight.BOLD, 22.0) }
        val subtitulo = Label("Cargá producción, mermas y quejas. La suma semanal se calcula sola.").apply { textFill = Color.GRAY }

        val dpFecha = DatePicker(LocalDate.now()).apply { prefWidth = 140.0 }
        val txtProd = TextField().apply { promptText = "Producción/Ventas"; prefWidth = 130.0 }
        val txtMermas = TextField().apply { promptText = "Mermas"; prefWidth = 90.0 }
        val txtQuejas = TextField().apply { promptText = "Quejas"; prefWidth = 90.0 }
        
        val btnGuardar = Button("Guardar").apply {
            style = "-fx-background-color: #A4CF4A; -fx-text-fill: #374151; -fx-border-radius: 6;"
            setOnAction {
                val prod = txtProd.text.toIntOrNull() ?: 0
                val mermas = txtMermas.text.toIntOrNull() ?: 0
                val quejas = txtQuejas.text.toIntOrNull() ?: 0
                val fecha = dpFecha.value
                
                registros.removeIf { it.fecha == fecha }
                registros.add(RegistroBitacora(fecha, prod, mermas, quejas))
                registros.sortBy { it.fecha }
                
                actualizarUI(fecha)
                txtProd.clear(); txtMermas.clear(); txtQuejas.clear()
            }
        }

        val panelInputs = HBox(15.0, dpFecha, txtProd, txtMermas, txtQuejas, btnGuardar).apply { alignment = Pos.CENTER_LEFT }

        // --- SOLUCIÓN: Usamos PropertyValueFactory para la tabla ---
        val colFecha = TableColumn<RegistroBitacora, LocalDate>("Fecha").apply {
            cellValueFactory = PropertyValueFactory("fecha")
            prefWidth = 120.0
        }
        val colProd = TableColumn<RegistroBitacora, Int>("Producción/ventas").apply {
            cellValueFactory = PropertyValueFactory("produccion")
        }
        val colMermas = TableColumn<RegistroBitacora, Int>("Mermas").apply {
            cellValueFactory = PropertyValueFactory("mermas")
        }
        val colQuejas = TableColumn<RegistroBitacora, Int>("Quejas").apply {
            cellValueFactory = PropertyValueFactory("quejas")
        }
        
        tabla.columns.addAll(colFecha, colProd, colMermas, colQuejas)
        tabla.prefHeight = 200.0

        val btnExportar = Button("Exportar a PDF").apply {
            style = "-fx-background-color: #A4CF4A; -fx-text-fill: #374151; -fx-border-radius: 6;"
            setOnAction {
                if (registros.isEmpty()) {
                    Alert(Alert.AlertType.WARNING, "No hay datos para exportar.").showAndWait()
                    return@setOnAction
                }
                val fileChooser = FileChooser().apply {
                    title = "Guardar Bitácora PDF"
                    extensionFilters.add(FileChooser.ExtensionFilter("PDF", "*.pdf"))
                    initialFileName = "Bitacora.pdf"
                }
                val archivo = fileChooser.showSaveDialog(this@VistaBitacora.scene.window)
                if (archivo != null) {
                    ExportManager.exportarBitacoraAPdf(registros, archivo)
                    Alert(Alert.AlertType.INFORMATION, "PDF guardado.").showAndWait()
                }
            }
        }

        lblTotalesSemana.font = Font.font("System", FontWeight.BOLD, 14.0)
        lblAvisoMes.font = Font.font("System", FontWeight.NORMAL, 13.0)

        children.addAll(titulo, subtitulo, panelInputs, lblTotalesSemana, lblAvisoMes, btnExportar, tabla)
        actualizarUI(LocalDate.now())
    }

    private fun actualizarUI(fechaRef: LocalDate) {
        tabla.items.clear()
        tabla.items.addAll(registros)

        val weekFields = WeekFields.of(Locale.getDefault())
        val numSemana = fechaRef.get(weekFields.weekOfWeekBasedYear())
        
        val registrosSemana = registros.filter { 
            it.fecha.year == fechaRef.year && it.fecha.get(weekFields.weekOfWeekBasedYear()) == numSemana 
        }
        
        val tProd = registrosSemana.sumOf { it.produccion }
        val tMermas = registrosSemana.sumOf { it.mermas }
        val tQuejas = registrosSemana.sumOf { it.quejas }
        
        lblTotalesSemana.text = "Total semana seleccionada: $tProd Producción | $tMermas Mermas | $tQuejas Quejas"

        val diasEnMes = fechaRef.lengthOfMonth()
        val diasCargadosMes = registros.filter { it.fecha.month == fechaRef.month && it.fecha.year == fechaRef.year }.size
        
        if (diasCargadosMes == 0) {
            lblAvisoMes.text = "Todavía no cargaste ningún día de este mes."
            lblAvisoMes.textFill = Color.GRAY
        } else if (diasCargadosMes < diasEnMes) {
            lblAvisoMes.text = "⚠️ Atención: Mes incompleto ($diasCargadosMes de $diasEnMes días cargados)."
            lblAvisoMes.textFill = Color.web("#d97706") 
        } else {
            lblAvisoMes.text = "✅ Mes completo."
            lblAvisoMes.textFill = Color.web("#10b981") 
        }
    }
}

class VistaRse : VBox() {
    init {
        padding = Insets(30.0)
        style = "-fx-background-color: #C2F2E4; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;"
        maxWidth = 700.0

        // Títulos
        val titulo = Label("Generador de Publicación Mensual de RSE").apply { font = Font.font("System", FontWeight.BOLD, 22.0) }
        val subtitulo = Label("Completá los datos del mes y copiá el texto listo para WhatsApp o Facebook.").apply { textFill = Color.GRAY }

        // Campos de entrada
        val txtEmpresa = TextField().apply { promptText = "Ej: Industria San Roque" }
        val boxEmpresa = VBox(5.0, Label("Nombre de la empresa").apply { font = Font.font("System", FontWeight.BOLD, 12.0); textFill = Color.web("#4b5563") }, txtEmpresa)

        val txtAmbiental = TextField().apply { promptText = "Ej: reciclamos 120 kg de cartón" }
        val boxAmbiental = VBox(5.0, Label("Acción ambiental (qué y cuánto)").apply { font = Font.font("System", FontWeight.BOLD, 12.0); textFill = Color.web("#4b5563") }, txtAmbiental)

        val txtComunidad = TextField().apply { promptText = "Ej: donamos productos a 2 comedores" }
        val boxComunidad = VBox(5.0, Label("Acción con la comunidad").apply { font = Font.font("System", FontWeight.BOLD, 12.0); textFill = Color.web("#4b5563") }, txtComunidad)

        val txtCalidad = TextField().apply { promptText = "Ej: controlamos la calidad en cada lote" }
        val boxCalidad = VBox(5.0, Label("Acción de calidad").apply { font = Font.font("System", FontWeight.BOLD, 12.0); textFill = Color.web("#4b5563") }, txtCalidad)

        // Área del resultado
        val txtResultado = TextArea().apply {
            promptText = "Completá los campos y tocá \"Generar texto\"."
            isEditable = false
            isWrapText = true
            prefHeight = 120.0
            style = "-fx-control-inner-background: #f3f4f6; -fx-font-size: 14px;"
        }

        // Botón para copiar el texto
        val btnCopiar = Button("Copiar texto").apply {
            isDisable = true // Desactivado hasta que se genere un texto
            setOnAction {
                val clipboard = Clipboard.getSystemClipboard()
                val content = ClipboardContent()
                content.putString(txtResultado.text)
                clipboard.setContent(content)
                this.text = "¡Copiado!" // Cambia el texto para dar feedback visual
            }
        }

        // Botón para generar el mensaje
        val btnGenerar = Button("Generar texto").apply {
            style = "-fx-background-color: #A4CF4A; -fx-text-fill: #374151; -fx-border-radius: 6;"
            setOnAction {
                // Recupera los textos o pone un aviso si el usuario dejó algo en blanco
                val empresa = txtEmpresa.text.ifBlank { "[Nombre de la Empresa]" }
                val ambiental = txtAmbiental.text.ifBlank { "[su acción ambiental]" }
                val comunidad = txtComunidad.text.ifBlank { "[su acción comunitaria]" }
                val calidad = txtCalidad.text.ifBlank { "[su acción de calidad]" }

                // Plantilla del mensaje
                val mensaje = """
                    🌱 ¡Este mes en $empresa seguimos comprometidos con el impacto positivo!
                    
                    ♻️ Ambiental: $ambiental.
                    🤝 Comunidad: $comunidad.
                    ⭐ Calidad: $calidad.
                    
                    ¡Gracias por elegirnos y ser parte del cambio! ✨
                """.trimIndent()
                
                txtResultado.text = mensaje
                btnCopiar.isDisable = false // Activa el botón de copiar
                btnCopiar.text = "Copiar texto" // Restaura el texto del botón por si fue presionado antes
            }
        }

        val contenedorCampos = VBox(15.0, boxEmpresa, boxAmbiental, boxComunidad, boxCalidad)
        
        children.addAll(
            titulo, subtitulo, 
            contenedorCampos, 
            btnGenerar, 
            txtResultado, btnCopiar
        )
    }
}

data class ContactoInstitucional(val institucion: String, val persona: String, val ultimoContacto: String, val proximoSeguimiento: LocalDate)
class VistaAgenda : VBox(15.0) {
    private val contactos = mutableListOf<ContactoInstitucional>()
    private val tabla = TableView<ContactoInstitucional>()
    private val lblAviso = Label("Todavía no agregaste ningún contacto.")

    init {
        padding = Insets(30.0)
        style = "-fx-background-color: #C2F2E4; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;"
        maxWidth = 750.0

        // Títulos
        val titulo = Label("Agenda de Contactos Institucionales").apply { font = Font.font("System", FontWeight.BOLD, 22.0) }
        val subtitulo = Label("No perdás el hilo de tus gestiones con el MADES, la municipalidad, el MIC o la universidad.").apply { textFill = Color.GRAY }

        // Campos del formulario
        val txtInstitucion = TextField().apply { promptText = "Ej: MADES - oficina regional" }
        val boxInstitucion = VBox(5.0, Label("Institución").apply { font = Font.font("System", FontWeight.BOLD, 12.0); textFill = Color.web("#4b5563") }, txtInstitucion)

        val txtPersona = TextField().apply { promptText = "Nombre" }
        val boxPersona = VBox(5.0, Label("Persona de contacto").apply { font = Font.font("System", FontWeight.BOLD, 12.0); textFill = Color.web("#4b5563") }, txtPersona)

        val txtUltimoContacto = TextField().apply { promptText = "Ej: consulta sobre reciclaje" }
        val boxUltimoContacto = VBox(5.0, Label("Último contacto (qué se trató)").apply { font = Font.font("System", FontWeight.BOLD, 12.0); textFill = Color.web("#4b5563") }, txtUltimoContacto)

        val dpProximo = DatePicker(LocalDate.now()).apply { prefWidth = 150.0 }
        val boxProximo = VBox(5.0, Label("Próximo seguimiento").apply { font = Font.font("System", FontWeight.BOLD, 12.0); textFill = Color.web("#4b5563") }, dpProximo)

        // Botón de acción principal
        val btnAgregar = Button("Agregar contacto").apply {
            style = "-fx-background-color: #A4CF4A; -fx-text-fill: #374151; -fx-border-radius: 6;"
            setOnAction {
                if (txtInstitucion.text.isNotBlank()) {
                    contactos.add(
                        ContactoInstitucional(
                            txtInstitucion.text,
                            txtPersona.text.ifBlank { "Sin nombre" },
                            txtUltimoContacto.text.ifBlank { "Sin detalle" },
                            dpProximo.value
                        )
                    )
                    // Ordenar siempre por la fecha más próxima de seguimiento
                    contactos.sortBy { it.proximoSeguimiento }
                    actualizarTabla()
                    
                    txtInstitucion.clear(); txtPersona.clear(); txtUltimoContacto.clear()
                } else {
                    Alert(Alert.AlertType.WARNING, "La institución es obligatoria.").showAndWait()
                }
            }
        }

        // Configuración de la Tabla
        val colSeguimiento = TableColumn<ContactoInstitucional, LocalDate>("Próximo").apply {
            cellValueFactory = PropertyValueFactory("proximoSeguimiento")
            prefWidth = 100.0
        }
        val colInst = TableColumn<ContactoInstitucional, String>("Institución").apply {
            cellValueFactory = PropertyValueFactory("institucion")
            prefWidth = 180.0
        }
        val colPersona = TableColumn<ContactoInstitucional, String>("Persona").apply {
            cellValueFactory = PropertyValueFactory("persona")
            prefWidth = 120.0
        }
        val colUltimo = TableColumn<ContactoInstitucional, String>("Asunto tratado").apply {
            cellValueFactory = PropertyValueFactory("ultimoContacto")
            prefWidth = 200.0
        }
        
        tabla.columns.addAll(colSeguimiento, colInst, colPersona, colUltimo)
        tabla.prefHeight = 150.0

        // Botón para exportar PDF
        val btnExportar = Button("Exportar Agenda a PDF").apply {
            style = "-fx-background-color: #10b981; -fx-text-fill: white; -fx-border-radius: 6;"
            setOnAction {
                if (contactos.isEmpty()) {
                    Alert(Alert.AlertType.WARNING, "No hay contactos para exportar.").showAndWait()
                    return@setOnAction
                }
                val fileChooser = FileChooser().apply {
                    title = "Guardar Agenda PDF"
                    extensionFilters.add(FileChooser.ExtensionFilter("PDF", "*.pdf"))
                    initialFileName = "Agenda_Institucional.pdf"
                }
                val archivo = fileChooser.showSaveDialog(this@VistaAgenda.scene.window)
                if (archivo != null) {
                    ExportManager.exportarAgendaAPdf(contactos, archivo)
                    Alert(Alert.AlertType.INFORMATION, "Agenda exportada correctamente.").showAndWait()
                }
            }
        }

        lblAviso.textFill = Color.GRAY
        val contenedorTabla = VBox(10.0, tabla, btnExportar)
        contenedorTabla.isVisible = false // Se oculta hasta que haya datos

        // Exponer contenedorTabla para que actualizarTabla() pueda hacerlo visible
        this.userData = contenedorTabla 

        val contenedorFormulario = VBox(15.0, boxInstitucion, boxPersona, boxUltimoContacto, boxProximo, btnAgregar)
        
        children.addAll(titulo, subtitulo, contenedorFormulario, lblAviso, contenedorTabla)
    }

    private fun actualizarTabla() {
        tabla.items.clear()
        tabla.items.addAll(contactos)
        
        if (contactos.isNotEmpty()) {
            lblAviso.isVisible = false
            lblAviso.isManaged = false
            val contenedorTabla = this.userData as VBox
            contenedorTabla.isVisible = true
        }
    }
}
class VistaKitDigital : VBox(20.0){
    init {
        padding = Insets(30.0)
        style = "-fx-background-color: #C2F2E4; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;"
        maxWidth = 750.0

        // Títulos
        val titulo = Label("Kit Digital Mínimo").apply { font = Font.font("System", FontWeight.BOLD, 22.0) }
        val subtitulo = Label("Tres herramientas gratuitas para empezar, antes de pensar en un sistema más grande.").apply { 
            textFill = Color.GRAY
            padding = Insets(0.0, 0.0, 10.0, 0.0)
        }

        // Tarjeta 1: WhatsApp Business
        val cardWhatsApp = crearTarjeta(
            "📱 WhatsApp Business",
            listOf(
                "1. Descargá la app \"WhatsApp Business\" (es gratis, distinta de tu WhatsApp normal).",
                "2. Creá el perfil de tu empresa con nombre, rubro y horario.",
                "3. Usalo para pedidos, quejas y para publicar tu informe mensual de RSE."
            ),
            "Ir a WhatsApp Business",
            "https://business.whatsapp.com/"
        )

        // Tarjeta 2: Google Sheets
        val cardSheets = crearTarjeta(
            "📊 Google Sheets",
            listOf(
                "1. Entrá a tu cuenta de Google (o creá una, es gratis).",
                "2. Abrí Sheets y creá una planilla nueva.",
                "3. Copiá ahí la Bitácora de 3 números cuando quieras pasarla de papel a digital."
            ),
            "Ir a Google Sheets",
            "https://docs.google.com/spreadsheets/"
        )

        // Tarjeta 3: Google Forms
        val cardForms = crearTarjeta(
            "📝 Google Forms",
            listOf(
                "1. Entrá a Google Forms con tu misma cuenta de Google.",
                "2. Armá una encuesta corta de 3 o 4 preguntas para tus clientes.",
                "3. Compartí el link por WhatsApp para medir satisfacción."
            ),
            "Ir a Google Forms",
            "https://docs.google.com/forms/"
        )

        // Envolvemos las tarjetas en un contenedor general
        val contenedorTarjetas = VBox(15.0, cardWhatsApp, cardSheets, cardForms)

        children.addAll(titulo, subtitulo, contenedorTarjetas)
    }

    // Función auxiliar para construir visualmente las tarjetas para no repetir código
    private fun crearTarjeta(tituloTexto: String, pasos: List<String>, textoBoton: String, url: String): VBox {
        val lblTitulo = Label(tituloTexto).apply { 
            font = Font.font("System", FontWeight.BOLD, 16.0) 
            textFill = Color.web("#A4CF4A")
        }
        
        val contenedorPasos = VBox(5.0)
        pasos.forEach { paso ->
            val lblPaso = Label(paso).apply { 
                font = Font.font("System", 14.0)
                textFill = Color.web("#4b5563")
                isWrapText = true
            }
            contenedorPasos.children.add(lblPaso)
        }
        
        val btnEnlace = Button(textoBoton).apply {
            style = "-fx-background-color: #A4CF4A; -fx-text-fill: #374151; -fx-border-radius: 6; -fx-font-weight: bold; -fx-padding: 8 15 8 15;"
            setOnAction {
                abrirEnlaceWeb(url)
            }
        }
        
        return VBox(10.0, lblTitulo, contenedorPasos, btnEnlace).apply {
            padding = Insets(20.0)
            style = "-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-background-radius: 8;"
        }
    }

    // Función que se encarga de comunicarse con el sistema operativo para abrir el navegador
    private fun abrirEnlaceWeb(url: String) {
        try {
            if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                // Esto funciona nativo en Windows (tu ejecutable .exe final) y Mac
                java.awt.Desktop.getDesktop().browse(java.net.URI(url))
            } else {
                // Esto funciona nativo como plan de emergencia en tu CachyOS/Linux
                Runtime.getRuntime().exec(arrayOf("xdg-open", url))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Alert(Alert.AlertType.ERROR, "No se pudo abrir el navegador. Enlace: $url").showAndWait()
        }
    }
}