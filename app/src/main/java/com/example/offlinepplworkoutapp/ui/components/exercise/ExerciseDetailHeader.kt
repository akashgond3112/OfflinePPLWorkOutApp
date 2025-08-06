package com.example.offlinepplworkoutapp.ui.components.exercise

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.offlinepplworkoutapp.ui.theme.CardBackground
import com.example.offlinepplworkoutapp.ui.theme.PrimaryCoral
import com.example.offlinepplworkoutapp.ui.theme.TextSecondary
import com.example.offlinepplworkoutapp.utils.ExerciseDetailUtils.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailHeader(
    exerciseName: String,
    completedSets: Int,
    totalSets: Int,
    totalTime: Long,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryCoral
                    )
                )
                Text(
                    text = "Sets: $completedSets/$totalSets • Total: ${formatTime(totalTime / 1000)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PrimaryCoral
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CardBackground
        )
    )
}