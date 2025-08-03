package com.example.offlinepplworkoutapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.offlinepplworkoutapp.ui.theme.*

data class SetPerformanceData(
    val repsPerformed: Int,
    val weightUsed: Float
)

@Composable
fun SetDataEntryDialog(
    setNumber: Int,
    exerciseName: String,
    onDataEntered: (SetPerformanceData) -> Unit,
    onCancel: (() -> Unit)? = null, // 🆕 NEW: Optional cancel callback for edit mode
    isRestTimerRunning: Boolean = false,
    restTimeFormatted: String = "00:00",
    // NEW: Edit mode support
    isEditMode: Boolean = false,
    initialReps: Int = 0,
    initialWeight: Float = 0f
) {
    var repsText by remember { mutableStateOf(if (isEditMode && initialReps > 0) initialReps.toString() else "") }
    var weightText by remember { mutableStateOf(if (isEditMode && initialWeight > 0f) initialWeight.toString() else "") }
    var repsError by remember { mutableStateOf(false) }
    var weightError by remember { mutableStateOf(false) }

    println("🎯 DIALOG: SetDataEntryDialog opened for set $setNumber of $exerciseName (Edit mode: $isEditMode)")

    Dialog(
        onDismissRequest = {
            // Allow dismiss only in edit mode
            if (isEditMode && onCancel != null) {
                onCancel()
            } else {
                println("🎯 DIALOG: Dismiss request blocked - data entry required")
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = isEditMode, // Allow back press only in edit mode
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with rest timer indicator (only show for new entries, not edits)
                if (isRestTimerRunning && !isEditMode) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFE0B2) // Light orange background
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rest Timer: $restTimeFormatted",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFE65100) // Orange text
                            )
                        }
                    }
                }

                // Title - different for edit vs new entry
                Text(
                    text = if (isEditMode) "Edit Set $setNumber" else "Set $setNumber Complete!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                // 🆕 NEW: Show current values prominently in edit mode
                if (isEditMode && (initialReps > 0 || initialWeight > 0f)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF3E5F5) // Light purple background
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Current Values:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF7B1FA2) // Purple text
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = buildString {
                                    if (initialReps > 0) append("$initialReps reps")
                                    if (initialReps > 0 && initialWeight > 0f) append(" × ")
                                    if (initialWeight > 0f) append("${initialWeight}lbs")
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7B1FA2)
                            )
                        }
                    }
                }

                // Exercise name
                Text(
                    text = exerciseName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                // Input fields
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Reps performed field
                    OutlinedTextField(
                        value = repsText,
                        onValueChange = {
                            repsText = it
                            repsError = false
                        },
                        label = { Text("Reps Performed *") },
                        placeholder = { Text("e.g., 12") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = repsError,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryCoral,
                            cursorColor = PrimaryCoral
                        ),
                        supportingText = if (repsError) {
                            { Text("Please enter a valid number of reps", color = MaterialTheme.colorScheme.error) }
                        } else null
                    )

                    // Weight used field
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = {
                            weightText = it
                            weightError = false
                        },
                        label = { Text("Weight Used (lbs) *") },
                        placeholder = { Text("e.g., 135.5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = weightError,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryCoral,
                            cursorColor = PrimaryCoral
                        ),
                        supportingText = if (weightError) {
                            { Text("Please enter a valid weight", color = MaterialTheme.colorScheme.error) }
                        } else null
                    )
                }

                // Required fields note
                Text(
                    text = "* Required fields",
                    fontSize = 12.sp,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                )

                // 🆕 NEW: Action buttons - different layout for edit vs new entry
                if (isEditMode && onCancel != null) {
                    // Edit mode: Show both Cancel and Update buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel button
                        OutlinedButton(
                            onClick = {
                                println("🎯 DIALOG: Cancel button clicked in edit mode")
                                onCancel()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PrimaryCoral
                            )
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Update button
                        Button(
                            onClick = {
                                println("🎯 DIALOG: Update button clicked - reps: '$repsText', weight: '$weightText'")

                                // Validate inputs
                                val reps = repsText.toIntOrNull()
                                val weight = weightText.toFloatOrNull()

                                when {
                                    reps == null || reps <= 0 -> {
                                        repsError = true
                                        println("🎯 DIALOG: Reps validation failed")
                                    }
                                    weight == null || weight < 0 -> {
                                        weightError = true
                                        println("🎯 DIALOG: Weight validation failed")
                                    }
                                    else -> {
                                        println("🎯 DIALOG: Validation passed - updating data")
                                        val data = SetPerformanceData(
                                            repsPerformed = reps,
                                            weightUsed = weight
                                        )
                                        onDataEntered(data)
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryCoral
                            )
                        ) {
                            Text(
                                text = "Update",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    // New entry mode: Show only ADD button (no cancel)
                    Button(
                        onClick = {
                            println("🎯 DIALOG: ADD button clicked - reps: '$repsText', weight: '$weightText'")

                            // Validate inputs
                            val reps = repsText.toIntOrNull()
                            val weight = weightText.toFloatOrNull()

                            when {
                                reps == null || reps <= 0 -> {
                                    repsError = true
                                    println("🎯 DIALOG: Reps validation failed")
                                }
                                weight == null || weight < 0 -> {
                                    weightError = true
                                    println("🎯 DIALOG: Weight validation failed")
                                }
                                else -> {
                                    println("🎯 DIALOG: Validation passed - submitting data")
                                    val data = SetPerformanceData(
                                        repsPerformed = reps,
                                        weightUsed = weight
                                    )
                                    onDataEntered(data)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryCoral
                        )
                    ) {
                        Text(
                            text = "ADD",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
