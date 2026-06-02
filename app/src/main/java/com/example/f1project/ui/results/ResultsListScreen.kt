package com.example.f1project.ui.results

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.f1project.F1App
import com.example.f1project.data.remote.Race
import com.example.f1project.ui.calendar.CacheBanner
import com.example.f1project.ui.season.SeasonTopBar
import com.example.f1project.ui.season.SeasonViewModel
import com.example.f1project.ui.theme.F1Dimens


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsListScreen(
    seasonViewModel: SeasonViewModel,
    onSessionClick: (season: String, round: String, sessionType: String, location: String) -> Unit,
    viewModel: ResultsListViewModel = viewModel(
        factory = ResultsListViewModel.factory(
            (LocalContext.current.applicationContext as F1App).repository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedSeason by seasonViewModel.selectedSeason.collectAsState()
    var isVisible by rememberSaveable { mutableStateOf(false) }
    var expandedRaceRound by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedSeason) {
        isVisible = false
        expandedRaceRound = null
        viewModel.loadSeason(selectedSeason)
    }

    val pullRefreshState = rememberPullToRefreshState()
    LaunchedEffect(pullRefreshState.isRefreshing) {
        if (pullRefreshState.isRefreshing) viewModel.refresh(selectedSeason)
    }
    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing) pullRefreshState.endRefresh()
    }
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) isVisible = true
    }

    Scaffold(
        topBar = {
            SeasonTopBar(
                selectedSeason = selectedSeason,
                availableSeasons = seasonViewModel.availableSeasons,
                onSeasonSelected = { seasonViewModel.selectSeason(it) },
                title = "Wyniki"
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(pullRefreshState.nestedScrollConnection),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
                uiState.error != null -> Text(
                    text = uiState.error ?: "Nieznany błąd",
                    color = MaterialTheme.colorScheme.error
                )
                else -> AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
                ) {
                    Column {
                        if (uiState.isFromCache) CacheBanner()
                        LazyColumn(
                            contentPadding = PaddingValues(
                                horizontal = F1Dimens.listPaddingH,
                                vertical = F1Dimens.listPaddingV
                            ),
                            verticalArrangement = Arrangement.spacedBy(F1Dimens.listItemSpacing)
                        ) {
                            items(uiState.finishedRaces, key = { it.round }) { race ->
                                ResultRaceCard(
                                    race = race,
                                    isExpanded = expandedRaceRound == race.round,
                                    onHeaderClick = {
                                        expandedRaceRound =
                                            if (expandedRaceRound == race.round) null
                                            else race.round
                                    },
                                    onSessionClick = onSessionClick
                                )
                            }
                        }
                    }
                }
            }
            PullToRefreshContainer(
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ResultRaceCard(
    race: Race,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    onSessionClick: (season: String, round: String, sessionType: String, location: String) -> Unit
) {
    val availableSessions = rememberSaveable(race) {
        buildList {
            add(SessionType.QUALIFYING)
            if (race.sprintQualifying != null) add(SessionType.SPRINT_QUALIFYING)
            if (race.sprint != null) add(SessionType.SPRINT)
            add(SessionType.RACE)
        }.sortedBy { it.ordinal }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(F1Dimens.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = F1Dimens.cardElevation)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .clickable { onHeaderClick() }
                    .padding(
                        horizontal = F1Dimens.cardPaddingH,
                        vertical = F1Dimens.cardPaddingV
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(F1Dimens.spacingM)
            ) {
                // Numer rundy — okrągły badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = race.round,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Nazwa + kraj
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = race.raceName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = race.circuit.location.country,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = F1Dimens.spacingXs)
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess
                    else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Zwiń" else "Rozwiń",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(F1Dimens.iconSize)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        thickness = F1Dimens.dividerThickness
                    )
                    Column(
                        modifier = Modifier.padding(
                            horizontal = F1Dimens.cardPaddingH,
                            vertical = F1Dimens.spacingM
                        ),
                        verticalArrangement = Arrangement.spacedBy(F1Dimens.spacingS)
                    ) {
                        availableSessions.forEach { session ->
                            val isRace = session == SessionType.RACE
                            Button(
                                onClick = {
                                    onSessionClick(
                                        race.season,
                                        race.round,
                                        session.key,
                                        race.circuit.location.locality
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(F1Dimens.cardCornerRadiusS),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRace) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                                    contentColor = if (isRace) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = if (isRace) F1Dimens.cardElevation else 0.dp
                                )
                            ) {
                                Text(
                                    text = session.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isRace) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class SessionType(val key: String, val displayName: String) {
    SPRINT_QUALIFYING("sprint_qualifying", "Kwalifikacje do Sprintu"),
    SPRINT("sprint", "Sprint"),
    QUALIFYING("qualifying", "Kwalifikacje"),
    RACE("race", "Wyścig")
}