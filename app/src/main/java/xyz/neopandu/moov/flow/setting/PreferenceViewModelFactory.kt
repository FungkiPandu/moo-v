package xyz.neopandu.moov.flow.setting

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


@Suppress("UNCHECKED_CAST")
class PreferenceViewModelFactory(private val applicationContext: Context) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PreferenceViewModel(applicationContext) as T
    }
}