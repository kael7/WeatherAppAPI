package com.example.weatherappapi;

import android.app.Application;

import androidx.room.Room;

import com.example.weatherappapi.dao.WeatherDao;
import com.example.weatherappapi.database.WeatherDatabase;

// Паттерн Singleton, наследуем класс Application, создаём базу данных
// в методе onCreate
public class App extends Application {
    private static App instance;

    // База данных
    private WeatherDatabase db;

    // Получаем объект приложения
    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Сохраняем объект приложения (для Singleton’а)
        instance = this;

        // Строим базу
        db = Room.databaseBuilder(
                getApplicationContext(),
                WeatherDatabase.class,
                "weather_database")
                .allowMainThreadQueries() //Только для примеров и тестирования.
                .build();
    }

    // Получаем EducationDao для составления запросов
    public WeatherDao getWeatherDao() {
        return db.getWeatherDao();
    }

}
