package com.example.offlinepplworkoutapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.offlinepplworkoutapp.data.database.PPLWorkoutDatabase
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun DebugDaySelector(
    onDaySelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🔧 Debug Day Selector",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn {
                item {
                    Text(
                        text = "Select a day to test different workouts:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                val days = listOf(
                    Calendar.MONDAY to "Monday - Push Day 1 💪",
                    Calendar.TUESDAY to "Tuesday - Pull Day 1 🏋️",
                    Calendar.WEDNESDAY to "Wednesday - Legs Day 1 🦵",
                    Calendar.THURSDAY to "Thursday - Push Day 2 💪",
                    Calendar.FRIDAY to "Friday - Pull Day 2 🏋️",
                    Calendar.SATURDAY to "Saturday - Legs Day 2 🦵",
                    Calendar.SUNDAY to "Sunday - Rest Day 🧘‍♂️"
                )

                items(days) { (dayOfWeek, dayLabel) ->
                    OutlinedButton(
                        onClick = { onDaySelected(dayOfWeek) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (dayOfWeek == Calendar.SUNDAY)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = dayLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Debug Actions:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                item {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                println("🔍 DEBUG: Checking current database state...")
                                val currentState = PPLWorkoutDatabase.verifyDatabaseEmpty()
                                println("📊 CURRENT STATE: ${currentState.first} days, ${currentState.second} entries, ${currentState.third} sets")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text("🔍 Check DB State")
                    }
                }

                item {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                println("🧨 DEBUG: Force reset database...")
                                PPLWorkoutDatabase.forceResetDatabase(context)
                                kotlinx.coroutines.delay(200)
                                val afterState = PPLWorkoutDatabase.verifyDatabaseEmpty()
                                println("📊 AFTER FORCE RESET: ${afterState.first} days, ${afterState.second} entries, ${afterState.third} sets")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("🧨 Force Reset DB")
                    }
                }
            }
        },
        confirmButton = {
            //TODO: Uncomment when reset functionality is implemented
//            TextButton(onClick = onResetToToday) {
//                Text("Reset to Today")
//            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
