package xyz.neopandu.moov.adapters


import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.row_item_movie.view.*
import kotlinx.android.synthetic.main.view_empty_list.view.*
import xyz.neopandu.moov.R
import xyz.neopandu.moov.flow.main.OnListFragmentInteractionListener
import xyz.neopandu.moov.models.Movie
import java.util.*

/**
 * [RecyclerView.Adapter] that can display a [Movie] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 */
class MyMovieRecyclerViewAdapter(
    private val context: Context,
    private val mListener: OnListFragmentInteractionListener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener
    private val mValues: MutableList<Movie> = mutableListOf()
    private val mFavorites: MutableList<Movie> = mutableListOf()

    private val mOnFavoriteButtonClicked: (Movie) -> Unit

    var emptyDrawable: Drawable? = null
    var emptyMessage: String? = null

    companion object {
        const val EMPTY_LIST_VIEWTYPE = 0
        const val BANNER_VIEWTYPE = 1
    }

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Movie
            mListener?.onListFragmentInteraction(item)
        }

        mOnFavoriteButtonClicked = {
            mListener?.toggleFavoriteButtonClicked(it)
        }
    }

    fun updateValues(items: List<Movie>) {
        mValues.clear()
        mValues.addAll(items)
        updateFavoriteButton()
    }

    fun updateFavorites(favorites: List<Movie>) {
        mFavorites.clear()
        mFavorites.addAll(favorites)
        updateFavoriteButton()
    }

    private fun updateFavoriteButton() {
        for (i in 0 until mValues.size) {
            mValues[i].isFavorite = mFavorites.any { it.id == mValues[i].id }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            EMPTY_LIST_VIEWTYPE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.view_empty_list, parent, false)
                EmptyViewHolder(view)
            }
            BANNER_VIEWTYPE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_item_movie, parent, false)
                ViewHolder(view)
            }
            else -> throw RuntimeException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is EmptyViewHolder) {
            holder.bind()
            return
        }
        val item = mValues[position]
        if (holder is ViewHolder) holder.bind(item)
    }

    override fun getItemViewType(position: Int): Int {
        return if (mValues.isEmpty()) EMPTY_LIST_VIEWTYPE else BANNER_VIEWTYPE
    }

    override fun getItemCount(): Int = if (mValues.isEmpty()) 1 else mValues.size

    inner class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageView = view.error_image
        private val textView = view.error_view_text

        fun bind() {
            emptyDrawable?.let {
                imageView.setImageDrawable(it)
            }
            emptyMessage?.let {
                textView.text = it
            }
        }
    }

    inner class ViewHolder(private val mView: View) : RecyclerView.ViewHolder(mView) {
        private val mBanner: ImageView = mView.img_banner
        private val mTitle: TextView = mView.tv_title
        private val mOriTitle: TextView = mView.tv_original_title
        private val mScore: TextView = mView.tv_score
        private val mFavoriteButton: ImageButton = mView.btn_favorite

        fun bind(item: Movie) {
            val releaseYear = item.releaseDate.replace("–", "-").split("-")[0]
            val title = item.title + if (releaseYear.isNotBlank()) " ($releaseYear)" else ""
            mTitle.text = title
            var bannerUrl = context.getString(R.string.banner_base_url) + item.bannerPath
            if (item.bannerPath == "null" && item.posterPath != "null") {
                bannerUrl = context.getString(R.string.poster_base_url) + item.posterPath
            }
            Glide.with(context).load(bannerUrl).into(mBanner)

            if (item.oriLang != "en") {
                mOriTitle.visibility = View.VISIBLE
                val oriTitle = "[${item.oriLang.toUpperCase(Locale.ENGLISH)}] ${item.oriTitle}"
                mOriTitle.text = oriTitle
            } else {
                mOriTitle.visibility = View.GONE
            }

            mScore.text = item.score.toInt().toString()

            mView.tag = item
            mView.setOnClickListener(mOnClickListener)
            mFavoriteButton.setOnClickListener {
                mOnFavoriteButtonClicked.invoke(item)
            }

            mFavoriteButton.setImageResource(
                if (item.isFavorite) R.drawable.ic_favorite_red_24dp
                else R.drawable.ic_favorite_border_white_24dp
            )
        }
    }
}
