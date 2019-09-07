package xyz.neopandu.moov.flow.main

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.error.ANError
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import xyz.neopandu.moov.data.repository.FavoriteRepository
import xyz.neopandu.moov.data.repository.MovieRepository
import xyz.neopandu.moov.data.repository.ResponseListener
import xyz.neopandu.moov.data.repository.TVRepository
import xyz.neopandu.moov.flow.main.movieList.MovieFragment
import xyz.neopandu.moov.models.Meta
import xyz.neopandu.moov.models.Movie

class MainViewModel(application: Application) : ViewModel() {

    private val favoriteRepository = FavoriteRepository(application)
    private val movieRepository = MovieRepository()
    private val tvRepository = TVRepository()

    private val _movieList = mutableListOf<Movie>()
    private val _tvList = mutableListOf<Movie>()

    private val _showMovieLoading = MutableLiveData<Boolean>()
    val showMovieLoading: LiveData<Boolean>
        get() = _showMovieLoading

    private val _showTVLoading = MutableLiveData<Boolean>()
    val showTVLoading: LiveData<Boolean>
        get() = _showTVLoading

    private val _movies = MutableLiveData<List<Movie>>()
    val movies: LiveData<List<Movie>>
        get() = _movies

    private val _tvShows = MutableLiveData<List<Movie>>()
    val tvShows: LiveData<List<Movie>>
        get() = _tvShows

    private val _favMovies = MutableLiveData<List<Movie>>()
    val favMovies: LiveData<List<Movie>>
        get() = _favMovies

    private val _favTVs = MutableLiveData<List<Movie>>()
    val favTVs: LiveData<List<Movie>>
        get() = _favTVs

    private val _showError = MutableLiveData<Pair<MovieFragment.FragmentType, () -> Unit>>()
    val showError: LiveData<Pair<MovieFragment.FragmentType, () -> Unit>>
        get() = _showError

    private var _moviePage = 1
    private var _tvPage = 1

    init {
        fetchMovieList(1)
        fetchTvShowList(1)
        updateFavoriteMovies()
        updateFavoriteTVs()
    }

    fun fetchNextMovieList() {
        _moviePage++
        fetchMovieList(_moviePage)
    }

    fun fetchNextTvShowList() {
        _tvPage++
        fetchTvShowList(_tvPage)
    }

    fun fetchMovieList(page: Int = 1) {
        _showMovieLoading.postValue(true)
        movieRepository.fetchPopularMovies(page, object : ResponseListener {
            override fun onResponse(meta: Meta, movies: List<Movie>) {
                if (meta.page == 1) {
                    _movieList.clear()
                    _moviePage = 1
                }
                _movieList.addAll(movies)
                _movies.postValue(_movieList)

                updateFavoriteMovies()
                _showMovieLoading.postValue(false)
            }

            override fun onError(anError: ANError?) {
                _showMovieLoading.postValue(false)
                _showError.postValue(MovieFragment.FragmentType.MOVIE to { fetchMovieList(page) })
            }

        })
    }

    fun fetchTvShowList(page: Int = 1) {
        _showTVLoading.postValue(true)

        tvRepository.fetchPopularTVs(page, object : ResponseListener {
            override fun onResponse(meta: Meta, movies: List<Movie>) {
                if (meta.page == 1) {
                    _tvList.clear()
                    _tvPage = 1
                }
                _tvList.addAll(movies)
                _tvShows.postValue(_tvList)
                updateFavoriteTVs()
                _showTVLoading.postValue(false)
            }

            override fun onError(anError: ANError?) {
                _showTVLoading.postValue(false)
                _showError.postValue(MovieFragment.FragmentType.TV_SHOW to { fetchTvShowList(page) })
            }
        })
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