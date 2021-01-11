package com.example.weatherappapi;

import com.example.weatherappapi.dao.WeatherDao;
import com.example.weatherappapi.model.WeatherHistory;

import java.util.Calendar;
import java.util.List;

// Вспомогательный класс, развязывающий зависимость между Room и RecyclerView
public class WeatherSource {
    private final WeatherDao weatherDao;

    // Буфер с данными: сюда будем подкачивать данные из БД
    private List<WeatherHistory> weatherHistories;
    private WeatherHistory weatherHistory;

    public WeatherSource(WeatherDao weatherDao) {
        this.weatherDao = weatherDao;
    }

    // Получить всех студентов
    public List<WeatherHistory> getWeatherHistories() {
        // Если объекты еще не загружены, загружаем их.
        // Это сделано для того, чтобы не делать запросы к БД каждый раз
        if (weatherHistories == null) {
            LoadWeatherHistory();
        }
        return weatherHistories;
    }

    private void LoadWeatherHistory() {
        weatherHistories = weatherDao.getAllWeather();
    }

    // Получаем количество записей
    public long getCountWeathers(){
        return weatherDao.getCountWeather();
    }

    // Добавляем студента
    public void addWeather(WeatherHistory weatherHistory){
        Calendar cal = Calendar.getInstance();
        weatherHistory.dateWeather = cal.getTime();
        weatherDao.insertWeather(weatherHistory);
        LoadWeatherHistory();
    }

    // Заменяем студента
    public void updateWeather(WeatherHistory weatherHistory){
        weatherDao.updateWeather(weatherHistory);
        LoadWeatherHistory();
    }

    // Удаляем студента из базы
    public void removeWeather(long id){
        weatherDao.deleteWeatherById(id);
        LoadWeatherHistory();
    }

    public WeatherHistory getWeatherHistory(Long id) {
        weatherHistory = weatherDao.getWeatherById(id);
        LoadWeatherHistory();
        return weatherHistory;
    }


}
