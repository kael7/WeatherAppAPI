package com.example.weatherappapi.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

// @Entity - это признак табличного объекта, то есть объект будет сохраняться
// в базе данных в виде строки
// indices указывает на индексы в таблице
@Entity(indices = {@Index(value = {"city"})})
public class WeatherHistory {
    public WeatherHistory(String city) {
        this.city = city;
    }

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "city")
    public String city;

    public Date dateWeather;

    @Override
    public String toString() {
        return city + "  " +
                dateWeather;
    }
}
