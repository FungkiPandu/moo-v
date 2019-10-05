package xyz.neopandu.moov.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import xyz.neopandu.moov.data.receiver.AlarmReceiver
import java.util.*

class ReminderRepository(private val context: Context) {

    fun setReminder(type: AlarmReceiver.ReminderType) {

        // easy for maintenance
//        val timeArray = when (type) {
//            AlarmReceiver.ReminderType.DAILY_REMINDER -> intArrayOf(7, 0)
//            AlarmReceiver.ReminderType.RELEASE_REMINDER -> intArrayOf(8, 0)
//            AlarmReceiver.ReminderType.AIRING_TODAY_REMINDER -> intArrayOf(7, 30)
//        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(AlarmReceiver.EXTRA_TYPE, type.id)

        val calendar = Calendar.getInstance()
//        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArray[0]))
//        calendar.set(Calendar.MINUTE, Integer.parseInt(timeArray[1]))
//        calendar.set(Calendar.SECOND, 0)
        calendar.add(Calendar.SECOND, 30) // for development only

        val pendingIntent = PendingIntent.getBroadcast(context, type.id, intent, 0)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        Toast.makeText(context, "${type.name} alarm set up", Toast.LENGTH_SHORT).show()
    }


    fun cancelReminder(type: AlarmReceiver.ReminderType) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val requestCode = type.id
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0)
        pendingIntent.cancel()

        alarmManager.cancel(pendingIntent)

        Toast.makeText(context, "${type.name} alarm canceled", Toast.LENGTH_SHORT).show()
    }

    fun isReminderSet(type: AlarmReceiver.ReminderType): Boolean {
        val intent = Intent(context, AlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            type.id,
            intent,
            PendingIntent.FLAG_NO_CREATE
        ) != null
    }
}