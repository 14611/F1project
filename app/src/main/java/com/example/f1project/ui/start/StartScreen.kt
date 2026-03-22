package com.example.f1project.ui.start

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.f1project.ui.theme.F1Dimens
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(viewModel: StartViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var isVisible by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(pullRefreshState.isRefreshing) {
        if (pullRefreshState.isRefreshing) viewModel.refresh()
    }
    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing) pullRefreshState.endRefresh()
    }
    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(F1Dimens.spacingL)
            .nestedScroll(pullRefreshState.nestedScrollConnection),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            uiState.error != null -> Text(
                text = "Błąd danych:\n${uiState.error}",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            else -> AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    uiState.nextRaceSession?.let { race ->
                        Text(
                            text = "NASTĘPNY WYŚCIG",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = race.raceName,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(
                                horizontal = F1Dimens.spacingL,
                                vertical = F1Dimens.spacingS
                            )
                        )
                        CountdownDisplay(uiState.raceCountdown)
                    }

                    Spacer(modifier = Modifier.height(F1Dimens.spacingXxl + F1Dimens.spacingL))

                    uiState.nextUpcomingSession?.let { session ->
                        if (session.sessionName != "Wyścig") {
                            Text(
                                text = "NAJBLIŻSZA SESJA: ${session.sessionName.uppercase()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(F1Dimens.spacingS))
                            Text(
                                text = uiState.sessionCountdown,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
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

@Composable
fun CountdownDisplay(countdown: String) {
    val parts = countdown.split(" ")
    if (parts.size < 2) {
        Text(
            text = countdown,
            style = MaterialTheme.typography.displayMedium
        )
        return
    }
    Row(horizontalArrangement = Arrangement.spacedBy(F1Dimens.spacingM)) {
        for (i in parts.indices step 2) {
            if (i + 1 < parts.size) {
                CountdownUnit(value = parts[i], label = parts[i + 1])
            }
        }
    }
}

@Composable
fun CountdownUnit(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}