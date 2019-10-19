package xyz.neopandu.moov.flow.main.movie

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import xyz.neopandu.moov.R
import xyz.neopandu.moov.flow.main.OnListFragmentInteractionListener
import xyz.neopandu.moov.flow.main.movieList.MovieListFragment
import xyz.neopandu.moov.flow.search.SearchActivity
import xyz.neopandu.moov.flow.setting.PreferenceActivity
import xyz.neopandu.moov.models.Movie

class MovieFragment : Fragment() {
    private var listener: OnListFragmentInteractionListener? = null

    private lateinit var appbar: AppBarLayout
    private lateinit var toolbar: Toolbar
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var sections: List<MovieListFragment.MovieEndpoint>
    private lateinit var pageTitle: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_movie, container, false)
        appbar = view.findViewById(R.id.movie_appbar)
        toolbar = view.findViewById(R.id.movie_toolbar)
        viewPager = view.findViewById(R.id.movie_view_pager)
        tabLayout = view.findViewById(R.id.movie_tab_layout)

        tabLayout.setupWithViewPager(viewPager)

        setupToolbar()

        val sectionsPagerAdapter =
            MoviePagerAdapter(requireContext(), sections, childFragmentManager)

        viewPager.adapter = sectionsPagerAdapter

        return view
    }

    private fun setupToolbar() {
        with(toolbar) {
            title = pageTitle
            setSubtitle(R.string.app_name)
            inflateMenu(R.menu.main_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_search -> {
                        startActivity(Intent(requireActivity(), SearchActivity::class.java))
                    }
                    R.id.action_preferences -> {
                        startActivity(Intent(requireActivity(), PreferenceActivity::class.java))
                    }
                }
                super.onOptionsItemSelected(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getStringArray(ARG_ENDPOINTS)?.let {
            sections = it.map { MovieListFragment.MovieEndpoint.valueOf(it) }
        }
        arguments?.getString(ARG_TITLE, getString(R.string.app_name))?.let {
            val type = Movie.MovieType.valueOf(it)
            pageTitle = when (type) {
                Movie.MovieType.MOVIE -> requireContext().getString(R.string.title_movie)
                Movie.MovieType.TV_SHOW -> requireContext().getString(R.string.title_tv_show)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    companion object {
        const val ARG_ENDPOINTS = "endpoints"
        const val ARG_TITLE = "title"

        @JvmStatic
        fun newInstance(
            movieType: Movie.MovieType,
            endpoints: List<MovieListFragment.MovieEndpoint>
        ) =
            MovieFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, movieType.name)
                    putStringArray(ARG_ENDPOINTS, endpoints.map { it.name }.toTypedArray())
                }
            }
    }
}
