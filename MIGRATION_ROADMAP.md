# 🏋️ PPL Workout App - Migration Roadmap

## Overview
This document outlines the migration path from the current simplified architecture to the advanced, production-ready architecture shown in the database and workflow diagrams.

## Current State Analysis ✅

### Database Schema (Working)
- [x] **exercises** table: Basic structure with `id`, `name`, `isCompound` ✅
- [x] **workout_days** table: Stores daily workout sessions ✅
- [x] **workout_entries** table: Links exercises to workout days ✅
- [x] **set_entries** table: Individual set tracking with timing ✅

### Features Working
- [x] PPL workout creation based on day of week
- [x] Exercise detail screens with set progression
- [x] Individual set timing and completion tracking
- [x] Cross-exercise contamination fixed
- [x] Database reset functionality for development
- [x] Timer functionality with start/stop per set

### Current Architecture
- **Exercise Library**: Hardcoded in `PPLWorkoutDatabase.getPPLExercises()` (34 exercises)
- **Workout Logic**: Day-based in repository layer
- **Database Version**: 5 (with destructive migration enabled)

---

## Phase 1: Enhanced Exercise Library & Templates 📊

### 1.1 Enhance Exercise Entity ⭐ **START HERE**
**Current**: Simple Exercise entity with `id`, `name`, `isCompound`
**Target**: Rich exercise metadata for better user experience

#### Tasks:
- [ ] **1.1.1** Expand Exercise entity with additional fields:
  ```kotlin
  data class Exercise(
      @PrimaryKey val id: Int,
      val name: String,
      val isCompound: Boolean,
      val primaryMuscle: String,      // NEW
      val secondaryMuscles: String,   // NEW - comma separated
      val equipment: String,          // NEW - "Barbell", "Dumbbell", etc.
      val difficulty: String,         // NEW - "Beginner", "Intermediate", "Advanced" 
      val instructions: String,       // NEW - Step by step guide
      val tips: String,              // NEW - Form tips and common mistakes
      val category: String           // NEW - "Push", "Pull", "Legs"
  )
  ```

- [ ] **1.1.2** Create database migration (Version 5 → 6):
  ```kotlin
  val MIGRATION_5_6 = object : Migration(5, 6) {
      override fun migrate(database: SupportSQLiteDatabase) {
          // Add new columns with default values
          database.execSQL("ALTER TABLE exercises ADD COLUMN primaryMuscle TEXT NOT NULL DEFAULT ''")
          database.execSQL("ALTER TABLE exercises ADD COLUMN secondaryMuscles TEXT NOT NULL DEFAULT ''")
          // ... add other columns
      }
  }
  ```

- [ ] **1.1.3** Update `getPPLExercises()` with rich metadata:
  ```kotlin
  Exercise(1, "Barbell Bench Press", true, 
          primaryMuscle = "Chest", 
          secondaryMuscles = "Triceps,Front Delts",
          equipment = "Barbell", 
          difficulty = "Intermediate",
          instructions = "1. Lie on bench...",
          tips = "Keep shoulder blades retracted...",
          category = "Push")
  ```

### 1.2 Create Workout Templates System
**Goal**: Replace hardcoded day-based logic with flexible templates

#### Tasks:
- [ ] **1.2.1** Create WorkoutTemplate entity:
  ```kotlin
  @Entity(tableName = "workout_templates")
  data class WorkoutTemplate(
      @PrimaryKey(autoGenerate = true) val id: Int = 0,
      val name: String,              // "Push Day 1", "Pull Day 1", etc.
      val description: String,       // "Chest and Triceps focused"
      val estimatedDuration: Int,    // Minutes
      val difficulty: String,        // "Beginner", "Intermediate", "Advanced"
      val category: String          // "Push", "Pull", "Legs"
  )
  ```

- [ ] **1.2.2** Create TemplateExercise junction entity:
  ```kotlin
  @Entity(tableName = "template_exercises")
  data class TemplateExercise(
      @PrimaryKey(autoGenerate = true) val id: Int = 0,
      val templateId: Int,           // FK to workout_templates
      val exerciseId: Int,           // FK to exercises
      val orderIndex: Int,           // Exercise order in template
      val sets: Int,                 // Default sets for this exercise
      val reps: Int,                 // Default reps for this exercise
      val restSeconds: Int          // Recommended rest between sets
  )
  ```

