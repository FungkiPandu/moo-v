package xyz.neopandu.moov.flow

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import xyz.neopandu.moov.data.repository.FavoriteRepository
import xyz.neopandu.moov.models.Movie

class FavoriteViewModel(application: Application) : ViewModel() {

    private val favoriteRepository = FavoriteRepository(application)

    val favoriteMovies: LiveData<List<Movie>>
        get() = favoriteRepository.getFavoriteMoviesLiveData()

    val favoriteTVs: LiveData<List<Movie>>
        get() = favoriteRepository.getFavoriteTVsLiveData()

    fun toggleFavorite(movie: Movie) {
        GlobalScope.launch {
            if (movie.isFavorite) {
                removeFavoriteAsync(movie).await()
            } else {
                addFavoriteAsync(movie).await()
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