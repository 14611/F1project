package com.example.f1project.ui.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.f1project.F1App
import com.example.f1project.domain.model.DomainRace
import com.example.f1project.ui.season.SeasonTopBar
import com.example.f1project.ui.season.SeasonViewModel
import com.example.f1project.ui.theme.F1Dimens
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    seasonViewModel: SeasonViewModel,
    viewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModel.factory(
            (LocalContext.current.applicationContext as F1App).repository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedSeason by seasonViewModel.selectedSeason.collectAsState()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(selectedSeason) {
        isVisible = false
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
        if (!uiState.isLoading) {
            delay(200)
            isVisible = true
        }
    }

    Scaffold(
        topBar = {
            SeasonTopBar(
                selectedSeason = selectedSeason,
                availableSeasons = seasonViewModel.availableSeasons,
                onSeasonSelected = { seasonViewModel.selectSeason(it) },
                title = "Kalendarz"
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
                    text = uiState.error!!,
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
                            verticalArrangement = Arrangement.spacedBy(F1Dimens.spacingL)
                        ) {
                            items(uiState.races, key = { it.round }) { race ->
                                RaceCard(race = race)
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
fun CacheBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Text(
            text = "Brak połączenia — wyświetlam ostatnio zapisane dane",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(
                horizontal = F1Dimens.spacingL,
                vertical = F1Dimens.spacingS
            )
        )
    }
}

@Composable
fun RaceCard(race: DomainRace) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                text = "RUNDA ${race.round}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = race.name,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = F1Dimens.spacingXs)
            )
            Text(
                text = "${race.circuitName}, ${race.country}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = F1Dimens.spacingXs)
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = F1Dimens.spacingL),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                thickness = F1Dimens.dividerThickness
            )
            race.sessions.forEach { session ->
                SessionInfo(
                    name = session.name,
                    date = session.date,
                    time = session.time,
                    isRace = session.isRace
                )
            }
        }
    }
}

@Composable
fun SessionInfo(
    name: String,
    date: String?,
    time: String?,
    isRace: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = F1Dimens.spacingXs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isRace) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = formatUtcDateTime(date, time),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isRace) FontWeight.Bold else FontWeight.Normal,
            color = if (isRace) MaterialTheme.colorScheme.primary
            else LocalContentColor.current
        )
    }
}