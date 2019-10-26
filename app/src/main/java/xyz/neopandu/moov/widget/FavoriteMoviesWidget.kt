package xyz.neopandu.moov.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import android.widget.Toast
import xyz.neopandu.moov.R
import xyz.neopandu.moov.data.database.DBContract.MovieColumns.Companion.MOVIE_TYPE
import xyz.neopandu.moov.flow.detail.DetailActivity
import xyz.neopandu.moov.models.Movie
import xyz.neopandu.moov.widget.StackWidgetService.Companion.EXTRA_ITEM
import xyz.neopandu.moov.widget.StackWidgetService.Companion.TOAST_ACTION

class FavoriteMoviesWidget : AppWidgetProvider() {

    companion object {
        @JvmStatic
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val intent = Intent(context, StackWidgetService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.putExtra(MOVIE_TYPE, Movie.MovieType.MOVIE.name)
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))

            val views = RemoteViews(context.packageName, R.layout.favorite_movie_banners_widget)
            views.setRemoteAdapter(R.id.stack_view, intent)
            views.setEmptyView(R.id.stack_view, R.id.empty_view)

            views.setTextViewText(R.id.banner_text, context.getText(R.string.favorite_movies))

            val toastIntent = Intent(context, FavoriteMoviesWidget::class.java)
            toastIntent.action = TOAST_ACTION
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))

            val toastPendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    0,
                    toastIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            views.setPendingIntentTemplate(R.id.stack_view, toastPendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        // There may be multiple widgets active, so update all of them
        if (appWidgetIds != null && context != null && appWidgetManager != null) {
            Toast.makeText(
                context,
                "doing onUpdate ${appWidgetIds.joinToString()}",
                Toast.LENGTH_SHORT
            ).show()
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action != null) {
            if (intent.action.equals(TOAST_ACTION)) {
                val selectedMovie = intent.getBundleExtra(EXTRA_ITEM)
                context?.startActivity(
                    Intent(context, DetailActivity::class.java)
                        .putExtra(EXTRA_ITEM, selectedMovie)
                )
//                Toast.makeText(context, "$selectedMovie clicked", Toast.LENGTH_SHORT).show()
            }
        }
    }
}