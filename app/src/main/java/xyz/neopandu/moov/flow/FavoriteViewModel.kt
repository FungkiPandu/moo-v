package xyz.neopandu.moov.flow

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import xyz.neopandu.moov.data.repository.FavoriteRepository
import xyz.neopandu.moov.flow.main.movieList.MovieFragment
import xyz.neopandu.moov.models.Movie

class FavoriteViewModel(application: Application) : ViewModel() {

    private val favoriteRepository = FavoriteRepository(application)

    private val _favMovies = MutableLiveData<List<Movie>>()
    val favMovies: LiveData<List<Movie>>
        get() = _favMovies

    private val _favTVs = MutableLiveData<List<Movie>>()
    val favTVs: LiveData<List<Movie>>
        get() = _favTVs

    private val _showError = MutableLiveData<Pair<MovieFragment.FragmentType, () -> Unit>>()
    val showError: LiveData<Pair<MovieFragment.FragmentType, () -> Unit>>
        get() = _showError

    val favoriteMovies: LiveData<List<Movie>>
        get() = favoriteRepository.getFavoriteMoviesLiveData()

    val favoriteTVs: LiveData<List<Movie>>
        get() = favoriteRepository.getFavoriteTVsLiveData()

    init {
        updateFavoriteMovies()
        updateFavoriteTVs()
    }

    fun toggleFavorite(movie: Movie) {
        GlobalScope.launch {
            if (movie.isFavorite) {
                removeFavoriteAsync(movie).await()
            } else {
                addFavoriteAsync(movie).await()
            }
            refreshSavedMovies(movie.movieType)
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

    private fun refreshSavedMovies(movieType: Movie.MovieType) {
        when (movieType) {
            Movie.MovieType.MOVIE -> updateFavoriteMovies()
            Movie.MovieType.TV_SHOW -> updateFavoriteTVs()
        }
    }

    fun updateFavoriteMovies() {
        GlobalScope.launch {
            _favMovies.postValue(favoriteRepository.getMovies())
        }
    }

    fun updateFavoriteTVs() {
        GlobalScope.launch {
            _favTVs.postValue(favoriteRepository.getTVs())
        }
    }
}