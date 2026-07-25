package com.example.localservice.data.local

import androidx.room.TypeConverter
import com.example.localservice.domain.model.Stage
import com.example.localservice.domain.model.StageStatus
import org.json.JSONArray
import org.json.JSONObject

class Converters {

    @TypeConverter
    fun fromStageList(stages: List<Stage>): String {
        val json = JSONArray()
        stages.forEach { stage ->
            json.put(JSONObject().apply {
                put("id", stage.id)
                put("name", stage.name)
                put("description", stage.description)
                put("status", stage.status.name)
                put("estimatedDays", stage.estimatedDays)
                put("order", stage.order)
                if (stage.completedAt != null) put("completedAt", stage.completedAt)
            })
        }
        return json.toString()
    }

    @TypeConverter
    fun toStageList(json: String): List<Stage> {
        if (json.isEmpty()) return emptyList()
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            Stage(
                id = obj.optString("id", ""),
                name = obj.optString("name", ""),
                description = obj.optString("description", ""),
                status = StageStatus.valueOf(obj.optString("status", "PENDING")),
                estimatedDays = obj.optInt("estimatedDays", 0),
                order = obj.optInt("order", 0),
                completedAt = if (obj.has("completedAt")) obj.optLong("completedAt") else null
            )
        }
    }
}
