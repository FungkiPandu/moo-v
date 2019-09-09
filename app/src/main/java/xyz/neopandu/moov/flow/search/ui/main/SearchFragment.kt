package xyz.neopandu.moov.flow.search.ui.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import xyz.neopandu.moov.R
import xyz.neopandu.moov.adapters.MyMovieRecyclerViewAdapter
import xyz.neopandu.moov.flow.FavoriteViewModel
import xyz.neopandu.moov.flow.FavoriteViewModelFactory
import xyz.neopandu.moov.flow.main.OnListFragmentInteractionListener
import xyz.neopandu.moov.flow.search.SearchViewModel
import xyz.neopandu.moov.models.Movie

/**
 * A placeholder fragment containing a simple view.
 */
class SearchFragment : Fragment() {

    private lateinit var searchViewModel: SearchViewModel
    private lateinit var rv: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var interactionListener: OnListFragmentInteractionListener? = null
    private var movieType: Movie.MovieType = Movie.MovieType.MOVIE

    private val adapter by lazy {
        MyMovieRecyclerViewAdapter(requireContext(), interactionListener)
    }
    private val favoriteViewModel by lazy {
        ViewModelProviders.of(this, FavoriteViewModelFactory(requireActivity().application))
            .get(FavoriteViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            interactionListener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString(ARG_MOVIE_TYPE)?.let {
            movieType = Movie.MovieType.valueOf(it)
        }

        searchViewModel = ViewModelProviders.of(requireActivity()).get(SearchViewModel::class.java)

        val liveData = if (movieType == Movie.MovieType.MOVIE) searchViewModel.movieSearchResult
        else searchViewModel.tvSearchResult

        liveData.observe(requireActivity(), Observer {
            adapter.updateValues(it)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        swipeRefreshLayout = SwipeRefreshLayout(requireContext())
        rv = RecyclerView(requireContext())

        swipeRefreshLayout.addView(rv)
        swipeRefreshLayout.setOnRefreshListener {
            searchViewModel.refreshSearchResult(movieType)
        }
        initRecyclerView()
        listenLoading()
        listenFavoriteList()

        if (movieType == Movie.MovieType.TV_SHOW) {
            adapter.emptyDrawable =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_live_tv_white_24dp)
        }

        return swipeRefreshLayout
    }

    private fun initRecyclerView() {
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                recyclerView.layoutManager?.let {
                    val visibleItemCount = it.childCount
                    val totalItemCount = it.itemCount
                    val firstVisibleItemPosition =
                        (it as LinearLayoutManager).findFirstVisibleItemPosition()

                    if (!swipeRefreshLayout.isRefreshing) {
                        if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                            searchViewModel.fetchNextPage(movieType)
                        }
                    }
                }
            }
        })
    }

    private fun listenLoading() {
        val loading = if (movieType == Movie.MovieType.MOVIE) searchViewModel.movieSearchLoading
        else searchViewModel.tvSearchLoading

        loading.observe(requireActivity(), Observer {
            swipeRefreshLayout.isRefreshing = it
        })

    }

    private fun listenFavoriteList() {
        val favorites = if (movieType == Movie.MovieType.MOVIE) favoriteViewModel.favoriteMovies
        else favoriteViewModel.favoriteTVs

        favorites.observe(requireActivity(), Observer {
            adapter.updateFavorites(it)
        })
    }

    companion object {
        private const val ARG_MOVIE_TYPE = "movie_type"

        @JvmStatic
        fun newInstance(movieType: Movie.MovieType): SearchFragment {
            return SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MOVIE_TYPE, movieType.name)
                }
            }
        }
    }
}