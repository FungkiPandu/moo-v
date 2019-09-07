package xyz.neopandu.moov.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import xyz.neopandu.moov.models.Movie

@Dao
interface FavoriteDao {

    @Insert
    suspend fun saveMovie(movie: Movie)

    @Query("SELECT * FROM movie WHERE movieType = 'TV_SHOW'")
    suspend fun loadFavoriteTVs(): List<Movie>

    @Query("SELECT * FROM movie WHERE movieType = 'MOVIE'")
    suspend fun loadFavoriteMovies(): List<Movie>

    @Query("SELECT * FROM movie WHERE id = :movieId")
    suspend fun getMovieById(movieId: Int): List<Movie>

    @Delete
    suspend fun deleteFavorite(movie: Movie)
}