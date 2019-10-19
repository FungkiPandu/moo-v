package xyz.neopandu.moov.helper

import androidx.annotation.StringRes
import xyz.neopandu.moov.R
import xyz.neopandu.moov.flow.main.movieList.MovieListFragment

@StringRes
fun getStringResTitleOf(movieEndpoint: MovieListFragment.MovieEndpoint): Int {
    return when (movieEndpoint) {
        MovieListFragment.MovieEndpoint.POPULAR_MOVIE -> R.string.popular_title
        MovieListFragment.MovieEndpoint.NOW_PLAYING -> R.string.now_playing_title
        MovieListFragment.MovieEndpoint.UPCOMING -> R.string.upcoming_title
        MovieListFragment.MovieEndpoint.RELEASE_TODAY -> R.string.release_today_title
        MovieListFragment.MovieEndpoint.POPULAR_TV -> R.string.popular_title
        MovieListFragment.MovieEndpoint.AIRING_TODAY -> R.string.airing_today_title
        MovieListFragment.MovieEndpoint.ON_THE_AIR -> R.string.on_the_air
    }
}