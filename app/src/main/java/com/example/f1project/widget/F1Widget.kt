package com.example.f1project.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.f1project.MainActivity

class F1Widget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> =
        PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val isLoading = prefs[F1WidgetKeys.KEY_IS_LOADING] == "true"

            if (isLoading) {
                LoadingContent()
            } else {
                WidgetMainContent(
                    sessionName = prefs[F1WidgetKeys.KEY_SESSION_NAME] ?: "",
                    sessionRaceName = prefs[F1WidgetKeys.KEY_SESSION_RACE_NAME] ?: "",
                    sessionDate = prefs[F1WidgetKeys.KEY_SESSION_DATE] ?: "—",
                    sessionTime = prefs[F1WidgetKeys.KEY_SESSION_TIME] ?: "—",
                    raceName = prefs[F1WidgetKeys.KEY_RACE_NAME] ?: "",
                    raceDate = prefs[F1WidgetKeys.KEY_RACE_DATE] ?: "—",
                    raceTime = prefs[F1WidgetKeys.KEY_RACE_TIME] ?: "—"
                )
            }
        }
    }
}

@Composable
fun LoadingContent() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF15151E)))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Ładowanie...",
            style = TextStyle(
                color = ColorProvider(Color(0xFFB3B3B3)),
                fontSize = 12.sp
            )
        )
    }
}

@Composable
fun WidgetMainContent(
    sessionName: String,
    sessionRaceName: String,
    sessionDate: String,
    sessionTime: String,
    raceName: String,
    raceDate: String,
    raceTime: String
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF15151E)))
            .clickable(actionStartActivity<MainActivity>())
            .padding(12.dp)
    ) {
        // Nagłówek
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "F1",
                style = TextStyle(
                    color = ColorProvider(Color(0xFFE10600)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            Text(
                text = "Następne wydarzenie",
                style = TextStyle(
                    color = ColorProvider(Color(0xFFB3B3B3)),
                    fontSize = 11.sp
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(10.dp))

        // Blok: najbliższa sesja
        SessionBlock(
            label = sessionName.ifBlank { "Sesja" },
            raceName = sessionRaceName,
            date = sessionDate,
            time = sessionTime,
            accentColor = Color(0xFF00D2BE)
        )

        Spacer(modifier = GlanceModifier.height(10.dp))

        // Separator
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ColorProvider(Color(0xFF303038)))
        ) {}

        Spacer(modifier = GlanceModifier.height(10.dp))

        // Blok: wyścig główny
        SessionBlock(
            label = "Wyścig",
            raceName = raceName,
            date = raceDate,
            time = raceTime,
            accentColor = Color(0xFFE10600)
        )
    }
}

@Composable
fun SessionBlock(
    label: String,
    raceName: String,
    date: String,
    time: String,
    accentColor: Color
) {
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        // Etykieta z kolorowym paskiem
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = GlanceModifier
                    .width(3.dp)
                    .height(14.dp)
                    .background(ColorProvider(accentColor))
            ) {}
            Spacer(modifier = GlanceModifier.width(6.dp))
            Text(
                text = label.uppercase(),
                style = TextStyle(
                    color = ColorProvider(accentColor),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(3.dp))

        // Nazwa wyścigu
        if (raceName.isNotBlank()) {
            Text(
                text = raceName,
                style = TextStyle(
                    color = ColorProvider(Color(0xFFFFFFFF)),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
        }

        // Data i godzina w jednym wierszu
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = date,
                style = TextStyle(
                    color = ColorProvider(Color(0xFFFFFFFF)),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = time,
                style = TextStyle(
                    color = ColorProvider(accentColor),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}