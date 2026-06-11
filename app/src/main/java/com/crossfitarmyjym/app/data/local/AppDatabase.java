package com.crossfitarmyjym.app.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.crossfitarmyjym.app.data.local.entity.dao.AttendanceDao;
import com.crossfitarmyjym.app.data.local.entity.dao.BookingDao;
import com.crossfitarmyjym.app.data.local.entity.dao.ExerciseDao;
import com.crossfitarmyjym.app.data.local.entity.dao.GroupDao;
import com.crossfitarmyjym.app.data.local.entity.dao.GymClassDao;
import com.crossfitarmyjym.app.data.local.entity.dao.ProfileDao;
import com.crossfitarmyjym.app.data.local.entity.dao.ResultDao;
import com.crossfitarmyjym.app.data.local.entity.dao.WodDao;
import com.crossfitarmyjym.app.data.local.entity.dao.WodExerciseDao;
import com.crossfitarmyjym.app.data.local.entity.AttendanceEntity;
import com.crossfitarmyjym.app.data.local.entity.BookingEntity;
import com.crossfitarmyjym.app.data.local.entity.ExerciseEntity;
import com.crossfitarmyjym.app.data.local.entity.FcmTokenEntity;
import com.crossfitarmyjym.app.data.local.entity.GroupEntity;
import com.crossfitarmyjym.app.data.local.entity.GymClassEntity;
import com.crossfitarmyjym.app.data.local.entity.ProfileEntity;
import com.crossfitarmyjym.app.data.local.entity.ResultEntity;
import com.crossfitarmyjym.app.data.local.entity.WodEntity;
import com.crossfitarmyjym.app.data.local.entity.WodExerciseEntity;

/**
 * Room база данных для офлайн-кэша.
 * Использует паттерн Singleton.
 */
@Database(
    entities = {
        WodEntity.class,
        GymClassEntity.class,
        BookingEntity.class,
        ExerciseEntity.class,
        ProfileEntity.class,
        GroupEntity.class,
        WodExerciseEntity.class,
        ResultEntity.class,
        AttendanceEntity.class,
        FcmTokenEntity.class
    },
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract WodDao wodDao();
    public abstract GymClassDao gymClassDao();
    public abstract BookingDao bookingDao();
    public abstract ExerciseDao exerciseDao();
    public abstract ProfileDao profileDao();
    public abstract GroupDao groupDao();
    public abstract WodExerciseDao wodExerciseDao();
    public abstract ResultDao resultDao();
    public abstract AttendanceDao attendanceDao();

    private static volatile AppDatabase instance;

    private static final String DATABASE_NAME = "crossfit_army_jym_cache";

    public static AppDatabase getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            // Здесь можно добавить начальные данные при необходимости
                        }
                    })
                    .build();
                }
            }
        }
        return instance;
    }

    /**
     * Очистить все кэшированные данные.
     */
    public static void clearInstance() {
        if (instance != null && instance.isOpen()) {
            instance.close();
        }
        instance = null;
    }
}