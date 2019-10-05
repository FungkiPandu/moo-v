package xyz.neopandu.moov.flow.setting

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import xyz.neopandu.moov.data.receiver.AlarmReceiver
import xyz.neopandu.moov.data.repository.ReminderRepository

class PreferenceViewModel(applicationContext: Context) : ViewModel() {

    private val reminderRepository = ReminderRepository(applicationContext)

    private val _dailyRemainderStatus = MutableLiveData<Boolean>()
    val dailyReminderStatus: LiveData<Boolean>
        get() = _dailyRemainderStatus

    private val _releaseRemainderStatus = MutableLiveData<Boolean>()
    val releaseRemainderStatus: LiveData<Boolean>
        get() = _releaseRemainderStatus

    private val _airingTodayRemainderStatus = MutableLiveData<Boolean>()
    val airingTodayStatus: LiveData<Boolean>
        get() = _airingTodayRemainderStatus

    init {
        updateAiringTodayReminderStatus()
        updateReleaseReminderStatus()
        updateDailyReminderStatus()
    }

    private fun updateDailyReminderStatus() {
        _dailyRemainderStatus.postValue(isDailyReminderEnabled)
    }

    private fun updateReleaseReminderStatus() {
        _releaseRemainderStatus.postValue(isReleaseReminderEnabled)
    }

    private fun updateAiringTodayReminderStatus() {
        _airingTodayRemainderStatus.postValue(isAiringTodayReminderEnabled)
    }

    private val isDailyReminderEnabled: Boolean
        get() = reminderRepository.isReminderSet(AlarmReceiver.ReminderType.DAILY_REMINDER)

    private val isReleaseReminderEnabled: Boolean
        get() = reminderRepository.isReminderSet(AlarmReceiver.ReminderType.RELEASE_REMINDER)

    private val isAiringTodayReminderEnabled: Boolean
        get() = reminderRepository.isReminderSet(AlarmReceiver.ReminderType.AIRING_TODAY_REMINDER)

    fun toggleDailyReminderStatus() {
        val type = AlarmReceiver.ReminderType.DAILY_REMINDER
        if (isDailyReminderEnabled)
            reminderRepository.cancelReminder(type)
        else reminderRepository.setReminder(type)
        updateDailyReminderStatus()
    }

    fun toggleReleaseReminderStatus() {
        val type = AlarmReceiver.ReminderType.RELEASE_REMINDER
        if (isReleaseReminderEnabled)
            reminderRepository.cancelReminder(type)
        else reminderRepository.setReminder(type)
        updateReleaseReminderStatus()
    }

    fun toggleAiringTodayReminderStatus() {
        val type = AlarmReceiver.ReminderType.AIRING_TODAY_REMINDER
        if (isAiringTodayReminderEnabled)
            reminderRepository.cancelReminder(type)
        else reminderRepository.setReminder(type)
        updateAiringTodayReminderStatus()
    }
}