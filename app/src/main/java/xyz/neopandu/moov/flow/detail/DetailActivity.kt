package xyz.neopandu.moov.flow.detail

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*
import xyz.neopandu.moov.R
import xyz.neopandu.moov.models.Movie

class DetailActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProviders.of(this, DetailViewModelFactory(application)).get(DetailViewModel::class.java)
    }

    private val item by lazy { intent?.extras?.getParcelable<Movie>(EXTRA_MOVIE_KEY) }

    private var menuItem: MenuItem? = null
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(detail_toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bannerUrl = getString(R.string.banner_base_url) + item?.posterPath
        Glide.with(this).load(bannerUrl).into(img_detail_backdrop)

        fab_up.setOnClickListener {
            detail_appbar.setExpanded(false, true)
        }

        /**
         * hide / remove title in [detail_toolbar_layout] when expanded / expanding, and show title only when collapsed
         */
        var isShow = true
        var scrollRange = -1
        detail_appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (scrollRange == -1) {
                scrollRange = appBarLayout.totalScrollRange
            }
            if (scrollRange + verticalOffset == 0) {
                detail_toolbar_layout.title = item?.title
                isShow = true
            } else if (isShow) {
                detail_toolbar_layout.title =
                    " " //careful there should a space between double quote otherwise it wont work
                isShow = false
            }
        })

        /**
         * load movie detail from intent
         */
        item?.let {
            tv_detail_title.text = it.title
            tv_detail_original_title.text = it.oriTitle
            tv_detail_release_date.text = it.releaseDate.replace("–", "-")
            tv_detail_popularity.text = it.popularity.toString()
            val score = it.score.toInt().toString() + "/100"
            tv_detail_score.text = score
            tv_detail_overview.text = it.description

            viewModel.checkIsFavorite(it)
        }
    }

    private fun listenIsFavorite() {
        viewModel.isFavorite.observe(this, Observer {
            isFavorite = it
            updateFavoriteState()
        })
    }

    private fun updateFavoriteState() {
        menuItem?.setIcon(
            if (isFavorite) R.drawable.ic_favorite_red_24dp
            else R.drawable.ic_favorite_border_white_24dp
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        menuItem = menu?.getItem(0)
        listenIsFavorite()
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem?): Boolean {
        when (menuItem?.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.action_favorite -> {
                item?.let {
                    viewModel.toggleFavorite(it)
                }
            }

        }
        return false
    }

    companion object {
        const val EXTRA_MOVIE_KEY = "extra_movie"
    }
}
