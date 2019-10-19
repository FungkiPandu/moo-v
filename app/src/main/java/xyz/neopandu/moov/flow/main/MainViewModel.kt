package xyz.neopandu.moov.flow.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.error.ANError
import xyz.neopandu.moov.data.repository.MovieRepository
import xyz.neopandu.moov.data.repository.ResponseListener
import xyz.neopandu.moov.data.repository.TVRepository
import xyz.neopandu.moov.models.Meta
import xyz.neopandu.moov.models.Movie

class MainViewModel : ViewModel() {

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

    private val _showError = MutableLiveData<Pair<Movie.MovieType, () -> Unit>>()
    val showError: LiveData<Pair<Movie.MovieType, () -> Unit>>
        get() = _showError

    private var _moviePage = 1
    private var _tvPage = 1

    init {
        fetchMovieList(1)
        fetchTvShowList(1)
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
                _showMovieLoading.postValue(false)
            }

            override fun onError(anError: ANError?) {
                _showMovieLoading.postValue(false)
                _showError.postValue(Movie.MovieType.MOVIE to { fetchMovieList(page) })
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
                _showTVLoading.postValue(false)
            }

            override fun onError(anError: ANError?) {
                _showTVLoading.postValue(false)
                _showError.postValue(Movie.MovieType.TV_SHOW to { fetchTvShowList(page) })
            }
        })
    }
}