package xyz.neopandu.moov.widget

import android.content.Intent
import android.widget.RemoteViewsService
import xyz.neopandu.moov.data.database.DBContract.MovieColumns.Companion.MOVIE_TYPE
import xyz.neopandu.moov.models.Movie

class StackWidgetService : RemoteViewsService() {
    companion object {
        const val TOAST_ACTION = "com.dicoding.picodiploma.TOAST_ACTION"
        const val EXTRA_ITEM = "com.dicoding.picodiploma.EXTRA_ITEM"
    }

    override fun onGetViewFactory(p0: Intent?): RemoteViewsFactory {
        val typeString = p0?.getStringExtra(MOVIE_TYPE) ?: Movie.MovieType.MOVIE.name
        val type = Movie.MovieType.valueOf(typeString)
        return StackRemoteViewsFactory(this.applicationContext, type)
    }
}