package xyz.neopandu.moov.data.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.androidnetworking.error.ANError
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import xyz.neopandu.moov.R
import xyz.neopandu.moov.data.repository.FavoriteRepository
import xyz.neopandu.moov.data.repository.MovieRepository
import xyz.neopandu.moov.data.repository.ResponseListener
import xyz.neopandu.moov.data.repository.TVRepository
import xyz.neopandu.moov.flow.main.MainActivity
import xyz.neopandu.moov.models.Meta
import xyz.neopandu.moov.models.Movie

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TYPE = "type"

        const val CHANNEL_ID = "Reminder_Channel"
        const val CHANNEL_NAME = "Reminder channel"
    }

    private val movieRepository = MovieRepository()
    private val tvRepository = TVRepository()
    private var favoriteRepository: FavoriteRepository? = null

    enum class ReminderType(val id: Int) {
        DAILY_REMINDER(111), RELEASE_REMINDER(112), AIRING_TODAY_REMINDER(113)
    }

    // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
    override fun onReceive(context: Context, intent: Intent) {
        val rawType = intent.getIntExtra(EXTRA_TYPE, ReminderType.DAILY_REMINDER.id)
        val type = ReminderType.values().firstOrNull { it.id == rawType }

        if (type != null) {
            when (type) {
                ReminderType.DAILY_REMINDER -> processDailyReminder(context)
                ReminderType.RELEASE_REMINDER -> processReleaseTodayReminder(context)
                ReminderType.AIRING_TODAY_REMINDER ->
                    processAiringTodayReminder(context, 1, listOf())
            }
        } else {
            Toast.makeText(context, "type is null! please check it.", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun processDailyReminder(context: Context) {
        showAlarmNotification(
            context,
            context.getString(R.string.app_name),
            context.getString(R.string.daily_reminder_message),
            ReminderType.DAILY_REMINDER
        )
    }

    private fun processReleaseTodayReminder(context: Context) {
        // fetch release today movies
        movieRepository.discoverReleaseToday(page = 1, callback = object : ResponseListener {
            override fun onError(anError: ANError?) {
                showAlarmNotification(
                    context,
                    context.getString(R.string.release_reminder_title_notification),
                    context.getString(R.string.release_reminder_message2),
                    ReminderType.RELEASE_REMINDER
                )
            }

            override fun onResponse(meta: Meta, movies: List<Movie>) {
                // show notification only when there are release movie on that day
                if (movies.isEmpty()) return

                // build notification message
                val message = context.getString(
                    R.string.release_reminder_message,
                    movies[0].title,
                    meta.totalResults - 1
                )

                // show notification
                showAlarmNotification(
                    context,
                    context.getString(R.string.release_reminder_title_notification),
                    message,
                    ReminderType.RELEASE_REMINDER
                )
            }
        })
    }

    private fun processAiringTodayReminder(
        context: Context,
        page: Int = 1,
        initMovieList: List<Movie>
    ) {
        if (favoriteRepository == null) {
            favoriteRepository = FavoriteRepository(context)
        }

        val notificationTitle = context.getString(R.string.airing_today_reminder_title_notification)

        // fetch release today movies
        tvRepository.fetchAiringToday(page = page, callback = object : ResponseListener {
            override fun onError(anError: ANError?) {
                showAlarmNotification(
                    context,
                    notificationTitle,
                    context.getString(R.string.airing_today_reminder_message2),
                    ReminderType.RELEASE_REMINDER
                )
            }

            override fun onResponse(meta: Meta, movies: List<Movie>) {
                if (meta.page < meta.totalPages && meta.page < 5) {

                    if (meta.page == 1) {
                        GlobalScope.launch {
                            if ((favoriteRepository?.getTVs()?.size ?: 0) > 0) {
                                processAiringTodayReminder(
                                    context,
                                    page + 1,
                                    initMovieList + movies
                                )
                            } else {
                                // show notification
                                showAlarmNotification(
                                    context,
                                    notificationTitle,
                                    context.getString(
                                        R.string.airing_today_reminder_message,
                                        movies.subList(0, if (movies.size >= 3) 3 else movies.size)
                                            .joinToString { it.title }
                                    ),
                                    ReminderType.RELEASE_REMINDER
                                )
                            }
                        }
                    } else {
                        processAiringTodayReminder(context, page + 1, initMovieList + movies)
                    }
                } else {
                    if (meta.page != 0 || !movies.isNullOrEmpty()) {

                        GlobalScope.launch {
                            // find favorite in airing today
                            val favorites = initMovieList.filter {
                                return@filter favoriteRepository?.isFavorite(it.id) ?: false
                            }

                            // build notification message
                            val message = if (favorites.isEmpty()) {
                                // if there is no favorite movie that airing today until page 5
                                context.getString(
                                    R.string.airing_today_reminder_message,
                                    movies.subList(0, if (movies.size >= 3) 3 else movies.size)
                                        .joinToString { it.title }
                                )
                            } else {
                                context.getString(
                                    R.string.airing_today_reminder_message,
                                    favorites.joinToString { it.title }
                                )
                            }

                            // show notification
                            showAlarmNotification(
                                context,
                                notificationTitle,
                                message,
                                ReminderType.AIRING_TODAY_REMINDER
                            )
                        }
                    }
                }
            }
        })
    }

    private fun showAlarmNotification(
        context: Context,
        title: String,
        message: String,
        type: ReminderType
    ) {

        val icon = when (type) {
            ReminderType.DAILY_REMINDER -> R.drawable.ic_ondemand_video_white_24dp
            ReminderType.RELEASE_REMINDER -> R.drawable.ic_movie_white_24dp
            ReminderType.AIRING_TODAY_REMINDER -> R.drawable.ic_live_tv_white_24dp
        }

        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent =
            PendingIntent.getActivity(context, 200, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationManagerCompat =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentIntent(pendingIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setColor(ContextCompat.getColor(context, android.R.color.transparent))
            .setVibrate(longArrayOf(500, 500, 800, 800, 1000))
            .setSound(alarmSound)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            /* Create or update. */
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(500, 500, 800, 800, 1000)

            builder.setChannelId(CHANNEL_ID)

            notificationManagerCompat.createNotificationChannel(channel)
        }

        val notification = builder.build()

        notificationManagerCompat.notify(type.id, notification)
    }
}
