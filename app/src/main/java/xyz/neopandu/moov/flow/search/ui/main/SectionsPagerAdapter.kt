package xyz.neopandu.moov.flow.search.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import xyz.neopandu.moov.R
import xyz.neopandu.moov.models.Movie

private val TAB_TITLES = arrayOf(
    R.string.title_movie,
    R.string.title_tv_show
)

class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a SearchFragment (defined as a static inner class below).
        return SearchFragment.newInstance(if (position == 0) Movie.MovieType.MOVIE else Movie.MovieType.TV_SHOW)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return 2
    }
}