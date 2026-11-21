package com.example.data

import android.content.Context
import android.util.Log
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Repositório 100% offline: guarda os lembretes num arquivo JSON dentro do armazenamento
 * privado do app. Não precisa de internet, conta Google nem Firebase.
 *
 * O parâmetro userId continua existindo nas funções só para manter a assinatura antiga; é ignorado.
 */
class ReminderRepository private constructor(context: Context) {

    private val file = File(context.applicationContext.filesDir, FILE_NAME)
    private val lock = Any()
    private val items = MutableStateFlow(load())

    fun snapshot(): List<Reminder> = items.value

    @Suppress("UNUSED_PARAMETER")
    fun observeReminders(userId: String): Flow<List<Reminder>> =
        items.map { list -> list.sortedBy { it.dueAtMillis } }

    @Suppress("UNUSED_PARAMETER")
    suspend fun addReminder(
        userId: String,
        title: String,
        notes: String,
        category: ReminderCategory,
        priority: ReminderPriority,
        recurrence: ReminderRecurrence,
        dueAtMillis: Long,
        notifyAlert: Boolean
    ): Result<String> = runCatchingIo {
        val cleanTitle = title.trim().take(140)
        require(cleanTitle.isNotEmpty()) { "O título não pode estar vazio." }
        val now = System.currentTimeMillis()
        val reminder = Reminder(
            id = UUID.randomUUID().toString(),
            userId = LOCAL_USER_ID,
            title = cleanTitle,
            notes = notes.trim().take(1000),
            category = category.name,
            priority = priority.name,
            recurrence = recurrence.name,
            dueAtMillis = dueAtMillis.coerceAtLeast(0L),
            completed = false,
            notifyAlert = notifyAlert,
            createdAt = now,
            updatedAt = now
        )
        mutate { it + reminder }
        reminder.id
    }

    @Suppress("UNUSED_PARAMETER")
    suspend fun updateReminder(
        userId: String,
        reminderId: String,
        title: String,
        notes: String,
        category: ReminderCategory,
        priority: ReminderPriority,
        recurrence: ReminderRecurrence,
        dueAtMillis: Long,
        notifyAlert: Boolean
    ): Result<Unit> = runCatchingIo {
        require(reminderId.isNotBlank()) { "ID do lembrete inválido." }
        val cleanTitle = title.trim().take(140)
        require(cleanTitle.isNotEmpty()) { "O título não pode estar vazio." }
        val now = System.currentTimeMillis()
        mutate { list ->
            list.map {
                if (it.id == reminderId) {
                    it.copy(
                        title = cleanTitle,
                        notes = notes.trim().take(1000),
                        category = category.name,
                        priority = priority.name,
                        recurrence = recurrence.name,
                        dueAtMillis = dueAtMillis.coerceAtLeast(0L),
                        notifyAlert = notifyAlert,
                        updatedAt = now
                    )
                } else it
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    suspend fun setCompleted(
        userId: String,
        reminderId: String,
        completed: Boolean
    ): Result<Unit> = runCatchingIo {
        require(reminderId.isNotBlank()) { "ID do lembrete inválido." }
        val now = System.currentTimeMillis()
        mutate { list ->
            list.map {
                if (it.id == reminderId) it.copy(completed = completed, updatedAt = now) else it
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    suspend fun rescheduleReminder(
        userId: String,
        reminderId: String,
        newDueAtMillis: Long,
        completed: Boolean = false
    ): Result<Unit> = runCatchingIo {
        require(reminderId.isNotBlank()) { "ID do lembrete inválido." }
        val now = System.currentTimeMillis()
        mutate { list ->
            list.map {
                if (it.id == reminderId) {
                    it.copy(
                        dueAtMillis = newDueAtMillis.coerceAtLeast(0L),
                        completed = completed,
                        updatedAt = now
                    )
                } else it
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    suspend fun deleteReminder(
        userId: String,
        reminderId: String
    ): Result<Unit> = runCatchingIo {
        require(reminderId.isNotBlank()) { "ID do lembrete inválido." }
        mutate { list -> list.filterNot { it.id == reminderId } }
    }

    private suspend fun <T> runCatchingIo(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "Falha na operação local", e)
        Result.failure(e)
    }

    private suspend fun mutate(transform: (List<Reminder>) -> List<Reminder>) {
        withContext(Dispatchers.IO) {
            synchronized(lock) {
                val updated = transform(items.value)
                persist(updated)
                items.value = updated
            }
        }
    }

    private fun persist(list: List<Reminder>) {
        val array = JSONArray()
        list.forEach { r ->
            array.put(
                JSONObject().apply {
                    put("id", r.id)
                    put("title", r.title)
                    put("notes", r.notes)
                    put("category", r.category)
                    put("priority", r.priority)
                    put("recurrence", r.recurrence)
                    put("dueAtMillis", r.dueAtMillis)
                    put("completed", r.completed)
                    put("notifyAlert", r.notifyAlert)
                    put("createdAt", r.createdAt)
                    put("updatedAt", r.updatedAt)
                }
            )
        }
        // Escreve num arquivo temporário e troca, para nunca deixar o arquivo pela metade.
        val tmp = File(file.parentFile, "$FILE_NAME.tmp")
        tmp.writeText(array.toString())
        if (!tmp.renameTo(file)) {
            file.writeText(array.toString())
            tmp.delete()
        }
    }

    private fun load(): List<Reminder> {
        if (!file.exists()) return emptyList()
        return try {
            val array = JSONArray(file.readText())
            (0 until array.length()).mapNotNull { index ->
                val o = array.optJSONObject(index) ?: return@mapNotNull null
                val id = o.optString("id")
                if (id.isBlank()) return@mapNotNull null
                Reminder(
                    id = id,
                    userId = LOCAL_USER_ID,
                    title = o.optString("title"),
                    notes = o.optString("notes"),
                    category = o.optString("category", ReminderCategory.PERSONAL.name),
                    priority = o.optString("priority", ReminderPriority.MEDIUM.name),
                    recurrence = o.optString("recurrence", ReminderRecurrence.NONE.name),
                    dueAtMillis = o.optLong("dueAtMillis", 0L),
                    completed = o.optBoolean("completed", false),
                    notifyAlert = o.optBoolean("notifyAlert", true),
                    createdAt = o.optLong("createdAt", 0L),
                    updatedAt = o.optLong("updatedAt", 0L)
                )
            }
        } catch (e: Exception) {
            // Arquivo corrompido: guarda uma cópia e começa vazio em vez de fechar o app.
            Log.e(TAG, "Arquivo de lembretes ilegível", e)
            runCatching { file.renameTo(File(file.parentFile, "$FILE_NAME.corrompido")) }
            emptyList()
        }
    }

    companion object {
        private const val TAG = "ReminderRepository"
        private const val FILE_NAME = "reminders.json"
        const val LOCAL_USER_ID = "local"

        @Volatile
        private var instance: ReminderRepository? = null

        fun get(context: Context): ReminderRepository =
            instance ?: synchronized(this) {
                instance ?: ReminderRepository(context.applicationContext).also { instance = it }
            }
    }
}
