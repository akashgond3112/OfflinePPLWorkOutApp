package com.example.offlinepplworkoutapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.offlinepplworkoutapp.data.entity.WorkoutTemplate
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for WorkoutTemplate operations
 */
@Dao
interface WorkoutTemplateDao {

    @Query("SELECT * FROM workout_templates WHERE isActive = 1 ORDER BY category, name")
    fun getAllActiveTemplates(): Flow<List<WorkoutTemplate>>

    @Query("SELECT * FROM workout_templates WHERE category = :category AND isActive = 1 ORDER BY name")
    fun getTemplatesByCategory(category: String): Flow<List<WorkoutTemplate>>

    @Query("SELECT * FROM workout_templates WHERE id = :templateId")
    suspend fun getTemplateById(templateId: Int): WorkoutTemplate?

    @Query("SELECT * FROM workout_templates WHERE category = :category AND difficulty = :difficulty AND isActive = 1")
    suspend fun getTemplatesByCategoryAndDifficulty(category: String, difficulty: String): List<WorkoutTemplate>

    @Insert
    suspend fun insertTemplate(template: WorkoutTemplate): Long

    @Insert
    suspend fun insertTemplates(templates: List<WorkoutTemplate>)

    @Update
    suspend fun updateTemplate(template: WorkoutTemplate)

    @Delete
    suspend fun deleteTemplate(template: WorkoutTemplate)

    @Query("DELETE FROM workout_templates")
    suspend fun deleteAll(): Int

    @Query("SELECT COUNT(*) FROM workout_templates")
    suspend fun getTemplateCount(): Int

    @Query("UPDATE workout_templates SET lastUsedDate = :date WHERE id = :templateId")
    suspend fun updateLastUsedDate(templateId: Int, date: String)

    @Query("SELECT * FROM workout_templates WHERE isCustom = 1 AND isActive = 1 ORDER BY name")
    fun getCustomTemplates(): Flow<List<WorkoutTemplate>>

    @Query("SELECT * FROM workout_templates WHERE isCustom = 0 AND isActive = 1 ORDER BY category, name")
    fun getPredefinedTemplates(): Flow<List<WorkoutTemplate>>
}
