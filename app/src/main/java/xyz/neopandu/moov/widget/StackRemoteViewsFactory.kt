package xyz.neopandu.moov.widget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.bumptech.glide.Glide
import xyz.neopandu.moov.R
import xyz.neopandu.moov.data.repository.FavoriteRepository
import xyz.neopandu.moov.helper.toMovies
import xyz.neopandu.moov.models.Movie
import xyz.neopandu.moov.widget.StackWidgetService.Companion.EXTRA_ITEM


class StackRemoteViewsFactory(
    private val applicationContext: Context,
    private val movieType: Movie.MovieType
) :
    RemoteViewsService.RemoteViewsFactory {

    private val widgetImages = mutableListOf<Movie>()
    private val favoriteRepository = FavoriteRepository(applicationContext)

    override fun onCreate() {}

    override fun getLoadingView(): RemoteViews {
        return RemoteViews(applicationContext.packageName, R.layout.widget_item_loading)
    }

    override fun getItemId(position: Int): Long {
        return widgetImages[position].id.toLong()
    }

    override fun onDataSetChanged() {
        val cursor =
            if (movieType == Movie.MovieType.MOVIE) favoriteRepository.getMoviesCursor()
            else favoriteRepository.getTVsCursor()
        widgetImages.clear()
        widgetImages.addAll(cursor.toMovies().filter { it.bannerPath != "null" })
        cursor.close()
    }

    override fun hasStableIds() = true

    override fun getViewAt(position: Int): RemoteViews {
        val rv = RemoteViews(applicationContext.packageName, R.layout.widget_item)
        val item = widgetImages[position]
        val url = applicationContext.getString(R.string.banner_base_url) + item.bannerPath
        val banner = Glide.with(applicationContext).asBitmap().load(url).submit().get()
        rv.setImageViewBitmap(R.id.imageView, banner)

        val extras = Bundle()
        extras.putBundle(EXTRA_ITEM, item.asBundle())
        val fillInIntent = Intent()
        fillInIntent.putExtras(extras)

        rv.setOnClickFillInIntent(R.id.imageView, fillInIntent)
        return rv
    }

    override fun getCount() = widgetImages.size

    override fun getViewTypeCount() = 1

    override fun onDestroy() {}

}