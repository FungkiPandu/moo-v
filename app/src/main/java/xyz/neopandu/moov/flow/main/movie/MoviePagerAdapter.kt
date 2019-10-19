package xyz.neopandu.moov.flow.main.movie

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import xyz.neopandu.moov.flow.main.movieList.MovieListFragment
import xyz.neopandu.moov.helper.getStringResTitleOf


class MoviePagerAdapter(
    private val context: Context,
    private val endpoints: List<MovieListFragment.MovieEndpoint>,
    fm: FragmentManager
) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {


    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a SearchFragment (defined as a static inner class below).
        return MovieListFragment.newInstance(endpoints[position])
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(getStringResTitleOf(endpoints[position]))
    }

    override fun getCount(): Int {
        return endpoints.size
    }
}