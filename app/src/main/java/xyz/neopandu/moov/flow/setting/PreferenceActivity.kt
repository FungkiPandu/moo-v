package xyz.neopandu.moov.flow.setting

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import xyz.neopandu.moov.R
import java.util.*

class PreferenceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private val viewModel by lazy {
            ViewModelProviders.of(
                requireActivity(),
                PreferenceViewModelFactory(requireActivity().applicationContext)
            ).get(PreferenceViewModel::class.java)
        }

        private var languagePreference: Preference? = null
        private var airingTodayReminderSwitch: SwitchPreferenceCompat? = null
        private var releaseTodayReminderSwitch: SwitchPreferenceCompat? = null
        private var dailyReminderSwitch: SwitchPreferenceCompat? = null

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            languagePreference = findPreference(getString(R.string.language))
            airingTodayReminderSwitch =
                findPreference(getString(R.string.airing_today_reminder_key))
            releaseTodayReminderSwitch = findPreference(getString(R.string.release_reminder_key))
            dailyReminderSwitch = findPreference(getString(R.string.daily_reminder_key))

            languagePreference?.summary = Locale.getDefault().displayLanguage
            languagePreference?.setOnPreferenceClickListener {
                val mIntent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                startActivity(mIntent)
                true
            }

            viewModel.airingTodayStatus.observe(requireActivity(), Observer {
                airingTodayReminderSwitch?.isChecked = it
            })

            viewModel.releaseRemainderStatus.observe(requireActivity(), Observer {
                releaseTodayReminderSwitch?.isChecked = it
            })

            viewModel.dailyReminderStatus.observe(requireActivity(), Observer {
                dailyReminderSwitch?.isChecked = it
            })

            airingTodayReminderSwitch?.setOnPreferenceChangeListener { _, _ ->
                viewModel.toggleAiringTodayReminderStatus()
                true
            }
            releaseTodayReminderSwitch?.setOnPreferenceChangeListener { _, _ ->
                viewModel.toggleReleaseReminderStatus()
                true
            }
            dailyReminderSwitch?.setOnPreferenceChangeListener { _, _ ->
                viewModel.toggleDailyReminderStatus()
                true
            }
        }
    }
}