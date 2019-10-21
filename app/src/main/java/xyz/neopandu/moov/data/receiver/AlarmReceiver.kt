package xyz.neopandu.moov.data.receiver

import android.app.Notification
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
import com.bumptech.glide.Glide
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import xyz.neopandu.moov.R
import xyz.neopandu.moov.data.repository.FavoriteRepository
import xyz.neopandu.moov.data.repository.MovieRepository
import xyz.neopandu.moov.data.repository.ResponseListener
import xyz.neopandu.moov.data.repository.TVRepository
import xyz.neopandu.moov.flow.detail.DetailActivity
import xyz.neopandu.moov.flow.main.MainActivity
import xyz.neopandu.moov.models.Meta
import xyz.neopandu.moov.models.Movie
import xyz.neopandu.moov.widget.StackWidgetService

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
        showSimpleNotification(
            context,
            context.getString(R.string.app_name),
            context.getString(R.string.daily_reminder_content),
            context.getString(R.string.daily_reminder_message),
            ReminderType.DAILY_REMINDER
        )
    }

    private fun processReleaseTodayReminder(context: Context) {
        // fetch release today movies
        movieRepository.discoverReleaseToday(page = 1, callback = object : ResponseListener {
            override fun onError(anError: ANError?) {
                showSimpleNotification(
                    context,
                    context.getString(R.string.release_reminder_title_notification),
                    context.getString(R.string.notification_error_content),
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
                    movies,
                    meta.totalResults,
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
                showSimpleNotification(
                    context,
                    notificationTitle,
                    context.getString(R.string.notification_error_content),
                    context.getString(R.string.airing_today_reminder_message2),
                    ReminderType.RELEASE_REMINDER
                )
            }

            override fun onResponse(meta: Meta, movies: List<Movie>) {
                if (meta.page < meta.totalPages && meta.page < 5) {

                    if (meta.page == 1) {
                        GlobalScope.launch {
                            // if there is favorite tv, app will get more airing today data
                            if ((favoriteRepository?.getTVs()?.size ?: 0) > 0) {
                                processAiringTodayReminder(
                                    context,
                                    page + 1,
                                    initMovieList + movies
                                )
                            } else {
                                // show notification instead if no favorite tvs found
                                showAlarmNotification(
                                    context,
                                    notificationTitle,
                                    context.getString(
                                        R.string.airing_today_reminder_message,
                                        movies.subList(0, if (movies.size >= 3) 3 else movies.size)
                                            .joinToString { it.title }
                                    ),
                                    movies,
                                    meta.totalResults,
                                    ReminderType.RELEASE_REMINDER
                                )
                            }
                        }
                    } else {
                        // ecpected if there is favorite tv, contiue fetch data
                        processAiringTodayReminder(context, page + 1, initMovieList + movies)
                    }
                } else {
                    if (meta.page != 0 || !movies.isNullOrEmpty()) {

                        GlobalScope.launch {
                            val allmovies = initMovieList + movies

                            // find favorite tv in airing today
                            val favorites = allmovies.filter {
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
                                if (favorites.isEmpty()) allmovies else favorites,
                                meta.totalResults,
                                ReminderType.AIRING_TODAY_REMINDER
                            )
                        }
                    }
                }
            }
        })
    }

    private fun showSimpleNotification(
        context: Context,
        title: String,
        content: String?,
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
            .setContentText(content)
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

    private fun showAlarmNotification(
        context: Context,
        title: String,
        message: String,
        movies: List<Movie>,
        total: Int,
        type: ReminderType
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            val icon = when (type) {
                ReminderType.DAILY_REMINDER -> R.drawable.ic_ondemand_video_white_24dp
                ReminderType.RELEASE_REMINDER -> R.drawable.ic_movie_white_24dp
                ReminderType.AIRING_TODAY_REMINDER -> R.drawable.ic_live_tv_white_24dp
            }

            val mainIntent = Intent(context, MainActivity::class.java)
            mainIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            val mainPendingIntent =
                PendingIntent.getActivity(
                    context,
                    200,
                    mainIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

            val notificationManagerCompat =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notifications = mutableListOf<Pair<Int, Notification>>()
            val inboxStyle = NotificationCompat.InboxStyle()

            for (movie in (if (movies.size > 5) movies.subList(0, 5) else movies)) {
                val path = movie.posterPath
                val posterUrl = if (path != "null")
                    context.getString(R.string.poster_small_base_url) + movie.posterPath
                else null

                val intent = Intent(context, DetailActivity::class.java)
                    .putExtra(StackWidgetService.EXTRA_ITEM, movie.asBundle())
                val pendingIntent =
                    PendingIntent.getActivity(
                        context,
                        movie.id,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )

                val newMessageNotification =
                    NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(icon)
                        .setContentTitle(movie.title)
                        .setContentText(movie.description)
                        .setGroup(type.name)
                        .setContentIntent(pendingIntent)


                if (posterUrl != null) {
                    newMessageNotification.setLargeIcon(
                        Glide.with(context).asBitmap().load(posterUrl).submit().get()
                    )
                }

                notifications.add(movie.id to newMessageNotification.build())
                inboxStyle.addLine(movie.title)
            }

            val summaryNotification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                //set content text to support devices running API level < 24
                .setContentText(message)
                .setSmallIcon(icon)
                //build summary info into InboxStyle template
                .setStyle(inboxStyle.setBigContentTitle("$total items"))
                //specify which group this notification belongs to
                .setGroup(type.name)
                //set this notification as the summary for the group
                .setGroupSummary(true)
                .setContentIntent(mainPendingIntent)
                .setSound(alarmSound)
                .build()
            notifications.add(type.id to summaryNotification)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                /* Create or update. */
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                )

                channel.enableVibration(true)
                channel.vibrationPattern = longArrayOf(500, 500, 800, 800, 1000)

                notificationManagerCompat.createNotificationChannel(channel)
            }

            notificationManagerCompat.run {
                for (pairIdandNotification in notifications) {
                    val (id, notification) = pairIdandNotification
                    notify(id, notification)
                }
            }
        } else {
            showSimpleNotification(context, title, null, message, type)
        }
    }
}
