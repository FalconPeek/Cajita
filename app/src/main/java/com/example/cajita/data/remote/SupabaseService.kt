package com.example.cajita.data.remote

import android.util.Log
import com.example.cajita.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class SupabaseService {

    private val supabaseUrl: String = BuildConfig.SUPABASE_URL.trimEnd('/')
    private val supabaseKey: String = BuildConfig.SUPABASE_KEY

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    private fun isConfigured(): Boolean {
        return supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()
    }

    suspend fun insertTurno(turno: TurnoSupabaseDto): Boolean {
        if (!isConfigured()) return false
        return try {
            val response = client.post("$supabaseUrl/rest/v1/turnos") {
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", supabaseKey)
                    append(HttpHeaders.Authorization, "Bearer $supabaseKey")
                    append("Prefer", "return=minimal")
                }
                setBody(turno)
            }
            isSuccessOrDuplicate(response.status, response.bodyAsText())
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error in insertTurno", e)
            false
        }
    }

    suspend fun insertMovimiento(movimiento: MovimientoSupabaseDto): Boolean {
        if (!isConfigured()) return false
        return try {
            val response = client.post("$supabaseUrl/rest/v1/movimientos") {
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", supabaseKey)
                    append(HttpHeaders.Authorization, "Bearer $supabaseKey")
                    append("Prefer", "return=minimal")
                }
                setBody(movimiento)
            }
            isSuccessOrDuplicate(response.status, response.bodyAsText())
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error in insertMovimiento", e)
            false
        }
    }

    suspend fun insertVenta(venta: VentaSupabaseDto): Boolean {
        if (!isConfigured()) return false
        return try {
            val response = client.post("$supabaseUrl/rest/v1/ventas") {
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", supabaseKey)
                    append(HttpHeaders.Authorization, "Bearer $supabaseKey")
                    append("Prefer", "return=minimal")
                }
                setBody(venta)
            }
            isSuccessOrDuplicate(response.status, response.bodyAsText())
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error in insertVenta", e)
            false
        }
    }

    suspend fun insertEgreso(egreso: EgresoSupabaseDto): Boolean {
        if (!isConfigured()) return false
        return try {
            val response = client.post("$supabaseUrl/rest/v1/egresos") {
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", supabaseKey)
                    append(HttpHeaders.Authorization, "Bearer $supabaseKey")
                    append("Prefer", "return=minimal")
                }
                setBody(egreso)
            }
            isSuccessOrDuplicate(response.status, response.bodyAsText())
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error in insertEgreso", e)
            false
        }
    }

    suspend fun insertCierre(cierre: CierreSupabaseDto): Boolean {
        if (!isConfigured()) return false
        return try {
            val response = client.post("$supabaseUrl/rest/v1/cierres") {
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", supabaseKey)
                    append(HttpHeaders.Authorization, "Bearer $supabaseKey")
                    append("Prefer", "return=minimal")
                }
                setBody(cierre)
            }
            isSuccessOrDuplicate(response.status, response.bodyAsText())
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error in insertCierre", e)
            false
        }
    }

    suspend fun fetchUltimoCierre(): CierreSupabaseDto? {
        if (!isConfigured()) return null
        return try {
            val response = client.get("$supabaseUrl/rest/v1/cierres?order=cerrado_en.desc&limit=1") {
                headers {
                    append("apikey", supabaseKey)
                    append(HttpHeaders.Authorization, "Bearer $supabaseKey")
                }
            }
            if (response.status == HttpStatusCode.OK) {
                val list = json.decodeFromString<List<CierreSupabaseDto>>(response.bodyAsText())
                list.firstOrNull()
            } else null
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error in fetchUltimoCierre", e)
            null
        }
    }

    suspend fun fetchTurnoById(id: String): TurnoSupabaseDto? {
        if (!isConfigured()) return null
        return try {
            val response = client.get("$supabaseUrl/rest/v1/turnos?id=eq.$id&limit=1") {
                headers {
                    append("apikey", supabaseKey)
                    append(HttpHeaders.Authorization, "Bearer $supabaseKey")
                }
            }
            if (response.status == HttpStatusCode.OK) {
                val list = json.decodeFromString<List<TurnoSupabaseDto>>(response.bodyAsText())
                list.firstOrNull()
            } else null
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error in fetchTurnoById", e)
            null
        }
    }

    suspend fun fetchTurnoAbierto(): TurnoSupabaseDto? {
        if (!isConfigured()) return null
        return try {
            val response = client.get("$supabaseUrl/rest/v1/turnos?estado=eq.ABIERTO&order=abierto_en.desc&limit=1") {
                headers {
                    append("apikey", supabaseKey)
                    append(HttpHeaders.Authorization, "Bearer $supabaseKey")
                }
            }
            if (response.status == HttpStatusCode.OK) {
                val list = json.decodeFromString<List<TurnoSupabaseDto>>(response.bodyAsText())
                list.firstOrNull()
            } else null
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error in fetchTurnoAbierto", e)
            null
        }
    }

    private fun isSuccessOrDuplicate(status: HttpStatusCode, bodyText: String): Boolean {
        if (status.value in 200..299) return true
        if (status == HttpStatusCode.Conflict || bodyText.contains("duplicate key") || bodyText.contains("23505")) {
            return true
        }
        Log.e("SupabaseService", "Sync Failed [HTTP ${status.value}]: $bodyText")
        return false
    }
}
