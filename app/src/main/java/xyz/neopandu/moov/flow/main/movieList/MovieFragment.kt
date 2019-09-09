package xyz.neopandu.moov.flow.main.movieList

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import xyz.neopandu.moov.R
import xyz.neopandu.moov.adapters.MyMovieRecyclerViewAdapter
import xyz.neopandu.moov.flow.FavoriteViewModel
import xyz.neopandu.moov.flow.FavoriteViewModelFactory
import xyz.neopandu.moov.flow.main.MainViewModel
import xyz.neopandu.moov.flow.main.OnListFragmentInteractionListener
import xyz.neopandu.moov.flow.search.SearchActivity

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [OnListFragmentInteractionListener] interface.
 */
class MovieFragment : Fragment() {

    private val viewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)
    }
    private val favoriteViewModel by lazy {
        ViewModelProviders.of(
            requireActivity(),
            FavoriteViewModelFactory(requireActivity().application)
        ).get(FavoriteViewModel::class.java)
    }
    private var type: FragmentType = FragmentType.MOVIE
    private var listener: OnListFragmentInteractionListener? = null
    private var adapter: MyMovieRecyclerViewAdapter? = null
    private lateinit var movieList: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var appbar: AppBarLayout
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            type = FragmentType.valueOf(it.getString(ARG_TYPE) ?: "MOVIE")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_movie_list, container, false)

        appbar = view.findViewById(R.id.appbar)
        toolbar = view.findViewById(R.id.toolbar)
        movieList = view.findViewById(R.id.movie_list)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)

        initToolbar()
        initRecyclerView()
        loadData()

        return view
    }

    private fun initToolbar() {
        when (type) {
            FragmentType.MOVIE -> {
                appbar.visibility = View.VISIBLE
                toolbar.setTitle(R.string.title_movie)
                setupToolbar()
            }
            FragmentType.TV_SHOW -> {
                appbar.visibility = View.VISIBLE
                toolbar.setTitle(R.string.title_tv_show)
                setupToolbar()
            }
            FragmentType.FAVORITE_TV, FragmentType.FAVORITE_MOVIE -> {
                appbar.visibility = View.GONE
            }
        }
    }

    private fun setupToolbar() {
        toolbar.setSubtitle(R.string.app_name)
        toolbar.inflateMenu(R.menu.main_menu)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_change_settings -> {
                    val mIntent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                    startActivity(mIntent)
                }
                R.id.action_search -> {
                    startActivity(Intent(requireActivity(), SearchActivity::class.java))
                }
            }
            super.onOptionsItemSelected(it)
        }
    }

    private fun initRecyclerView() {
        movieList.layoutManager = LinearLayoutManager(context)
        adapter = MyMovieRecyclerViewAdapter(requireContext(), listener)
        movieList.adapter = this@MovieFragment.adapter
    }

    private fun loadData() {
        when (type) {
            FragmentType.MOVIE -> {
                viewModel.showMovieLoading.observe(this, Observer {
                    swipeRefreshLayout.isRefreshing = it
                })
                viewModel.movies.observe(viewLifecycleOwner, Observer {
                    adapter?.updateValues(it)
                })
                swipeRefreshLayout.setOnRefreshListener {
                    viewModel.fetchMovieList()
                }
                favoriteViewModel.favoriteMovies.observe(viewLifecycleOwner, Observer {
                    adapter?.updateFavorites(it)
                })
            }
            FragmentType.TV_SHOW -> {
                viewModel.showTVLoading.observe(this, Observer {
                    swipeRefreshLayout.isRefreshing = it
                })
                viewModel.tvShows.observe(viewLifecycleOwner, Observer {
                    adapter?.updateValues(it)
                    adapter?.emptyDrawable = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_live_tv_white_24dp
                    )
                })
                swipeRefreshLayout.setOnRefreshListener {
                    viewModel.fetchTvShowList()
                }
                favoriteViewModel.favoriteTVs.observe(viewLifecycleOwner, Observer {
                    adapter?.updateFavorites(it)
                })
            }
            FragmentType.FAVORITE_MOVIE -> {
                favoriteViewModel.favoriteMovies.observe(viewLifecycleOwner, Observer {
                    adapter?.updateValues(it)
                    adapter?.updateFavorites(it)
                })
                swipeRefreshLayout.isEnabled = false
            }
            FragmentType.FAVORITE_TV -> {
                favoriteViewModel.favoriteTVs.observe(viewLifecycleOwner, Observer {
                    adapter?.updateValues(it)
                    adapter?.updateFavorites(it)
                    adapter?.emptyDrawable = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_live_tv_white_24dp
                    )
                })
                swipeRefreshLayout.isEnabled = false
            }
        }


        movieList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                recyclerView.layoutManager?.let {
                    val visibleItemCount = it.childCount
                    val totalItemCount = it.itemCount
                    val firstVisibleItemPosition = (it as LinearLayoutManager).findFirstVisibleItemPosition()

                    if (!swipeRefreshLayout.isRefreshing) {
                        if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                            fetchNextPage()
                        }
                    }
                }

            }
        })
    }

    private fun fetchNextPage() {
        when (type) {
            FragmentType.MOVIE -> viewModel.fetchNextMovieList()
            FragmentType.TV_SHOW -> viewModel.fetchNextTvShowList()
            else -> {
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    enum class FragmentType {
        MOVIE, TV_SHOW, FAVORITE_MOVIE, FAVORITE_TV
    }

    companion object {
        const val ARG_TYPE = "type"

        @JvmStatic
        fun newInstance(type: FragmentType) =
            MovieFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TYPE, type.name)
                }
            }
    }
}
