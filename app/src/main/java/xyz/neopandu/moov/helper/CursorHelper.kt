package xyz.neopandu.moov.helper

import android.database.Cursor
import xyz.neopandu.moov.models.Movie

fun Cursor.toMovie(): Movie {
    val id = this.getInt(this.getColumnIndexOrThrow("id"))
    val title = this.getString(this.getColumnIndexOrThrow("title"))
    val oriTitle = this.getString(this.getColumnIndexOrThrow("oriTitle"))
    val oriLang = this.getString(this.getColumnIndexOrThrow("oriLang"))
    val description = this.getString(this.getColumnIndexOrThrow("description"))
    val posterPath = this.getString(this.getColumnIndexOrThrow("posterPath"))
    val bannerPath = this.getString(this.getColumnIndexOrThrow("bannerPath"))
    val score = this.getDouble(this.getColumnIndexOrThrow("score"))
    val popularity = this.getDouble(this.getColumnIndexOrThrow("popularity"))
    val releaseDate = this.getString(this.getColumnIndexOrThrow("releaseDate"))
//    val movieType = this.getString(this.getColumnIndexOrThrow("movieType"))
    return Movie(
        id, title, oriTitle, oriLang, description, posterPath, bannerPath, score,
        popularity, releaseDate, Movie.MovieType.valueOf("MOVIE"), true
    )
}


fun Cursor.toMovies(): List<Movie> {
    val movies = mutableListOf<Movie>()
    while (this.moveToNext()) {
        movies.add(this.toMovie())
    }
    return movies
}