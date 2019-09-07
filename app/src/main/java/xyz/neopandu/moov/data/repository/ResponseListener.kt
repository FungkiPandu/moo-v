package xyz.neopandu.moov.data.repository

import com.androidnetworking.error.ANError
import xyz.neopandu.moov.models.Movie


interface ResponseListener {
    fun onResponse(page: Int, movies: List<Movie>)
    fun onError(anError: ANError?)
}