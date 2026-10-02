package com.miapp

import com.lowagie.text.Document
import com.lowagie.text.Element
import com.lowagie.text.Font
import com.lowagie.text.Paragraph
import com.lowagie.text.Phrase
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

data class ReporteSemaforoMensual(val mes: String, val anio: String, val estados: Map<String, String>)

object ExportManager {

    fun exportarAExcel(actividades: List<ActividadDiaria>, archivo: File) {
        XSSFWorkbook().use { workbook ->
            val sheet = workbook.createSheet("Reporte de Actividades")
            
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("ID")
            headerRow.createCell(1).setCellValue("Título")
            headerRow.createCell(2).setCellValue("Estado")

            actividades.forEachIndexed { index, actividad ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(actividad.id.toDouble())
                row.createCell(1).setCellValue(actividad.titulo)
                row.createCell(2).setCellValue(actividad.estado)
            }

            sheet.autoSizeColumn(0)
            sheet.autoSizeColumn(1)
            sheet.autoSizeColumn(2)

            FileOutputStream(archivo).use { outputStream ->
                workbook.write(outputStream)
            }
        }
    }

    fun exportarAPdf(actividades: List<ActividadDiaria>, archivo: File) {
        val document = Document()
        FileOutputStream(archivo).use { outputStream ->
            PdfWriter.getInstance(document, outputStream)
            document.open()

            val tituloPdf = Paragraph("Reporte de Gestión Diaria\n\n")
            document.add(tituloPdf)

            val table = PdfPTable(3)
            
            table.addCell(Phrase("ID"))
            table.addCell(Phrase("Título"))
            table.addCell(Phrase("Estado"))

            actividades.forEach { actividad ->
                table.addCell(Phrase(actividad.id.toString()))
                table.addCell(Phrase(actividad.titulo))
                table.addCell(Phrase(actividad.estado))
            }

            document.add(table)
            document.close()
        }
    }

    fun exportarSemaforoAPdf(reportes: List<ReporteSemaforoMensual>, archivo: File) {
        val document = Document()
        FileOutputStream(archivo).use { outputStream ->
            PdfWriter.getInstance(document, outputStream)
            document.open()

            val tituloPrincipal = Paragraph("Historial de Semáforos de Diagnóstico\n\n").apply {
                alignment = Element.ALIGN_CENTER
                font = Font(Font.HELVETICA, 16f, Font.BOLD)
            }
            document.add(tituloPrincipal)

            reportes.forEach { reporte ->
                val tituloMes = Paragraph("Período evaluado: ${reporte.mes} ${reporte.anio}\n").apply {
                    font = Font(Font.HELVETICA, 12f, Font.BOLD)
                }
                document.add(tituloMes)

                val table = PdfPTable(2)
                table.setWidthPercentage(100f) // Uso del método setter seguro
                table.setSpacingBefore(10f)
                table.setSpacingAfter(25f)
                
                table.addCell(Phrase("Factor Analizado"))
                table.addCell(Phrase("Estado Declarado"))

                reporte.estados.forEach { (factor, estado) ->
                    table.addCell(Phrase(factor))
                    table.addCell(Phrase(estado))
                }

                document.add(table)
            }

            document.close()
        }
    }
}