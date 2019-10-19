package xyz.neopandu.moov.flow.main.favorite

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import xyz.neopandu.moov.models.Movie


class FavoriteListFragment : Fragment() {

    companion object {
        const val TYPE_EXTRA = "movie_type"

        @JvmStatic
        fun newInstance(type: Movie.MovieType): FavoriteListFragment {
            return FavoriteListFragment().apply {
                arguments = Bundle().apply {
                    putString(TYPE_EXTRA, type.name)
                }
            }
        }
    }

    private var type: Movie.MovieType? = null
    private var listener: OnListFragmentInteractionListener? = null
    private val adapter by lazy { MyMovieRecyclerViewAdapter(requireContext(), listener) }
    private lateinit var movieList: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

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
        arguments?.getString(TYPE_EXTRA)?.let {
            type = Movie.MovieType.valueOf(it)
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

        swipeRefreshLayout.isEnabled = false

        val favoriteLiveData =
            if (type == Movie.MovieType.MOVIE) favoriteViewModel.favoriteMovies
            else favoriteViewModel.favoriteTVs

        favoriteLiveData.observe(requireActivity(), Observer {
            adapter.updateValues(it)
            adapter.updateFavorites(it)
        })

        return view
    }
}