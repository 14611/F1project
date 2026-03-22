package com.example.f1project.ui.season

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonTopBar(
    selectedSeason: String,
    availableSeasons: List<String>,
    onSeasonSelected: (String) -> Unit,
    title: String
) {
    var expanded by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            Text(title, style = MaterialTheme.typography.headlineMedium)
        },
        actions = {
            Box {
                // Przycisk z aktualnym rokiem i strzałką
                TextButton(onClick = { expanded = true }) {
                    Text(
                        text = selectedSeason,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Wybierz sezon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Dropdown z listą lat
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    availableSeasons.forEach { season ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = season,
                                    color = if (season == selectedSeason)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onSeasonSelected(season)
                                expanded = false
                            }
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}