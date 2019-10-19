package xyz.neopandu.moov.flow.main.movieList

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
import xyz.neopandu.moov.helper.getStringResTitleOf
import xyz.neopandu.moov.models.Movie


class MovieListFragment : Fragment() {

    enum class MovieEndpoint {
        POPULAR_MOVIE, RELEASE_TODAY, NOW_PLAYING, UPCOMING, //movie
        POPULAR_TV, AIRING_TODAY, ON_THE_AIR //tv
    }

    companion object {
        const val ENDPOINT_EXTRA = "movie_endpoint"

        @JvmStatic
        fun newInstance(endpoint: MovieEndpoint): MovieListFragment {
            return MovieListFragment().apply {
                arguments = Bundle().apply {
                    putString(ENDPOINT_EXTRA, endpoint.name)
                }
            }
        }
    }

    private var endpoint: MovieEndpoint? = null
    private var listener: OnListFragmentInteractionListener? = null
    private val adapter by lazy { MyMovieRecyclerViewAdapter(requireContext(), listener) }
    private lateinit var movieList: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val viewModel by lazy {
        val endpoint = this.endpoint ?: MovieEndpoint.POPULAR_MOVIE
        ViewModelProviders.of(requireActivity()).get(endpoint.name, MovieListViewModel::class.java)
            .apply {
                movieEndpoint = endpoint
            }
    }

    private val favoriteViewModel by lazy {
        ViewModelProviders.of(
            requireActivity(),
            FavoriteViewModelFactory(requireActivity().application)
        ).get(FavoriteViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activityContext = requireActivity()
        if (activityContext is OnListFragmentInteractionListener) {
            listener = activityContext
        } else {
            throw RuntimeException("$context must implement OnListFragmentInteractionListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString(ENDPOINT_EXTRA)?.let {
            endpoint = MovieEndpoint.valueOf(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_movie_list, container, false)

        movieList = view.findViewById(R.id.movie_list)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)

        movieList.adapter = this.adapter
        movieList.layoutManager = LinearLayoutManager(requireContext())

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchInitMovieList()
        }

        movieList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                recyclerView.layoutManager?.let {
                    val visibleItemCount = it.childCount
                    val totalItemCount = it.itemCount
                    val firstVisibleItemPosition =
                        (it as LinearLayoutManager).findFirstVisibleItemPosition()

                    if (!swipeRefreshLayout.isRefreshing) {
                        if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                            viewModel.fetchNextMovieList()
                        }
                    }
                }

            }
        })

        viewModel.movies.observe(this, Observer {
            adapter.updateValues(it)
        })

        viewModel.showMovieLoading.observe(this, Observer {
            swipeRefreshLayout.isRefreshing = it
        })

        val favoriteObserver = Observer<List<Movie>> { favorites ->
            adapter.updateFavorites(favorites)
        }

        viewModel.isMovie.observe(this, Observer {
            if (it) favoriteViewModel.favoriteMovies.observe(this, favoriteObserver)
            else favoriteViewModel.favoriteTVs.observe(this, favoriteObserver)
        })

        viewModel.showError.observe(this, Observer {
            showErrorDialog(it)
        })

        return view
    }


    private fun showErrorDialog(action: () -> Unit) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.error_dialog_title))
            setMessage(
                getString(
                    R.string.error_dialog_message,
                    getString(
                        getStringResTitleOf(endpoint ?: MovieEndpoint.POPULAR_MOVIE)
                    )
                )
            )
            setPositiveButton(R.string.try_again) { _, _ ->
                action.invoke()
            }
            setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }.show()
    }
}