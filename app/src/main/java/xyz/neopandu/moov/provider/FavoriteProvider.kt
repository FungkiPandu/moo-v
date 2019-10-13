package xyz.neopandu.moov.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import xyz.neopandu.moov.data.database.DBContract.AUTHORITY
import xyz.neopandu.moov.data.database.DBContract.MovieColumns.Companion.CONTENT_URI_MOVIE
import xyz.neopandu.moov.data.database.DBContract.MovieColumns.Companion.CONTENT_URI_TV
import xyz.neopandu.moov.data.database.DBContract.MovieColumns.Companion.TABLE_MOVIE
import xyz.neopandu.moov.data.database.DBContract.MovieColumns.Companion.TABLE_TV
import xyz.neopandu.moov.data.repository.FavoriteRepository


class FavoriteProvider : ContentProvider() {

    companion object {

        /*
        Integer digunakan sebagai identifier antara select all sama select by id
         */
        private const val MOVIE = 1
        private const val TV = 2
        private const val MOVIE_ID = 3
        private const val TV_ID = 4

        private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            sUriMatcher.addURI(AUTHORITY, TABLE_MOVIE, MOVIE)
            sUriMatcher.addURI(AUTHORITY, TABLE_TV, TV)

            sUriMatcher.addURI(AUTHORITY, "$TABLE_MOVIE/#", MOVIE_ID)
            sUriMatcher.addURI(AUTHORITY, "$TABLE_TV/#", TV_ID)
        }
    }

    private val favoriteRepository by lazy {
        FavoriteRepository(context as Context)
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        strings: Array<String>?,
        s: String?,
        strings1: Array<String>?,
        s1: String?
    ): Cursor? {
        return when (sUriMatcher.match(uri)) {
            MOVIE -> favoriteRepository.getMoviesCursor()
            TV -> favoriteRepository.getTVsCursor()
            else -> null
        }
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun delete(uri: Uri, s: String?, strings: Array<String>?): Int {
        return when (sUriMatcher.match(uri)) {
            MOVIE_ID -> {
                val res = favoriteRepository.deleteSavedMovieById(uri.lastPathSegment?.toInt() ?: 0)
                context?.contentResolver?.notifyChange(CONTENT_URI_MOVIE, null)
                return res
            }
            TV_ID -> {
                val res = favoriteRepository.deleteSavedMovieById(uri.lastPathSegment?.toInt() ?: 0)
                context?.contentResolver?.notifyChange(CONTENT_URI_TV, null)
                return res
            }
            else -> 0
        }
    }

    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        return null
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        return 0
    }
}