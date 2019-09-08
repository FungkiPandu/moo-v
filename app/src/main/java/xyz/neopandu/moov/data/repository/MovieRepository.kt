package xyz.neopandu.moov.data.repository

import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import xyz.neopandu.moov.data.tmdbKey
import xyz.neopandu.moov.helper.TMDBHelper
import xyz.neopandu.moov.models.Meta
import xyz.neopandu.moov.models.Movie
import java.util.*

class MovieRepository {

    private fun doRequest(url: String, callback: ResponseListener) {
        Log.e("doRequest", url)
        AndroidNetworking.get(url).addQueryParameter("api_key", tmdbKey).build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    response?.let { obj ->
                        val meta = parseMeta(obj)
                        print("total_pages: ${meta.totalPages}")
                        GlobalScope.launch {
                            callback.onResponse(meta, parseMovies(obj))
                        }
                    }
                }

                override fun onError(anError: ANError?) {
                    Log.e("MovieRepository", "$url => " + anError?.errorBody)
                    callback.onError(anError)
                }
            })
    }

    private fun parseMeta(obj: JSONObject) = Meta(
        obj.getInt("page"),
        obj.getInt("total_results"),
        obj.getInt("total_pages")
    )

    private fun parseMovies(obj: JSONObject): List<Movie> {
        val resultArray = obj.getJSONArray("results")
        val size = resultArray.length()
        val movies = mutableListOf<Movie>()
        try {
            for (i in 0 until size) {
                val resObj = resultArray.getJSONObject(i)
                val objId = resObj.getInt("id")
                movies.add(
                    Movie(
                        id = objId,
                        title = resObj.getString("title"),
                        oriLang = resObj.getString("original_language"),
                        oriTitle = resObj.getString("original_title"),
                        description = resObj.getString("overview"),
                        posterPath = resObj.getString("poster_path"),
                        bannerPath = resObj.getString("backdrop_path"),
                        releaseDate = resObj.getString("release_date"),
                        score = resObj.getDouble("vote_average") * 10,
                        popularity = resObj.getDouble("popularity"),
                        movieType = Movie.MovieType.MOVIE,
                        isFavorite = false
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return movies
    }

    fun fetchPopularMovies(page: Int = 1, callback: ResponseListener) {
        val url = TMDBHelper.tmdbURL.Movie.Popular().setPage(page).url
        doRequest(url, callback)
    }

    fun fetchNowPlaying(page: Int = 1, callback: ResponseListener) {
        val url = TMDBHelper.tmdbURL.Movie.NowPlaying().setPage(page).url
        doRequest(url, callback)
    }

    fun fetchUpcoming(page: Int = 1, callback: ResponseListener) {
        val url = TMDBHelper.tmdbURL.Movie.Upcoming().setPage(page).url
        doRequest(url, callback)
    }

    fun fetchWithCostummUrl(url: String, page: Int = 1, callback: ResponseListener) {
        val newUrl = TMDBHelper.tmdbURL.Costum(url).setPage(page).url
        doRequest(newUrl, callback)
    }

    fun searchMovie(query: String, page: Int = 1, callback: ResponseListener) {
        val url = TMDBHelper.tmdbURL.Search.Movie(query).setPage(page).url
        doRequest(url, callback)
    }

    fun discoverReleaseToday(page: Int = 1, callback: ResponseListener) {
        val discoverMovie = TMDBHelper.tmdbURL.Discover.Movie()
        val today = Date()
        discoverMovie.setPrimaryReleaseDateStart(today).setPrimaryReleaseDateEnd(today)
        val url = discoverMovie.setPage(page).url
        doRequest(url, callback)
    }
}