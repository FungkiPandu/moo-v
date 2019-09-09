package xyz.neopandu.moov.flow.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.error.ANError
import xyz.neopandu.moov.data.repository.MovieRepository
import xyz.neopandu.moov.data.repository.ResponseListener
import xyz.neopandu.moov.data.repository.TVRepository
import xyz.neopandu.moov.models.Meta
import xyz.neopandu.moov.models.Movie

class SearchViewModel : ViewModel() {
    private val movieRepository by lazy { MovieRepository() }
    private val tvRepository by lazy { TVRepository() }

    private val _movieSearchLoading = MutableLiveData<Boolean>()
    val movieSearchLoading: LiveData<Boolean>
        get() = _movieSearchLoading
    private val _tvSearchLoading = MutableLiveData<Boolean>()
    val tvSearchLoading: LiveData<Boolean>
        get() = _tvSearchLoading

    private val _movieSearchResult = MutableLiveData<List<Movie>>()
    val movieSearchResult: LiveData<List<Movie>>
        get() = _movieSearchResult
    private val _tvSearchResult = MutableLiveData<List<Movie>>()
    val tvSearchResult: LiveData<List<Movie>>
        get() = _tvSearchResult

    private val _errorRequest = MutableLiveData<ANError?>()
    val errorRequest: LiveData<ANError?>
        get() = _errorRequest

    private var lastMovieQuery = ""
    private var moviePage = 1
    private var isLastMoviePage = false
    private var lastTVQuery = ""
    private var tvPage = 1
    private var isLastTVPage = false

    private fun requestListener(movieType: Movie.MovieType): ResponseListener {
        return object : ResponseListener {
            override fun onResponse(meta: Meta, movies: List<Movie>) {
                if (movieType == Movie.MovieType.MOVIE) {
                    isLastMoviePage = meta.totalPages >= meta.page
                    moviePage++
                    _movieSearchLoading.postValue(false)
                } else {
                    isLastTVPage = meta.totalPages >= meta.page
                    tvPage++
                    _tvSearchLoading.postValue(false)
                }
                val list =
                    if (movieType == Movie.MovieType.MOVIE) _movieSearchResult else _tvSearchResult
                if (meta.page == 1) list.postValue(movies)
                else {
                    val currentList = list.value
                    if (currentList != null) {
                        list.postValue(currentList + movies)
                    } else {
                        list.postValue(movies)
                    }
                }
            }

            override fun onError(anError: ANError?) {
                _errorRequest.postValue(anError)
                when (movieType) {
                    Movie.MovieType.MOVIE -> _movieSearchLoading.postValue(false)
                    Movie.MovieType.TV_SHOW -> _tvSearchLoading.postValue(false)
                }
            }

        }
    }

    fun search(query: String) {
        if (query.isBlank()) return
        searchMovie(query)
        searchTV(query)
    }

    private fun searchMovie(query: String, page: Int = 1) {
        if (query != lastMovieQuery || page == 1) {
            isLastMoviePage = false
            moviePage = 1
        }
        if (isLastMoviePage) return
        _movieSearchLoading.postValue(true)
        lastMovieQuery = query
        movieRepository.searchMovie(query, page, requestListener(Movie.MovieType.MOVIE))
    }

    private fun searchTV(query: String, page: Int = 1) {
        if (query != lastTVQuery || page == 1) {
            isLastTVPage = false
            tvPage = 1
        }
        if (isLastTVPage) return
        _tvSearchLoading.postValue(true)
        lastTVQuery = query
        tvRepository.search(query, page, requestListener(Movie.MovieType.TV_SHOW))
    }

    fun refreshSearchResult(movieType: Movie.MovieType) {
        if (movieType == Movie.MovieType.MOVIE) {
            searchMovie(query = lastMovieQuery, page = 1)
        } else {
            searchTV(query = lastTVQuery, page = 1)
        }
    }

    fun fetchNextPage(movieType: Movie.MovieType) {
        if (movieType == Movie.MovieType.MOVIE) {
            if (lastMovieQuery.isBlank()) return
            searchMovie(query = lastMovieQuery, page = moviePage)
        } else {
            if (lastTVQuery.isBlank()) return
            searchTV(query = lastTVQuery, page = tvPage)
        }
    }
}