package xyz.neopandu.moov.data.repository

import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import xyz.neopandu.moov.models.Movie

class TVRepository {

    private val baseURL = "https://api.themoviedb.org/3/tv/"
    private val popularPath = "popular"

    private fun buildRequest(path: String = "", page: Int) =
        AndroidNetworking.get("$baseURL$path")
            .addQueryParameter("api_key", "d7a850624b27f194152ecb081f7e1da0")
            .addQueryParameter("page", page.toString())
            .build()

    fun fetchPopularTVs(page: Int = 1, callback: ResponseListener) {
        buildRequest(popularPath, page)
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    GlobalScope.launch {
                        response?.let { obj ->
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

                            callback.onResponse(page, movies)
                        }
                    }
                }

                override fun onError(anError: ANError?) {
                    callback.onError(anError)
                }
            })
    }
}