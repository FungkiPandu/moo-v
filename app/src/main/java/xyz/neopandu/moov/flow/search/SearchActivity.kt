package xyz.neopandu.moov.flow.search

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.coroutines.Job
import xyz.neopandu.moov.R
import xyz.neopandu.moov.flow.FavoriteViewModel
import xyz.neopandu.moov.flow.FavoriteViewModelFactory
import xyz.neopandu.moov.flow.detail.DetailActivity
import xyz.neopandu.moov.flow.main.OnListFragmentInteractionListener
import xyz.neopandu.moov.flow.search.ui.main.SectionsPagerAdapter
import xyz.neopandu.moov.helper.debounce
import xyz.neopandu.moov.models.Movie

class SearchActivity : AppCompatActivity(), OnListFragmentInteractionListener {

    private val favoriteViewModel by lazy {
        ViewModelProviders.of(this, FavoriteViewModelFactory(application))
            .get(FavoriteViewModel::class.java)
    }

    private val searchViewModel by lazy {
        ViewModelProviders.of(this).get(SearchViewModel::class.java)
    }

    private var textChangedJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        val movieType = intent.getStringExtra("movieType")
        viewPager.currentItem = if (movieType == Movie.MovieType.TV_SHOW.name) 1 else 0

        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    doSearch(query)
                    return true
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { query ->
                    textChangedJob?.cancel()
                    textChangedJob = debounce {
                        doSearch(query)
                    }
                    return true
                }
                return true
            }
        })
    }

    override fun onStart() {
        super.onStart()
        search_view.requestFocus()
    }

    private fun doSearch(query: String) {
        searchViewModel.search(query)
    }

    override fun onListFragmentInteraction(item: Movie) {
        val intent = Intent(this, DetailActivity::class.java)
            .putExtra(DetailActivity.EXTRA_MOVIE_KEY, item)
        startActivity(intent)
    }

    override fun toggleFavoriteButtonClicked(item: Movie) {
        favoriteViewModel.toggleFavorite(item)
    }
}