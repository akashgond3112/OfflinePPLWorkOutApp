package com.example.offlinepplworkoutapp.ui.components

import androidx.compose.foundation.layout.*
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
    isRestTimerRunning: Boolean = false,
    restTimeFormatted: String = "00:00"
) {
    var repsText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var repsError by remember { mutableStateOf(false) }
    var weightError by remember { mutableStateOf(false) }

    println("🎯 DIALOG: SetDataEntryDialog opened for set $setNumber of $exerciseName")

    Dialog(
        onDismissRequest = {
            // No dismiss on outside click - user must enter data
            println("🎯 DIALOG: Dismiss request blocked - data entry required")
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
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
                // Header with rest timer indicator
                if (isRestTimerRunning) {
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

                // Title
                Text(
                    text = "Set $setNumber Complete!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

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

                // ADD button (no cancel button as per requirements)
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
