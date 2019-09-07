package xyz.neopandu.moov.helper

import xyz.neopandu.moov.data.tmdbKey
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class TMDBHelper {
    abstract class BaseTmdbURL {
        val baseUrl = "https://api.themoviedb.org/3"
        var url: String = baseUrl

        init {
            addQuery("key", tmdbKey)
        }

        private fun removeExistingQuery(key: String): BaseTmdbURL {
            val idx = url.indexOf(key)
            if (idx < 0) return this
            val endIdx = url.indexOf("&", idx)
            url.removeRange(idx, endIdx)
            return this
        }

        fun addQuery(key: String, data: String, encodeData: Boolean = false): BaseTmdbURL {
            removeExistingQuery(key)
            if (!url.contains('?')) url += "?"
            if (url.last() != '?') url != "&"
            val newQuery = "$key=${if (encodeData) URLEncoder.encode(data, "UTF-8") else data}"
            url += newQuery
            return this
        }

        /**
         * Specify which page to query.
         * minimum: 1. maximum: 1000. default: 1
         */
        fun setPage(page: Int): BaseTmdbURL {
            addQuery("page", page.toString())
            return this
        }

        /**
         * Add language parameter.
         * Pass a ISO 639-1 value to display translated data for the fields that support it.
         * minLength: 2. pattern: ([a-z]{2})-([A-Z]{2}). default: en-US
         */
        fun setLanguage(language: String): BaseTmdbURL {
            val isValid = Regex("([a-z]{2})-([A-Z]{2})").matches(language)
            if (isValid) addQuery("language", language)
            return this
        }
    }

    abstract class BaseDiscover : BaseTmdbURL() {

        fun setPrimaryReleaseDateStart(date: Date): BaseDiscover {
            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
            addQuery("primary_release_date.gte", formattedDate)
            return this
        }

        fun setPrimaryReleaseDateEnd(date: Date): BaseDiscover {
            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
            addQuery("primary_release_date.lte", formattedDate)
            return this
        }

        fun setTimeZone(timezone: String): BaseDiscover {
            addQuery("timezone", timezone)
            return this
        }
    }

    class tmdbURL {
        companion object {
            var url = "https://api.themoviedb.org/3"
        }

        class Costum(url: String) : BaseTmdbURL() {
            init {
                this.url = url
            }
        }

        class Search {
            class TV(query: String) : BaseTmdbURL() {
                init {
                    this.url = "$baseUrl/search/tv"
                    this.addQuery("query", query, true)
                }
            }

            class Movie(query: String) : BaseTmdbURL() {
                init {
                    this.url = "$baseUrl/search/movie"
                    this.addQuery("query", query, true)
                }

            }
        }

        class Discover {
            class TV : BaseDiscover() {
                init {
                    this.url = "$baseUrl/discover/tv"
                }

            }

            class Movie : BaseDiscover() {
                init {
                    this.url = "$baseUrl/discover/movie"
                }

            }
        }

        class Movie {
            companion object {
                var movieUrl = "$url/movie"
            }

            class Detail(movieId: Int) : BaseTmdbURL() {
                init {
                    url = "$movieUrl/$movieId"
                }
            }

            class Popular : BaseTmdbURL() {
                init {
                    url = "$movieUrl/popular"
                }
            }

            class TopRated : BaseTmdbURL() {
                init {
                    url = "$movieUrl/top_rated"
                }
            }

            class NowPlaying : BaseTmdbURL() {
                init {
                    url = "$movieUrl/now_playing"
                }
            }

            class Upcoming : BaseTmdbURL() {
                init {
                    url = "$movieUrl/upcoming"
                }
            }
        }

        class TV {
            companion object {
                var tvUrl = "$url/tv"
            }

            class Detail(tvId: Int) : BaseTmdbURL() {
                init {
                    url = "$tvUrl/$tvId"
                }
            }

            class Popular : BaseTmdbURL() {
                init {
                    url = "$tvUrl/popular"
                }
            }

            class TopRated : BaseTmdbURL() {
                init {
                    url = "$tvUrl/top_rated"
                }
            }

            class AiringToday : BaseTmdbURL() {
                init {
                    url = "$tvUrl/airing_today"
                }
            }

            class OnTheAir : BaseTmdbURL() {
                init {
                    url = "$tvUrl/on_the_air"
                }
            }
        }
    }
}