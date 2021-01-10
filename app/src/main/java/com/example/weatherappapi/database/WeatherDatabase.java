package com.example.weatherappapi.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.weatherappapi.dao.WeatherDao;
import com.example.weatherappapi.model.WeatherHistory;

@Database(entities = {WeatherHistory.class}, version = 1)
public abstract  class WeatherDatabase extends RoomDatabase {
    public abstract WeatherDao getWeatherDao();
}
