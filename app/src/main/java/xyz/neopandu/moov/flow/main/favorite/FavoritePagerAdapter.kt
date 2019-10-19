package xyz.neopandu.moov.flow.main.favorite

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import xyz.neopandu.moov.R
import xyz.neopandu.moov.models.Movie

class FavoritePagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment = when (position) {
        0 -> FavoriteListFragment.newInstance(Movie.MovieType.MOVIE)
        else -> FavoriteListFragment.newInstance(Movie.MovieType.TV_SHOW)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> context.getString(R.string.title_movie)
            1 -> context.getString(R.string.title_tv_show)
            else -> ""
        }
    }

    override fun getCount(): Int = 2
}