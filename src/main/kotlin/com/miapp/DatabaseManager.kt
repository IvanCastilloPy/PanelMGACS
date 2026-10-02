package com.miapp

import java.sql.Connection
import java.sql.DriverManager

object DatabaseManager {
    // La ruta crea un archivo local llamado actividades.db en la raíz del proyecto
    private const val URL = "jdbc:sqlite:actividades.db"

    fun getConnection(): Connection {
        return DriverManager.getConnection(URL)
    }

    fun crearTablaSiNoExiste() {
        val query = """
            CREATE TABLE IF NOT EXISTS actividad_diaria (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                titulo VARCHAR(100) NOT NULL,
                estado VARCHAR(20) NOT NULL
            )
        """.trimIndent()
        getConnection().use { conn ->
            conn.createStatement().execute(query)
        }
    }

    fun obtenerActividades(): List<ActividadDiaria> {
        val lista = mutableListOf<ActividadDiaria>()
        getConnection().use { conn ->
            val rs = conn.createStatement().executeQuery("SELECT * FROM actividad_diaria")
            while (rs.next()) {
                lista.add(ActividadDiaria(rs.getInt("id"), rs.getString("titulo"), rs.getString("estado")))
            }
        }
        return lista
    }

    fun guardarActividad(titulo: String, estado: String) {
        val query = "INSERT INTO actividad_diaria (titulo, estado) VALUES (?, ?)"
        getConnection().use { conn ->
            val pstmt = conn.prepareStatement(query)
            pstmt.setString(1, titulo)
            pstmt.setString(2, estado)
            pstmt.executeUpdate()
        }
    }
}