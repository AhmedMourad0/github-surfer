package dev.ahmedmourad.githubsurfer.users.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.ahmedmourad.githubsurfer.users.local.entities.*
import dev.ahmedmourad.githubsurfer.users.local.daos.UsersDao

@Database(entities = [UserEntity::class], version = 1)
internal abstract class UsersDatabase : RoomDatabase() {

    abstract fun usersDao(): UsersDao

    companion object {

        @Volatile
        private var INSTANCE: UsersDatabase? = null

        fun getInstance(appCtx: Context) = INSTANCE ?: synchronized(UsersDatabase::class.java) {
            INSTANCE ?: buildDatabase(appCtx).also { INSTANCE = it }
        }

        private fun buildDatabase(appCtx: Context) = Room.databaseBuilder(
            appCtx,
            UsersDatabase::class.java,
            LocalContract.DATABASE_NAME
        ).build()
    }
}
