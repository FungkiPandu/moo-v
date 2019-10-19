package xyz.neopandu.moov.data.repository

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.database.Cursor
import androidx.lifecycle.LiveData
import xyz.neopandu.moov.R
import xyz.neopandu.moov.data.FavoriteDao
import xyz.neopandu.moov.data.database.MovieDatabase
import xyz.neopandu.moov.models.Movie
import xyz.neopandu.moov.widget.FavoriteMoviesWidget


class FavoriteRepository(private val applicationContext: Context) {

    private lateinit var favoriteDao: FavoriteDao


    init {
        val database: MovieDatabase? = MovieDatabase.getInstance(applicationContext)
        database?.let {
            favoriteDao = it.favoriteDao()
        }
    }

    fun getFavoriteMoviesLiveData(): LiveData<List<Movie>> = favoriteDao.favoriteMovies()
    fun getFavoriteTVsLiveData(): LiveData<List<Movie>> = favoriteDao.favoriteTVs()

    private fun updateWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val ids = appWidgetManager.getAppWidgetIds(
            ComponentName(
                applicationContext,
                FavoriteMoviesWidget::class.java
            )
        )
        appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.stack_view)
    }

    fun saveMovie(movie: Movie) {
        try {
            favoriteDao.saveMovie(movie)
            updateWidgets()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getMovies(): List<Movie> {
        return favoriteDao.loadFavoriteMovies()
    }

    fun getMoviesCursor(): Cursor {
        return favoriteDao.favoriteMoviesCursor()
    }

    suspend fun getTVs(): List<Movie> {
        return favoriteDao.loadFavoriteTVs()
    }

    fun getTVsCursor(): Cursor {
        return favoriteDao.favoriteTVsCursor()
    }

    fun deleteSavedMovie(movie: Movie) {
        favoriteDao.deleteFavorite(movie)
        updateWidgets()
    }

    fun deleteSavedMovieById(movieId: Int) : Int {
        return try {
            favoriteDao.deleteFavoriteById(movieId)
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }

    }

    suspend fun isFavorite(movieId: Int): Boolean {
        return favoriteDao.getMovieById(movieId).isNotEmpty()
    }
}