- [ ] **1.2.3** Create corresponding DAOs:
  ```kotlin
  @Dao interface WorkoutTemplateDao
  @Dao interface TemplateExerciseDao
  ```

### 1.3 Implement Template-Based Workout Creation
**Goal**: Replace current day-based workout creation with template selection

#### Tasks:
- [ ] **1.3.1** Update Repository to use templates:
  ```kotlin
  suspend fun createWorkoutFromTemplate(templateId: Int, date: String): List<WorkoutEntry>
  ```

- [ ] **1.3.2** Create template population in database:
  ```kotlin
  private fun getPPLTemplates(): List<WorkoutTemplate>
  private fun getTemplateExercises(): List<TemplateExercise>
  ```

- [ ] **1.3.3** Update ViewModel to use template-based creation
- [ ] **1.3.4** Migrate from day-based to template-based workout creation

---

## Phase 2: Advanced Features 🚀

### 2.1 Progress Tracking
- [ ] Create PersonalRecord entity for 1RM tracking
- [ ] Implement weight progression suggestions
- [ ] Add exercise history graphs

### 2.2 Custom Workouts
- [ ] Allow users to create custom templates
- [ ] Exercise substitution system
- [ ] Workout sharing functionality

### 2.3 Analytics & Insights
- [ ] Weekly/monthly progress reports
- [ ] Muscle group balance analysis
- [ ] Training volume analytics

---

## Phase 3: Production Ready 🏆

### 3.1 Performance Optimization
- [ ] Database indexing optimization
- [ ] LazyColumn performance improvements
- [ ] Background data sync

### 3.2 User Experience
- [ ] Onboarding flow
- [ ] Exercise video/animation support
- [ ] Dark mode theme improvements

### 3.3 Data Management
- [ ] Export/import functionality
- [ ] Cloud backup integration
- [ ] Data migration between devices

---

## Implementation Priority 🎯

### Sprint 1 (Week 1): Foundation ✅ **COMPLETED**
1. **1.1.1** - Enhance Exercise Entity ⭐ ✅ **DONE**
2. **1.1.2** - Database Migration 5→6 ✅ **DONE**
3. **1.1.3** - Update exercise data with metadata ✅ **DONE**

### Sprint 2 (Week 2): Templates
4. **1.2.1** - WorkoutTemplate entity
5. **1.2.2** - TemplateExercise junction
6. **1.2.3** - Template DAOs

### Sprint 3 (Week 3): Integration
7. **1.3.1** - Template-based repository
8. **1.3.2** - Template population
9. **1.3.3** - ViewModel updates

---

## Week 1 Sprint 1 - Completed Features 🎯

### ✅ Enhanced Exercise Entity
- **Before**: Simple Exercise entity with only `id`, `name`, `isCompound`
- **After**: Rich Exercise entity with 8 additional fields:
  - `primaryMuscle`: Main muscle group targeted
  - `secondaryMuscles`: Secondary muscles (comma separated)
  - `equipment`: Required equipment type
  - `difficulty`: Beginner/Intermediate/Advanced
  - `instructions`: Step-by-step exercise guide
  - `tips`: Form tips and common mistakes
  - `category`: Push/Pull/Legs classification

### ✅ Database Migration v5 → v6
- **Migration**: Properly implemented Room database migration
- **Backward Compatibility**: Existing data preserved during migration
- **New Columns**: All new fields added with appropriate defaults
- **Build Status**: ✅ Build successful, no compilation errors

### ✅ Rich Exercise Data Library
- **New File**: Created `ExerciseData.kt` with comprehensive metadata
- **All 34 Exercises**: Each exercise now includes:
  - Detailed step-by-step instructions
  - Form tips and safety advice
  - Muscle group classifications
  - Equipment requirements
  - Difficulty levels
- **Database Integration**: Database now uses the enhanced exercise data

## Next Steps - Week 2 Sprint 2 🚀
Ready to start **Sprint 2** with workout templates system:
1. Create `WorkoutTemplate` entity for flexible workout creation
2. Create `TemplateExercise` junction table for template-exercise relationships
3. Build corresponding DAOs for the new entities
