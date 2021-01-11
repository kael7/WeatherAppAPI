package com.example.weatherappapi.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.weatherappapi.model.WeatherHistory;

import java.util.List;

@Dao
public interface WeatherDao {
    // Метод для добавления студента в базу данных
    // @Insert - признак добавления
    // onConflict - что делать, если такая запись уже есть
    // В данном случае просто заменим её
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWeather(WeatherHistory weatherHistory);

    @Update
    void updateWeather(WeatherHistory weatherHistory);

    @Delete
    void  deleteWeather(WeatherHistory weatherHistory);

    @Query("DELETE FROM WeatherHistory WHERE id = :id")
    void deleteWeatherById(long id);

    @Query("SELECT * FROM WeatherHistory")
    List<WeatherHistory> getAllWeather();

    @Query("SELECT * FROM WeatherHistory WHERE id = :id")
    WeatherHistory getWeatherById(long id);

    @Query("SELECT COUNT() FROM WeatherHistory")
    long getCountWeather();
}
