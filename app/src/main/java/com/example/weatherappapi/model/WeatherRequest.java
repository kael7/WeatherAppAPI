package com.example.weatherappapi.model;

public class WeatherRequest {
    private Coord coord;
    private Weather[] weather;
    private Main main;
    private Wind wind;
    private Clouds clouds;
    private String name;

    public Coord getCoord() {
        return coord;
    }

    public Weather[] getWeather() {
        return weather;
    }

    public Main getMain() {
        return main;
    }

    public Wind getWind() {
        return wind;
    }

    public Clouds getClouds() {
        return clouds;
    }

    public String getName() {
        return name;
    }

    public void setCoord(Coord coord) {
        this.coord = coord;
    }

    public void setWeather(Weather[] weather) {
        this.weather = weather;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public void setClouds(Clouds clouds) {
        this.clouds = clouds;
    }

    public void setName(String name) {
        this.name = name;
    }
}
