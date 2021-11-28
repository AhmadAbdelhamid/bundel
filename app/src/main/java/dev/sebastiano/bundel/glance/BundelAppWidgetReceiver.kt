package dev.sebastiano.bundel.glance

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionCallback
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionRunCallback
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dev.sebastiano.bundel.glance.BundelAppWidgetReceiver.Companion.updateWidgets
import dev.sebastiano.bundel.notifications.BundelNotificationListenerService
import dev.sebastiano.bundel.ui.BundelGlanceTheme
import kotlinx.coroutines.flow.first
import timber.log.Timber

class BundelAppWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = CannoliWidget(numberOfItems = null)

    private class CannoliWidget(
        val numberOfItems: Int?
    ) : GlanceAppWidget() {

        override val sizeMode = SizeMode.Responsive(
            setOf(DpSize(36.dp, 36.dp), DpSize(100.dp, 96.dp))
        )

        @Composable
        override fun Content() {
            BundelGlanceTheme {
                Box(
                    modifier = GlanceModifier.background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable(actionRunCallback<MustBeTopLevelBecauseReasonsCallbackClassApi>())
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (numberOfItems == null) {
                        val context = LocalContext.current
                        LaunchedEffect(Unit) {
                            val notificationsCount = BundelNotificationListenerService.activeNotificationsFlow.first().size
                            context.updateWidgets(notificationsCount)
                        }
                    }

                    val text = when {
                        numberOfItems == null -> "⏳"
                        numberOfItems > 0 -> numberOfItems.toString()
                        else -> "💩"
                    }

                    val size = LocalSize.current
                    Timber.d("Size: $size")
                    if (size.width >= 100.dp && size.height >= 96.dp) {
                        Column(
                            modifier = GlanceModifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "I'm big whoa")
                            Spacer(modifier = GlanceModifier.height(16.dp))
                            Text(
                                text = text,
                                style = TextStyle(
                                    color = ColorProvider(MaterialTheme.colorScheme.onPrimaryContainer),
                                    fontSize = MaterialTheme.typography.displayMedium.fontSize
                                )
                            )
                        }
                    } else {
                        Text(
                            text = text,
                            style = TextStyle(
                                color = ColorProvider(MaterialTheme.colorScheme.onPrimaryContainer),
                                fontSize = MaterialTheme.typography.displayMedium.fontSize
                            )
                        )
                    }
                }
            }
        }
    }

    companion object {

        internal suspend fun Context.updateWidgets(notificationsCount: Int?) {
            val manager = GlanceAppWidgetManager(this)

            Timber.i("Updating widget. Count: $notificationsCount")

            manager.getGlanceIds(CannoliWidget::class.java)
                .forEach { id -> CannoliWidget(notificationsCount).update(this, id) }
        }
    }
}

class MustBeTopLevelBecauseReasonsCallbackClassApi : ActionCallback {

    override suspend fun onRun(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val notificationsCount = BundelNotificationListenerService.activeNotificationsFlow.first().size
        context.updateWidgets(notificationsCount)
    }
}