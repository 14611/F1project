package com.example.f1project.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.f1project.domain.model.DomainDriverProfile
import com.example.f1project.domain.model.DomainRaceResult
import com.example.f1project.ui.theme.F1Dimens
import com.example.f1project.ui.theme.PodiumColors
import com.example.f1project.ui.theme.getFlag
import com.example.f1project.ui.theme.teamColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: DriverProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.profile

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = (profile?.driver?.fullName ?: "Kierowca").uppercase(),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> Box(
                Modifier.fillMaxSize(), Alignment.Center
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
            uiState.error != null -> Box(
                Modifier.fillMaxSize().padding(padding), Alignment.Center
            ) { Text(uiState.error!!, color = MaterialTheme.colorScheme.error) }
            profile == null -> Box(
                Modifier.fillMaxSize(), Alignment.Center
            ) { Text("Brak danych", color = MaterialTheme.colorScheme.error) }
            else -> LazyColumn(
                contentPadding = PaddingValues(
                    start = F1Dimens.listPaddingH,
                    end = F1Dimens.listPaddingH,
                    top = padding.calculateTopPadding() + F1Dimens.spacingS,
                    bottom = F1Dimens.spacingXl
                ),
                verticalArrangement = Arrangement.spacedBy(F1Dimens.spacingM)
            ) {
                item { DriverInfoCard(profile = profile) }
                item {
                    SeasonStatsCard(
                        points = profile.seasonStats.points.toInt(),
                        wins = profile.seasonStats.wins,
                        podiums = profile.seasonStats.podiums
                    )
                }
                item {
                    Text(
                        text = "WYNIKI WYŚCIGÓW",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = F1Dimens.spacingS)
                    )
                }
                items(profile.raceResults) { result ->
                    DriverRaceResultItem(result = result)
                }
            }
        }
    }
}

@Composable
fun DriverInfoCard(profile: DomainDriverProfile) {
    val teamColor = teamColors.entries
        .firstOrNull { profile.currentTeam.lowercase().contains(it.key) }
        ?.value ?: MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(F1Dimens.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = F1Dimens.cardElevation)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = F1Dimens.cardPaddingH,
                vertical = F1Dimens.cardPaddingV
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val flag = getFlag(profile.driver.nationality)
                    Text(
                        text = "$flag ${profile.driver.nationality}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (profile.currentTeam.isNotBlank()) {
                        Text(
                            text = profile.currentTeam.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = teamColor,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(top = F1Dimens.spacingXs)
                        )
                    }
                }
                if (profile.driver.number.isNotBlank()) {
                    Text(
                        text = "#${profile.driver.number}",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (profile.driver.dateOfBirth.isNotBlank()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = F1Dimens.spacingM),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    thickness = F1Dimens.dividerThickness
                )
                InfoRow(
                    label = "Data urodzenia",
                    value = formatDate(profile.driver.dateOfBirth)
                )
            }
        }
    }
}

@Composable
fun SeasonStatsCard(points: Int, wins: Int, podiums: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(F1Dimens.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = F1Dimens.cardElevation)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = F1Dimens.cardPaddingH,
                vertical = F1Dimens.cardPaddingV
            )
        ) {
            Text(
                text = "SEZON",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = F1Dimens.spacingM)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(value = points.toString(), label = "Punkty")
                StatItem(value = wins.toString(), label = "Zwycięstwa")
                StatItem(value = podiums.toString(), label = "Podium")
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DriverRaceResultItem(result: DomainRaceResult) {
    val podiumColor = PodiumColors.forPosition(result.position)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(F1Dimens.cardCornerRadius),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (podiumColor != null) F1Dimens.cardElevationHigh
            else F1Dimens.cardElevation
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            if (podiumColor != null) {
                Box(
                    modifier = Modifier
                        .width(F1Dimens.teamBarWidth)
                        .fillMaxHeight()
                        .background(podiumColor)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = F1Dimens.cardPaddingH,
                        vertical = F1Dimens.spacingM
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "P${result.position}",
                    style = MaterialTheme.typography.titleLarge,
                    color = podiumColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (podiumColor != null) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(end = F1Dimens.spacingL)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Runda ${result.round}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = result.raceName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = result.status,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (result.points > 0) {
                    Text(
                        text = "+${result.points.toInt()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

fun formatDate(dateString: String): String {
    return try {
        val parts = dateString.split("-")
        if (parts.size != 3) return dateString
        val months = listOf(
            "stycznia", "lutego", "marca", "kwietnia", "maja", "czerwca",
            "lipca", "sierpnia", "września", "października", "listopada", "grudnia"
        )
        "${ parts[2].toInt() } ${ months[parts[1].toInt() - 1] } ${ parts[0] }"
    } catch (e: Exception) { dateString }
}