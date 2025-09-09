package com.example.movein.shared.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.movein.shared.data.UserData
import com.example.movein.shared.data.ChecklistData
import com.example.movein.shared.data.Defect
import com.example.movein.shared.data.ChecklistItem
import com.example.movein.shared.data.SubTask
import com.example.movein.shared.data.FileAttachment
import com.example.movein.shared.data.Priority
import com.example.movein.shared.data.TaskStatus
import com.example.movein.shared.data.DefectStatus
import com.example.movein.shared.data.DefectCategory
import org.json.JSONArray
import org.json.JSONObject

actual class AppStorage(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("movein_app", Context.MODE_PRIVATE)
    
    actual fun saveUserData(userData: UserData) {
        val json = JSONObject().apply {
            put("rooms", userData.rooms)
            put("bathrooms", userData.bathrooms)
            put("parking", userData.parking)
            put("warehouse", userData.warehouse)
            put("balconies", userData.balconies)
            put("selectedRoomNames", JSONArray(userData.selectedRoomNames))
        }
        prefs.edit().putString("user_data", json.toString()).apply()
    }
    
    actual fun loadUserData(): UserData? {
        val jsonString = prefs.getString("user_data", null) ?: return null
        return try {
            val json = JSONObject(jsonString)
            val roomNamesArray = json.getJSONArray("selectedRoomNames")
            val roomNames = mutableListOf<String>()
            for (i in 0 until roomNamesArray.length()) {
                roomNames.add(roomNamesArray.getString(i))
            }
            UserData(
                rooms = json.getInt("rooms"),
                bathrooms = json.getInt("bathrooms"),
                parking = json.getInt("parking"),
                warehouse = json.getBoolean("warehouse"),
                balconies = json.getInt("balconies"),
                selectedRoomNames = roomNames
            )
        } catch (e: Exception) {
            null
        }
    }
    
    actual fun saveChecklistData(checklistData: ChecklistData) {
        val json = JSONObject().apply {
            put("firstWeek", checklistItemsToJsonArray(checklistData.firstWeek))
            put("firstMonth", checklistItemsToJsonArray(checklistData.firstMonth))
            put("firstYear", checklistItemsToJsonArray(checklistData.firstYear))
        }
        prefs.edit().putString("checklist_data", json.toString()).apply()
    }
    
    actual fun loadChecklistData(): ChecklistData? {
        val jsonString = prefs.getString("checklist_data", null) ?: return null
        return try {
            val json = JSONObject(jsonString)
            ChecklistData(
                firstWeek = jsonArrayToChecklistItems(json.getJSONArray("firstWeek")),
                firstMonth = jsonArrayToChecklistItems(json.getJSONArray("firstMonth")),
                firstYear = jsonArrayToChecklistItems(json.getJSONArray("firstYear"))
            )
        } catch (e: Exception) {
            null
        }
    }
    
    actual fun saveDefects(defects: List<Defect>) {
        val jsonArray = JSONArray()
        defects.forEach { defect ->
            jsonArray.put(defectToJson(defect))
        }
        prefs.edit().putString("defects", jsonArray.toString()).apply()
    }
    
    actual fun loadDefects(): List<Defect> {
        val jsonString = prefs.getString("defects", null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonString)
            val defects = mutableListOf<Defect>()
            for (i in 0 until jsonArray.length()) {
                defects.add(jsonToDefect(jsonArray.getJSONObject(i)))
            }
            defects
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun checklistItemsToJsonArray(items: List<ChecklistItem>): JSONArray {
        val jsonArray = JSONArray()
        items.forEach { item ->
            jsonArray.put(checklistItemToJson(item))
        }
        return jsonArray
    }
    
    private fun jsonArrayToChecklistItems(jsonArray: JSONArray): List<ChecklistItem> {
        val items = mutableListOf<ChecklistItem>()
        for (i in 0 until jsonArray.length()) {
            items.add(jsonToChecklistItem(jsonArray.getJSONObject(i)))
        }
        return items
    }
    
    private fun checklistItemToJson(item: ChecklistItem): JSONObject {
        return JSONObject().apply {
            put("id", item.id)
            put("title", item.title)
            put("description", item.description)
            put("category", item.category)
            put("isCompleted", item.isCompleted)
            put("notes", item.notes)
            put("priority", item.priority.name)
            put("dueDate", item.dueDate ?: "")
            put("isUserAdded", item.isUserAdded)
            put("status", item.status.name)
            put("subTasks", subTasksToJsonArray(item.subTasks))
            put("attachments", attachmentsToJsonArray(item.attachments))
        }
    }
    
    private fun jsonToChecklistItem(json: JSONObject): ChecklistItem {
        val subTasksArray = json.getJSONArray("subTasks")
        val subTasks = mutableListOf<SubTask>()
        for (i in 0 until subTasksArray.length()) {
            subTasks.add(jsonToSubTask(subTasksArray.getJSONObject(i)))
        }
        
        val attachmentsArray = json.getJSONArray("attachments")
        val attachments = mutableListOf<FileAttachment>()
        for (i in 0 until attachmentsArray.length()) {
            attachments.add(jsonToFileAttachment(attachmentsArray.getJSONObject(i)))
        }
        
        return ChecklistItem(
            id = json.getString("id"),
            title = json.getString("title"),
            description = json.getString("description"),
            category = json.getString("category"),
            isCompleted = json.getBoolean("isCompleted"),
            notes = json.getString("notes"),
            priority = Priority.valueOf(json.getString("priority")),
            dueDate = json.getString("dueDate").takeIf { it.isNotEmpty() },
            isUserAdded = json.getBoolean("isUserAdded"),
            status = TaskStatus.valueOf(json.getString("status")),
            subTasks = subTasks,
            attachments = attachments
        )
    }
    
    private fun subTasksToJsonArray(subTasks: List<SubTask>): JSONArray {
        val jsonArray = JSONArray()
        subTasks.forEach { subTask ->
            jsonArray.put(JSONObject().apply {
                put("id", subTask.id)
                put("title", subTask.title)
                put("isCompleted", subTask.isCompleted)
            })
        }
        return jsonArray
    }
    
    private fun jsonToSubTask(json: JSONObject): SubTask {
        return SubTask(
            id = json.getString("id"),
            title = json.getString("title"),
            isCompleted = json.getBoolean("isCompleted")
        )
    }
    
    private fun attachmentsToJsonArray(attachments: List<FileAttachment>): JSONArray {
        val jsonArray = JSONArray()
        attachments.forEach { attachment ->
            jsonArray.put(JSONObject().apply {
                put("id", attachment.id)
                put("name", attachment.name)
                put("type", attachment.type)
                put("uri", attachment.uri)
                put("size", attachment.size)
            })
        }
        return jsonArray
    }
    
    private fun jsonToFileAttachment(json: JSONObject): FileAttachment {
        return FileAttachment(
            id = json.getString("id"),
            name = json.getString("name"),
            type = json.getString("type"),
            uri = json.getString("uri"),
            size = json.getLong("size")
        )
    }
    
    private fun defectToJson(defect: Defect): JSONObject {
        return JSONObject().apply {
            put("id", defect.id)
            put("location", defect.location)
            put("category", defect.category.name)
            put("priority", defect.priority.name)
            put("description", defect.description)
            put("status", defect.status.name)
            put("createdAt", defect.createdAt)
            put("dueDate", defect.dueDate ?: "")
            put("notes", defect.notes)
            put("assignedTo", defect.assignedTo ?: "")
            put("images", JSONArray(defect.images))
            put("subTasks", subTasksToJsonArray(defect.subTasks))
        }
    }
    
    private fun jsonToDefect(json: JSONObject): Defect {
        val subTasksArray = json.getJSONArray("subTasks")
        val subTasks = mutableListOf<SubTask>()
        for (i in 0 until subTasksArray.length()) {
            subTasks.add(jsonToSubTask(subTasksArray.getJSONObject(i)))
        }
        
        val imagesArray = json.getJSONArray("images")
        val images = mutableListOf<String>()
        for (i in 0 until imagesArray.length()) {
            images.add(imagesArray.getString(i))
        }
        
        return Defect(
            id = json.getString("id"),
            location = json.getString("location"),
            category = DefectCategory.valueOf(json.getString("category")),
            priority = Priority.valueOf(json.getString("priority")),
            description = json.getString("description"),
            status = DefectStatus.valueOf(json.getString("status")),
            createdAt = json.getString("createdAt"),
            dueDate = json.getString("dueDate").takeIf { it.isNotEmpty() },
            notes = json.getString("notes"),
            assignedTo = json.getString("assignedTo").takeIf { it.isNotEmpty() },
            images = images,
            subTasks = subTasks
        )
    }
}
