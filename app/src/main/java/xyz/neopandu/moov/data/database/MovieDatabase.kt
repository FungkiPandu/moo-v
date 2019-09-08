package xyz.neopandu.moov.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import xyz.neopandu.moov.data.FavoriteDao
import xyz.neopandu.moov.models.Movie

@Database(entities = [Movie::class], version = 1, exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @JvmStatic
        private var instance: MovieDatabase? = null
        fun getInstance(context: Context): MovieDatabase? {
            if (instance == null) {
                synchronized(MovieDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        MovieDatabase::class.java, "movie_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return instance
        }
    }
}
