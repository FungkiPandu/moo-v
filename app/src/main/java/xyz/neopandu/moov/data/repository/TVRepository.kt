package xyz.neopandu.moov.data.repository

import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import xyz.neopandu.moov.helper.TMDBHelper
import xyz.neopandu.moov.models.Meta
import xyz.neopandu.moov.models.Movie

class TVRepository {

    private val baseURL = "https://api.themoviedb.org/3/tv/"
    private val popularPath = "popular"

    private fun doRequest(url: String, callback: ResponseListener) {
        AndroidNetworking.get(url).build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    GlobalScope.launch {
                        response?.let { obj ->
                            callback.onResponse(parseMeta(obj), parseTVs(obj))
                        }
                    }
                }

                override fun onError(anError: ANError?) {
                    callback.onError(anError)
                }
            })
    }

    private fun parseMeta(obj: JSONObject) = Meta(
        obj.getInt("page"),
        obj.getInt("total_results"),
        obj.getInt("total_pages")
    )

    private fun parseTVs(obj: JSONObject): List<Movie> {
        val resultArray = obj.getJSONArray("results")
        val size = resultArray.length()
        val movies = mutableListOf<Movie>()
        for (i in 0 until size) {
            val resObj = resultArray.getJSONObject(i)
            val objId = resObj.getInt("id")
            movies.add(
                Movie(
                    id = objId,
                    title = resObj.getString("name"),
                    oriLang = resObj.getString("original_language"),
                    oriTitle = resObj.getString("original_name"),
                    description = resObj.getString("overview"),
                    posterPath = resObj.getString("poster_path"),
                    bannerPath = resObj.getString("backdrop_path"),
                    releaseDate = resObj.getString("first_air_date"),
                    score = resObj.getDouble("vote_average") * 10,
                    popularity = resObj.getDouble("popularity"),
                    movieType = Movie.MovieType.TV_SHOW,
                    isFavorite = false
                )
            )
        }
        return movies
    }

    fun fetchPopularTVs(page: Int = 1, callback: ResponseListener) {
        val url = TMDBHelper.tmdbURL.TV.Popular().setPage(page).url
        doRequest(url, callback)
    }

    fun fetchAiringToday(page: Int = 1, callback: ResponseListener) {
        val url = TMDBHelper.tmdbURL.TV.AiringToday().setPage(page).url
        doRequest(url, callback)
    }

    fun search(query: String, page: Int = 1, callback: ResponseListener) {
        val url = TMDBHelper.tmdbURL.Search.TV(query).setPage(page).url
        doRequest(url, callback)
    }
}