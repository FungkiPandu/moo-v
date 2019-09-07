package xyz.neopandu.moov.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.android.parcel.Parcelize

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
    var isFavorite: Boolean
) : Parcelable {
    enum class MovieType {
        MOVIE, TV_SHOW
    }
}