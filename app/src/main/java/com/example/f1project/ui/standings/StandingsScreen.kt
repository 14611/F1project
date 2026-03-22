package com.example.f1project.ui.standings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.f1project.F1App
import com.example.f1project.domain.model.DomainConstructorStanding
import com.example.f1project.domain.model.DomainDriverStanding
import com.example.f1project.ui.calendar.CacheBanner
import com.example.f1project.ui.season.SeasonTopBar
import com.example.f1project.ui.season.SeasonViewModel
import com.example.f1project.ui.theme.F1Dimens
import com.example.f1project.ui.theme.getFlag
import com.example.f1project.ui.theme.teamColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StandingsScreen(
    seasonViewModel: SeasonViewModel,
    onDriverClick: (driverId: String) -> Unit = {},
    onConstructorClick: (constructorId: String) -> Unit = {},
    viewModel: StandingsViewModel = viewModel(
        factory = StandingsViewModel.factory(
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
                title = "Klasyfikacja"
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
                        StandingsList(
                            drivers = uiState.drivers,
                            constructors = uiState.constructors,
                            season = selectedSeason,
                            onDriverClick = onDriverClick,
                            onConstructorClick = onConstructorClick
                        )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StandingsList(
    drivers: List<DomainDriverStanding>,
    constructors: List<DomainConstructorStanding>,
    season: String,
    onDriverClick: (driverId: String) -> Unit = {},
    onConstructorClick: (constructorId: String) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = F1Dimens.listPaddingH,
            vertical = F1Dimens.listPaddingV
        ),
        verticalArrangement = Arrangement.spacedBy(F1Dimens.listItemSpacing)
    ) {
        item {
            Text(
                text = "KIEROWCY $season",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = F1Dimens.spacingS)
            )
        }
        itemsIndexed(
            drivers,
            key = { _, item -> "driver_${item.driver.driverId}" }
        ) { _, driverStanding ->
            DriverStandingItem(
                standing = driverStanding,
                modifier = Modifier.animateItemPlacement(),
                onClick = { onDriverClick(driverStanding.driver.driverId) }
            )
        }
        item {
            Spacer(modifier = Modifier.height(F1Dimens.spacingXl))
            Text(
                text = "KONSTRUKTORZY $season",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = F1Dimens.spacingS)
            )
        }
        itemsIndexed(
            constructors,
            key = { _, item -> "constructor_${item.constructor.constructorId}" }
        ) { _, constructorStanding ->
            ConstructorStandingItem(
                standing = constructorStanding,
                modifier = Modifier.animateItemPlacement(),
                onClick = { onConstructorClick(constructorStanding.constructor.constructorId) }
            )
        }
    }
}

@Composable
fun DriverStandingItem(
    standing: DomainDriverStanding,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val teamColor = standing.constructor?.let {
        teamColors[it.constructorId]
    } ?: Color.Gray
    val flag = getFlag(standing.driver.nationality)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(F1Dimens.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = F1Dimens.cardElevation)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(F1Dimens.teamBarWidth)
                    .fillMaxHeight()
                    .background(teamColor)
            )
            Text(
                text = standing.position.toString(),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = F1Dimens.spacingL),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = F1Dimens.spacingM)
            ) {
                Text(
                    text = "$flag ${standing.driver.fullName}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = standing.constructor?.name ?: "Brak zespołu",
                    style = MaterialTheme.typography.bodySmall,
                    color = teamColor.copy(alpha = 0.85f),
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(top = F1Dimens.spacingXs)
                )
            }
            Text(
                text = "${standing.points.toInt()} pkt",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = F1Dimens.spacingL),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ConstructorStandingItem(
    standing: DomainConstructorStanding,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val teamColor = teamColors[standing.constructor.constructorId] ?: Color.Gray
    val flag = getFlag(standing.constructor.nationality)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(F1Dimens.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = F1Dimens.cardElevation)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(F1Dimens.teamBarWidth)
                    .fillMaxHeight()
                    .background(teamColor)
            )
            Text(
                text = standing.position.toString(),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = F1Dimens.spacingL),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$flag ${standing.constructor.name}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = F1Dimens.spacingM)
            )
            Text(
                text = "${standing.points.toInt()} pkt",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = F1Dimens.spacingL),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}