package xyz.neopandu.moov.flow.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import xyz.neopandu.moov.R
import xyz.neopandu.moov.flow.detail.DetailActivity
import xyz.neopandu.moov.flow.main.favorite.FavoriteFragment
import xyz.neopandu.moov.flow.main.movieList.MovieFragment
import xyz.neopandu.moov.models.Movie


class MainActivity : AppCompatActivity(), OnListFragmentInteractionListener {

    private val viewModel by lazy {
        ViewModelProviders.of(this, MainViewModelFactory(application)).get(MainViewModel::class.java)
    }
    private val movieFragment =
        MovieFragment.newInstance(MovieFragment.FragmentType.MOVIE)
    private val tvShowFragment =
        MovieFragment.newInstance(MovieFragment.FragmentType.TV_SHOW)
    private val favoriteFragment = FavoriteFragment()
    private val fragmentManager by lazy { supportFragmentManager }
    private var pageFragment : Fragment = movieFragment
    private var isErrorMovieShowing = false
    private var isErrorTVShowing = false

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
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
        fragmentManager.beginTransaction().replace(R.id.fragment_place_holder, pageFragment).commit()
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            viewModel.fetchMovieList(1)
            viewModel.fetchTvShowList(1)
        }

        nav_view.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        if (savedInstanceState == null) {
            nav_view.selectedItemId = nav_view.menu.getItem(0).itemId
        } else {
            pageFragment = fragmentManager.getFragment(savedInstanceState, "page") as Fragment
            nav_view.selectedItemId = savedInstanceState.getInt("selectedId")
        }

        viewModel.showError.observe(this, Observer { (movieType, action) ->
            showErrorDialog(movieType, action)
        })

        //cold start
        viewModel
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        if (outState != null) {
            supportFragmentManager.putFragment(outState, "page", pageFragment)
            outState.putInt("selectedId", nav_view.selectedItemId)
        }
        super.onSaveInstanceState(outState)
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.main_menu, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.action_change_settings) {
//            val mIntent = Intent(ACTION_LOCALE_SETTINGS)
//            startActivity(mIntent)
//        }
//        return super.onOptionsItemSelected(item)
//    }

    override fun onListFragmentInteraction(item: Movie) {
        val intent = Intent(this, DetailActivity::class.java).putExtra(DetailActivity.EXTRA_MOVIE_KEY, item)
        startActivity(intent)
        Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()
    }

    override fun toggleFavoriteButtonClicked(item: Movie) {
        viewModel.toggleFavorite(item)
    }

    private fun showErrorDialog(movieType: MovieFragment.FragmentType, action: () -> Unit) {
        if (movieType == MovieFragment.FragmentType.MOVIE && isErrorMovieShowing) return
        if (movieType == MovieFragment.FragmentType.TV_SHOW && isErrorTVShowing) return

        val ad = AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.error_dialog_title))
            setMessage(
                getString(
                    R.string.error_dialog_message,
                    getString(
                        if (movieType == MovieFragment.FragmentType.MOVIE) R.string.movie_list
                        else R.string.tv_list
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
        if (movieType == MovieFragment.FragmentType.MOVIE)
            isErrorMovieShowing = true
        else isErrorTVShowing = true
        ad.setOnDismissListener {
            if (movieType == MovieFragment.FragmentType.MOVIE)
                isErrorMovieShowing = false
            else isErrorTVShowing = false
        }
    }

}
