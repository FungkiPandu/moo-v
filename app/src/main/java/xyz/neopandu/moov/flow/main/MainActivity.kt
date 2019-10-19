package xyz.neopandu.moov.flow.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import xyz.neopandu.moov.R
import xyz.neopandu.moov.flow.FavoriteViewModel
import xyz.neopandu.moov.flow.FavoriteViewModelFactory
import xyz.neopandu.moov.flow.detail.DetailActivity
import xyz.neopandu.moov.flow.main.favorite.FavoriteFragment
import xyz.neopandu.moov.flow.main.movie.MovieFragment
import xyz.neopandu.moov.flow.main.movieList.MovieListFragment
import xyz.neopandu.moov.models.Movie


class MainActivity : AppCompatActivity(), OnListFragmentInteractionListener {

    private val favoriteViewModel by lazy {
        ViewModelProviders.of(this, FavoriteViewModelFactory(application))
            .get(FavoriteViewModel::class.java)
    }
    private val movieFragment by lazy {
        MovieFragment.newInstance(
            Movie.MovieType.MOVIE,
            listOf(
                MovieListFragment.MovieEndpoint.POPULAR_MOVIE,
                MovieListFragment.MovieEndpoint.RELEASE_TODAY,
                MovieListFragment.MovieEndpoint.NOW_PLAYING,
                MovieListFragment.MovieEndpoint.UPCOMING
            )
        )
    }
    private val tvShowFragment by lazy {
        MovieFragment.newInstance(
            Movie.MovieType.TV_SHOW,
            listOf(
                MovieListFragment.MovieEndpoint.POPULAR_TV,
                MovieListFragment.MovieEndpoint.AIRING_TODAY,
                MovieListFragment.MovieEndpoint.ON_THE_AIR
            )
        )
    }
    private val favoriteFragment = FavoriteFragment()
    private val fragmentManager by lazy { supportFragmentManager }
    private var pageFragment: Fragment = movieFragment

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    pageFragment = movieFragment
                }
                R.id.navigation_dashboard -> {
                    pageFragment = tvShowFragment
                }
                R.id.navigation_favorite -> {
                    pageFragment = favoriteFragment
                }
            }
            fragmentManager.beginTransaction().replace(R.id.fragment_place_holder, pageFragment)
                .commit()
            true
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nav_view.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        if (savedInstanceState == null) {
            nav_view.selectedItemId = nav_view.menu.getItem(0).itemId
        } else {
            pageFragment = fragmentManager.getFragment(savedInstanceState, "page") as Fragment
            nav_view.selectedItemId = savedInstanceState.getInt("selectedId")
        }

        //cold start
        favoriteViewModel
    }

    override fun onSaveInstanceState(outState: Bundle) {
        supportFragmentManager.putFragment(outState, "page", pageFragment)
        outState.putInt("selectedId", nav_view.selectedItemId)

        super.onSaveInstanceState(outState)
    }

    override fun onListFragmentInteraction(item: Movie) {
        val intent =
            Intent(this, DetailActivity::class.java).putExtra(DetailActivity.EXTRA_MOVIE_KEY, item)
        startActivity(intent)
    }

    override fun toggleFavoriteButtonClicked(item: Movie) {
        favoriteViewModel.toggleFavorite(item)
    }
}
