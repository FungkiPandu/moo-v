package xyz.neopandu.moov.models

import androidx.room.TypeConverter

class MovieTypeConverter {
    @TypeConverter
    fun toMovieType(value: String): Movie.MovieType = Movie.MovieType.valueOf(value)

    @TypeConverter
    fun toString(movieType: Movie.MovieType): String = movieType.name
}