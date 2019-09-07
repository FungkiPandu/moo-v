package xyz.neopandu.moov.flow.detail

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import xyz.neopandu.moov.data.repository.FavoriteRepository
import xyz.neopandu.moov.models.Movie

class DetailViewModel(application: Application) : ViewModel() {

    private val favoriteRepository = FavoriteRepository(application)

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean>
        get() = _isFavorite

    fun checkIsFavorite(movie: Movie) {
        GlobalScope.launch {
            val isFav = favoriteRepository.isFavorite(movie.id)
            _isFavorite.postValue(isFav)
        }
    }

    fun toggleFavorite(movie: Movie) {
        GlobalScope.launch {
            isFavorite.value?.let {
                if (it) {
                    removeFavoriteAsync(movie).await()
                } else {
                    addFavoriteAsync(movie).await()
                }
                checkIsFavorite(movie)
            }
        }
    }

    private suspend fun removeFavoriteAsync(movie: Movie): Deferred<Unit> =
        GlobalScope.async {
            favoriteRepository.deleteSavedMovie(movie)
        }

    private suspend fun addFavoriteAsync(movie: Movie): Deferred<Unit> =
        GlobalScope.async {
            favoriteRepository.saveMovie(movie)
        }

}