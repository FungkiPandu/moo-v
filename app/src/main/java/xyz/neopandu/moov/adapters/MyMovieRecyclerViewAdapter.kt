package xyz.neopandu.moov.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.row_item_movie.view.*
import xyz.neopandu.moov.R
import xyz.neopandu.moov.flow.main.OnListFragmentInteractionListener
import xyz.neopandu.moov.models.Movie

/**
 * [RecyclerView.Adapter] that can display a [Movie] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 */
class MyMovieRecyclerViewAdapter(
    private val context: Context,
    private val mListener: OnListFragmentInteractionListener?
) : RecyclerView.Adapter<MyMovieRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener
    private val mValues: MutableList<Movie> = mutableListOf()
    private val mFavorites: MutableList<Movie> = mutableListOf()

    private val mOnFavoriteButtonClicked: (Movie) -> Unit

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Movie
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }

        mOnFavoriteButtonClicked = {
            mListener?.toggleFavoriteButtonClicked(it)
        }
    }

    fun updateValues(items: List<Movie>) {
        mValues.clear()
        mValues.addAll(items)
        notifyDataSetChanged()
    }

    fun updateFavorites(favorites: List<Movie>) {
        mFavorites.clear()
        mFavorites.addAll(favorites)
        for(i in 0 until mValues.size) {
            val movie = mValues[i]
            mValues[i] = movie.copy(isFavorite = mFavorites.any { it.id == movie.id })
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_item_movie, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(private val mView: View) : RecyclerView.ViewHolder(mView) {
        private val mBanner: ImageView = mView.img_banner
        private val mTitle: TextView = mView.tv_title
        private val mOriTitle: TextView = mView.tv_original_title
        private val mScore: TextView = mView.tv_score
        private val mFavoriteButton: ImageButton = mView.btn_favorite

        fun bind(item: Movie) {
            val releaseYear = item.releaseDate.replace("–", "-").split("-")[0]
            val title = item.title + " (" + releaseYear + ")"
            mTitle.text = title
            Glide.with(context).load(context.getString(R.string.banner_base_url) + item.bannerPath).into(mBanner)

            if (item.oriLang != "en") {
                mOriTitle.visibility = View.VISIBLE
                val oriTitle = "[${item.oriLang.toUpperCase()}] ${item.oriTitle}"
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
