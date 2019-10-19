package xyz.neopandu.moov.flow.main.movieList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.error.ANError
import xyz.neopandu.moov.data.repository.MovieRepository
import xyz.neopandu.moov.data.repository.ResponseListener
import xyz.neopandu.moov.data.repository.TVRepository
import xyz.neopandu.moov.helper.SingleLiveEvent
import xyz.neopandu.moov.models.Meta
import xyz.neopandu.moov.models.Movie

class MovieListViewModel : ViewModel() {

    private val movieRepository = MovieRepository()
    private val tvRepository = TVRepository()

    private val _movieList = mutableListOf<Movie>()

    private val _showMovieLoading = MutableLiveData<Boolean>()
    val showMovieLoading: LiveData<Boolean>
        get() = _showMovieLoading

    private val _movies = MutableLiveData<List<Movie>>()
    val movies: LiveData<List<Movie>>
        get() = _movies

    private val _showError = SingleLiveEvent<() -> Unit>()
    val showError: LiveData<() -> Unit>
        get() = _showError

    private val _isMovie = MutableLiveData<Boolean>()
    val isMovie: LiveData<Boolean>
        get() = _isMovie

    private var _moviePage = 0
    private var isMax = false

    var movieEndpoint: MovieListFragment.MovieEndpoint? = null
        set(value) {
            if (field != null) return
            field = value
            _isMovie.postValue(
                when (movieEndpoint) {
                    MovieListFragment.MovieEndpoint.POPULAR_MOVIE,
                    MovieListFragment.MovieEndpoint.RELEASE_TODAY,
                    MovieListFragment.MovieEndpoint.NOW_PLAYING,
                    MovieListFragment.MovieEndpoint.UPCOMING -> true
                    else -> false
                }
            )
            fetchInitMovieList()
        }

    fun fetchInitMovieList() {
        fetchMovieList()
    }

    fun fetchNextMovieList() {
        if (isMax) return
        _moviePage++
        fetchMovieList(_moviePage)
    }

    private fun fetchMovieList(page: Int = 1) {
        when (movieEndpoint) {
            MovieListFragment.MovieEndpoint.POPULAR_MOVIE -> fetchPopularMovieList(page)
            MovieListFragment.MovieEndpoint.RELEASE_TODAY -> fetchReleaseTodayMovieList(page)
            MovieListFragment.MovieEndpoint.NOW_PLAYING -> fetchNowPlayingMovieList(page)
            MovieListFragment.MovieEndpoint.UPCOMING -> fetchUpcomingMovieList(page)
            MovieListFragment.MovieEndpoint.POPULAR_TV -> fetchPopularTVList(page)
            MovieListFragment.MovieEndpoint.AIRING_TODAY -> fetchAiringTodayTVList(page)
            MovieListFragment.MovieEndpoint.ON_THE_AIR -> fetchOnTheAirTVList(page)
        }
    }

    private fun fetchPopularMovieList(page: Int = 1) {
        _showMovieLoading.postValue(true)
        movieRepository.fetchPopularMovies(page, responseListener(page))
    }

    private fun fetchNowPlayingMovieList(page: Int = 1) {
        _showMovieLoading.postValue(true)
        movieRepository.fetchNowPlaying(page, responseListener(page))
    }

    private fun fetchUpcomingMovieList(page: Int = 1) {
        _showMovieLoading.postValue(true)
        movieRepository.fetchUpcoming(page, responseListener(page))
    }

    private fun fetchReleaseTodayMovieList(page: Int = 1) {
        _showMovieLoading.postValue(true)
        movieRepository.discoverReleaseToday(page, responseListener(page))
    }

    private fun fetchPopularTVList(page: Int = 1) {
        _showMovieLoading.postValue(true)
        tvRepository.fetchPopularTVs(page, responseListener(page))
    }

    private fun fetchAiringTodayTVList(page: Int = 1) {
        _showMovieLoading.postValue(true)
        tvRepository.fetchAiringToday(page, responseListener(page))
    }

    private fun fetchOnTheAirTVList(page: Int = 1) {
        _showMovieLoading.postValue(true)
        tvRepository.fetchOnTheAir(page, responseListener(page))
    }

    private fun responseListener(page: Int) = object : ResponseListener {
        override fun onResponse(meta: Meta, movies: List<Movie>) {
            if (meta.page == 1) {
                _movieList.clear()
                _moviePage = 1
            }
            isMax = page >= meta.totalPages
            _movieList.addAll(movies)
            _movies.postValue(_movieList)
            _showMovieLoading.postValue(false)
        }

        override fun onError(anError: ANError?) {
            _showMovieLoading.postValue(false)
            _showError.postValue {
                fetchMovieList(page)
            }
        }
    }
}