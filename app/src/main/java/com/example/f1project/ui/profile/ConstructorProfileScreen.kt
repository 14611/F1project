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
import com.example.f1project.domain.model.DomainConstructorProfile
import com.example.f1project.domain.model.DomainConstructorRaceResult
import com.example.f1project.domain.model.DomainDriverResult
import com.example.f1project.ui.theme.F1Dimens
import com.example.f1project.ui.theme.PodiumColors
import com.example.f1project.ui.theme.getFlag
import com.example.f1project.ui.theme.teamColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstructorProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ConstructorProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.profile

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = (profile?.constructor?.name ?: "Zespół").uppercase(),
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
                item { ConstructorInfoCard(profile = profile) }
                item {
                    SeasonStatsCard(
                        points = profile.seasonStats.points,
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
                    ConstructorRaceResultItem(result = result)
                }
            }
        }
    }
}

@Composable
fun ConstructorInfoCard(profile: DomainConstructorProfile) {
    val teamColor = teamColors[profile.constructor.constructorId]
        ?: MaterialTheme.colorScheme.primary
    val flag = getFlag(profile.constructor.nationality)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(F1Dimens.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = F1Dimens.cardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(F1Dimens.teamBarWidth)
                    .fillMaxHeight()
                    .background(teamColor)
            )
            Column(
                modifier = Modifier.padding(
                    horizontal = F1Dimens.cardPaddingH,
                    vertical = F1Dimens.cardPaddingV
                )
            ) {
                Text(
                    text = "$flag ${profile.constructor.nationality}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = profile.constructor.name.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = teamColor,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(top = F1Dimens.spacingXs)
                )
            }
        }
    }
}

@Composable
fun ConstructorRaceResultItem(result: DomainConstructorRaceResult) {
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
                text = "Runda ${result.round} — ${result.raceName}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = F1Dimens.spacingS)
            )
            HorizontalDivider(
                modifier = Modifier.padding(bottom = F1Dimens.spacingS),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                thickness = F1Dimens.dividerThickness
            )
            ConstructorDriverResultRow(driverResult = result.driver1)
            result.driver2?.let { ConstructorDriverResultRow(driverResult = it) }
        }
    }
}

@Composable
fun ConstructorDriverResultRow(driverResult: DomainDriverResult) {
    val podiumColor = PodiumColors.forPosition(driverResult.position)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = F1Dimens.spacingXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "P${driverResult.position}",
            style = MaterialTheme.typography.titleMedium,
            color = podiumColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (podiumColor != null) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(end = F1Dimens.spacingM)
        )
        Text(
            text = driverResult.fullName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (driverResult.points > 0) {
            Text(
                text = "+${driverResult.points.toInt()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}