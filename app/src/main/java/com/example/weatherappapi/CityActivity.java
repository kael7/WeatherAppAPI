package com.example.weatherappapi;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class CityActivity extends MainActivity{

    private String city_detail;
    private String temperature_detail;
    private String pressure_detail;
    private String humidity_detail;
    private String windSpeed_detail;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);
        init();
        try {
            Bundle arguments = getIntent().getExtras();
            if(arguments!=null){
                city_detail = arguments.get("city").toString();
                temperature_detail = arguments.get("temperature").toString();
                pressure_detail = arguments.get("pressure").toString();
                humidity_detail = arguments.get("humidity").toString();
                windSpeed_detail = arguments.get("windSpeed").toString();
                showDetails();
            }
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDetails() {
        city.setText(city_detail);
        temperature.setText(temperature_detail);
        pressure.setText(pressure_detail);
        humidity.setText(humidity_detail);
        windSpeed.setText(windSpeed_detail);
    }
}
