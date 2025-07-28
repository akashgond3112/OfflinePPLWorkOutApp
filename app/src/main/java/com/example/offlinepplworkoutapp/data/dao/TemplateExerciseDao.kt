package com.example.offlinepplworkoutapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Transaction
import com.example.offlinepplworkoutapp.data.entity.TemplateExercise
import com.example.offlinepplworkoutapp.data.entity.Exercise
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for TemplateExercise operations
 */
@Dao
interface TemplateExerciseDao {

    @Query("SELECT * FROM template_exercises WHERE templateId = :templateId ORDER BY orderIndex")
    suspend fun getExercisesForTemplate(templateId: Int): List<TemplateExercise>

    @Query("""
        SELECT e.* FROM exercises e 
        INNER JOIN template_exercises te ON e.id = te.exerciseId 
        WHERE te.templateId = :templateId 
        ORDER BY te.orderIndex
    """)
    suspend fun getExerciseDetailsForTemplate(templateId: Int): List<Exercise>

    @Query("""
        SELECT te.*, e.name as exerciseName, e.primaryMuscle, e.equipment 
        FROM template_exercises te 
        INNER JOIN exercises e ON te.exerciseId = e.id 
        WHERE te.templateId = :templateId 
        ORDER BY te.orderIndex
    """)
    fun getTemplateExercisesWithDetails(templateId: Int): Flow<List<TemplateExerciseWithDetails>>

    @Query("SELECT * FROM template_exercises WHERE id = :id")
    suspend fun getTemplateExerciseById(id: Int): TemplateExercise?

    @Insert
    suspend fun insertTemplateExercise(templateExercise: TemplateExercise): Long

    @Insert
    suspend fun insertTemplateExercises(templateExercises: List<TemplateExercise>)

    @Update
    suspend fun updateTemplateExercise(templateExercise: TemplateExercise)

    @Delete
    suspend fun deleteTemplateExercise(templateExercise: TemplateExercise)

    @Query("DELETE FROM template_exercises WHERE templateId = :templateId")
    suspend fun deleteExercisesForTemplate(templateId: Int): Int

    @Query("DELETE FROM template_exercises")
    suspend fun deleteAll(): Int

    @Query("SELECT COUNT(*) FROM template_exercises WHERE templateId = :templateId")
    suspend fun getExerciseCountForTemplate(templateId: Int): Int

    @Query("SELECT MAX(orderIndex) FROM template_exercises WHERE templateId = :templateId")
    suspend fun getMaxOrderIndexForTemplate(templateId: Int): Int?

    @Query("UPDATE template_exercises SET orderIndex = orderIndex - 1 WHERE templateId = :templateId AND orderIndex > :deletedIndex")
    suspend fun reorderAfterDeletion(templateId: Int, deletedIndex: Int)
}

/**
 * Data class for template exercises with exercise details
 */
data class TemplateExerciseWithDetails(
    val id: Int,
    val templateId: Int,
    val exerciseId: Int,
    val orderIndex: Int,
    val sets: Int,
    val reps: Int,
    val restSeconds: Int,
    val weight: Double,
    val notes: String,
    val isSuperset: Boolean,
    val supersetGroup: Int,
    val exerciseName: String,
    val primaryMuscle: String,
    val equipment: String
)
