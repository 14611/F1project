package com.example.f1project.ui.results

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.f1project.F1App
import com.example.f1project.domain.model.DomainResult
import com.example.f1project.ui.theme.F1Dimens
import com.example.f1project.ui.theme.PodiumColors
import com.example.f1project.ui.theme.getFlag
import com.example.f1project.ui.theme.teamColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ResultsViewModel = viewModel(
        factory = ResultsViewModel.Factory(
            repository = (LocalContext.current.applicationContext as F1App).repository,
            openF1Repository = (LocalContext.current.applicationContext as F1App).openF1Repository,
            owner = LocalSavedStateRegistryOwner.current
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    val isQualifying = remember(uiState.results) {
        uiState.results.any { it.q2 != null }
    }
    val isSprintQualifying = remember(uiState.title) {
        uiState.title.contains("Sprint Qualifying", ignoreCase = true)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = uiState.title.uppercase(),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wróć",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
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
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            uiState.error != null -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = F1Dimens.spacingXl)
                )
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(
                    horizontal = F1Dimens.listPaddingH,
                    vertical = F1Dimens.listPaddingV
                ),
                verticalArrangement = Arrangement.spacedBy(F1Dimens.listItemSpacing),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(uiState.results) { result ->
                    ResultItem(
                        result = result,
                        isQualifying = isQualifying,
                        isSprintQualifying = isSprintQualifying
                    )
                }
            }
        }
    }
}

@Composable
fun ResultItem(
    result: DomainResult,
    isQualifying: Boolean,
    isSprintQualifying: Boolean = false
) {
    val teamColor = teamColors[result.constructorId]
        ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    val podiumColor = PodiumColors.forPosition(result.position)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(F1Dimens.cardCornerRadius),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Box(
                    modifier = Modifier
                        .width(F1Dimens.teamBarWidth)
                        .fillMaxHeight()
                        .background(teamColor)
                )
                Row(
                    modifier = Modifier
                        .padding(
                            horizontal = F1Dimens.cardPaddingH,
                            vertical = F1Dimens.cardPaddingV
                        )
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = result.position.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = podiumColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(F1Dimens.positionColumnWidth)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        val flag = getFlag(result.nationality)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(F1Dimens.spacingS)
                        ) {
                            if (flag.isNotBlank()) {
                                Text(text = flag, fontSize = 14.sp)
                            }
                            Text(
                                text = result.driverFullName,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Text(
                            text = result.constructorName.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    result.points?.takeIf { it > 0.0 }?.let { pts ->
                        Text(
                            text = "+${pts.toInt()} PTS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            val primaryTime = if (isQualifying) result.q1 else result.timeOrStatus
            if (primaryTime != null) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.background,
                    thickness = F1Dimens.dividerThickness
                )
                if (isQualifying) {
                    QualifyingTimesRow(
                        result = result,
                        isSprintQualifying = isSprintQualifying
                    )
                } else {
                    Text(
                        text = primaryTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(
                            start = F1Dimens.timesRowStartPadding,
                            top = F1Dimens.spacingS,
                            bottom = F1Dimens.spacingS,
                            end = F1Dimens.spacingL
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun QualifyingTimesRow(
    result: DomainResult,
    isSprintQualifying: Boolean = false
) {
    val label1 = if (isSprintQualifying) "SQ1" else "Q1"
    val label2 = if (isSprintQualifying) "SQ2" else "Q2"
    val label3 = if (isSprintQualifying) "SQ3" else "Q3"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = F1Dimens.timesRowStartPadding,
                end = F1Dimens.spacingL,
                top = F1Dimens.spacingS,
                bottom = F1Dimens.spacingS
            ),
        horizontalArrangement = Arrangement.spacedBy(F1Dimens.spacingS)
    ) {
        TimeColumn(label = label1, time = result.q1, modifier = Modifier.weight(1f))
        TimeColumn(label = label2, time = result.q2, modifier = Modifier.weight(1f))
        TimeColumn(label = label3, time = result.q3, modifier = Modifier.weight(1f))
    }
}

@Composable
fun TimeColumn(label: String, time: String?, modifier: Modifier) {
    Column(modifier = modifier) {
        if (time != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = F1Dimens.spacingXs)
            )
        }
    }
}