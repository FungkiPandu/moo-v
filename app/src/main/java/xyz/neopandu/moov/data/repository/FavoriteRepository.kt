package xyz.neopandu.moov.data.repository

import android.app.Application
import xyz.neopandu.moov.data.FavoriteDao
import xyz.neopandu.moov.data.database.MovieDatabase
import xyz.neopandu.moov.models.Movie

class FavoriteRepository(application: Application) {

    private lateinit var favoriteDao: FavoriteDao

    init {
        val database: MovieDatabase? = MovieDatabase.getInstance(application.applicationContext)
        database?.let {
            favoriteDao = it.favoriteDao()
        }
    }

    suspend fun saveMovie(movie: Movie) {
        favoriteDao.saveMovie(movie)
    }

    suspend fun getMovies(): List<Movie> {
        return favoriteDao.loadFavoriteMovies()
    }

    suspend fun getTVs(): List<Movie> {
        return favoriteDao.loadFavoriteTVs()
    }

    suspend fun deleteSavedMovie(movie: Movie) {
        return favoriteDao.deleteFavorite(movie)
    }

    suspend fun isFavorite(movieId: Int): Boolean {
        return favoriteDao.getMovieById(movieId).isNotEmpty()
    }
}