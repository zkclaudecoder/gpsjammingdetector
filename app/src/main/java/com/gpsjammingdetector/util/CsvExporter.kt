package com.gpsjammingdetector.util

import com.gpsjammingdetector.data.local.entity.AnomalyEntity
import com.gpsjammingdetector.data.local.entity.GpsReadingEntity
import java.io.OutputStream
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    fun exportReadings(readings: List<GpsReadingEntity>, outputStream: OutputStream) {
        PrintWriter(outputStream).use { writer ->
            writer.println("id,latitude,longitude,altitude,accuracy,speed,bearing,timestamp,datetime,isMock,provider,satelliteCount,sessionId")
            readings.forEach { r ->
                writer.println(
                    "${r.id},${r.latitude},${r.longitude},${r.altitude},${r.accuracy},${r.speed},${r.bearing}," +
                    "${r.timestamp},${dateFormat.format(Date(r.timestamp))},${r.isMock},${r.provider},${r.satelliteCount},${r.sessionId}"
                )
            }
        }
    }

    fun exportAnomalies(anomalies: List<AnomalyEntity>, outputStream: OutputStream) {
        PrintWriter(outputStream).use { writer ->
            writer.println("id,readingId,type,severity,description,value,threshold,latitude,longitude,timestamp,datetime,sessionId")
            anomalies.forEach { a ->
                writer.println(
                    "${a.id},${a.readingId},${a.type},${a.severity},\"${a.description}\",${a.value},${a.threshold}," +
                    "${a.latitude},${a.longitude},${a.timestamp},${dateFormat.format(Date(a.timestamp))},${a.sessionId}"
                )
            }
        }
    }
}
