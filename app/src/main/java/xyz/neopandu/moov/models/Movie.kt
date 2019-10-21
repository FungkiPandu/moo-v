package xyz.neopandu.moov.models

import android.os.Bundle
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.android.parcel.Parcelize
import xyz.neopandu.moov.data.database.DBContract.MovieColumns.Companion.BANNER_PATH
import xyz.neopandu.moov.data.database.DBContract.MovieColumns.Companion.DESCRIPTION
import xyz.neopandu.moov.data.database.DBContract.MovieColumns.Companion.MOVIE_TYPE
import xyz.neopandu.moov.data.database.DBContract.MovieColumns.Companion.ORI_LANG
import xyz.neopandu.moov.data.database.DBContract.MovieColumns.Companion.ORI_TITLE
import xyz.neopandu.moov.data.database.DBContract.MovieColumns.Companion.POPULARITY
import xyz.neopandu.moov.data.database.DBContract.MovieColumns.Companion.POSTER_PATH
import xyz.neopandu.moov.data.database.DBContract.MovieColumns.Companion.RELEASE_DATE
import xyz.neopandu.moov.data.database.DBContract.MovieColumns.Companion.SCORE
import xyz.neopandu.moov.data.database.DBContract.MovieColumns.Companion.TITLE
import xyz.neopandu.moov.data.database.DBContract.MovieColumns.Companion._ID
import java.lang.reflect.Array.getDouble

@Parcelize
@Entity(tableName = "movie")
@TypeConverters(MovieTypeConverter::class)
data class Movie(
    @PrimaryKey val id: Int,
    val title: String,
    val oriTitle: String,
    val oriLang: String,
    val description: String,
    val posterPath: String,
    val bannerPath: String,
    val score: Double,
    val popularity: Double,
    val releaseDate: String,
    val movieType: MovieType,
    var isFavorite: Boolean = false
) : Parcelable {
    enum class MovieType(val value: String) {
        MOVIE("MOVIE"), TV_SHOW("TV SHOW")
    }

    fun asBundle(): Bundle {
        return Bundle().apply {
            putInt(_ID, id)
            putString(TITLE, title)
            putString(ORI_TITLE, oriTitle)
            putString(ORI_LANG, oriLang)
            putString(DESCRIPTION, description)
            putString(POSTER_PATH, posterPath)
            putString(BANNER_PATH, bannerPath)
            putDouble(SCORE, score)
            putDouble(POPULARITY, popularity)
            putString(RELEASE_DATE, releaseDate)
            putString(MOVIE_TYPE, movieType.name)
        }
    }

    companion object {
        fun fromBundleOrNull(bundle: Bundle?) : Movie? {
            return bundle?.let {
                Movie(
                    it.getInt(_ID, 0),
                    it.getString(TITLE, "N/A"),
                    it.getString(ORI_TITLE, ""),
                    it.getString(ORI_LANG, ""),
                    it.getString(DESCRIPTION, "No description"),
                    it.getString(POSTER_PATH, "/"),
                    it.getString(BANNER_PATH, "/"),
                    it.getDouble(SCORE, 0.0),
                    it.getDouble(POPULARITY, 0.0),
                    it.getString(RELEASE_DATE, "N/A"),
                    MovieType.valueOf(it.getString(MOVIE_TYPE, MovieType.MOVIE.name)),
                    true
                )
            }
        }
    }
}