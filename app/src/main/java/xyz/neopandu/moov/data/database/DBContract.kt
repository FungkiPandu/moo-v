package xyz.neopandu.moov.data.database

import android.net.Uri
import android.provider.BaseColumns


object DBContract {

    const val AUTHORITY = "xyz.neopandu.moov.provider"
    const val SCHEME = "content"

    class MovieColumns : BaseColumns {
        companion object {
            const val TABLE_MOVIE = "movie"
            const val TABLE_TV = "tv"

            const val _ID = "id"
            const val TITLE = "title"
            const val DESCRIPTION = "description"
            const val ORI_TITLE = "oriTitle"
            const val ORI_LANG = "oriLang"
            const val POSTER_PATH = "posterPath"
            const val BANNER_PATH = "bannerPath"
            const val SCORE = "score"
            const val POPULARITY = "popularity"
            const val RELEASE_DATE = "releaseDate"
            const val MOVIE_TYPE = "movieType"

            val CONTENT_URI_MOVIE: Uri = Uri.Builder().scheme(SCHEME)
                .authority(AUTHORITY)
                .appendPath(TABLE_MOVIE)
                .build()

            val CONTENT_URI_TV: Uri = Uri.Builder().scheme(SCHEME)
                .authority(AUTHORITY)
                .appendPath(TABLE_TV)
                .build()
        }
    }
}