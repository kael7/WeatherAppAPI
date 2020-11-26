package com.example.weatherappapi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weatherappapi.model.Main;
import com.example.weatherappapi.model.Weather;
import com.example.weatherappapi.model.WeatherRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    public static final String CITY_KEY = "CITY";
    private boolean isExistWeather;  // Можно ли расположить рядом фрагмент с погодой
    private static final String TAG = "WEATHER";
    private Pattern checkLogin = Pattern.compile("^[A-Z][a-z]{2,}$"); // Регулярные выражения позволяют проверить на соответствие шаблону
    private TextInputEditText cityInput;
    private FloatingActionButton floatingActionButton;
    public EditText city;
    public EditText temperature;
    public EditText pressure;
    public EditText humidity;
    public EditText windSpeed;
    private static final String WEATHER_URL_PART_1 = "https://api.openweathermap.org/data/2.5/weather?q=";
    private static final String WEATHER_URL_PART_3 = ",&appid=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Вы нажали на Snackbar", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onRestoreInstanceState(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
    }

    public void init() {
        city = findViewById(R.id.textCity);
        temperature = findViewById(R.id.textTemprature);
        pressure = findViewById(R.id.textPressure);
        humidity = findViewById(R.id.textHumidity);
        windSpeed = findViewById(R.id.textWindspeed);
        cityInput = findViewById(R.id.textInputCity);
        floatingActionButton = findViewById(R.id.floatingActionButton);
        Button search = findViewById(R.id.search);
        isExistWeather = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;  // Определение, можно ли будет расположить рядом герб в другом фрагменте
    }

    // Обработка нажатия кнопки
    public void clickOnSearchButton(View view) {
        validate(cityInput, checkLogin, "Это не город!");
        String cityName = cityInput.getText().toString();
        Toast.makeText(getApplicationContext(), cityName, Toast.LENGTH_SHORT).show();
        getDetails(cityName);
    }

    public void getDetails(String city) {
        try {
            String WEATHER_URL_PART_2 = city;
            final URL uri = new URL(WEATHER_URL_PART_1 + WEATHER_URL_PART_2 + WEATHER_URL_PART_3 + BuildConfig.WEATHER_API_KEY);
            Snackbar.make(cityInput, "Вы ищите погоду для города: " + WEATHER_URL_PART_2, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            new Thread(new Runnable() {
                public void run() {
                    HttpsURLConnection urlConnection = null;
                    try {
                        urlConnection = (HttpsURLConnection) uri.openConnection();
                        urlConnection.setRequestMethod("GET"); // установка метода получения данных -GET
                        urlConnection.setReadTimeout(10000); // установка таймаута - 10 000 миллисекунд
                        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream())); // читаем  данные в поток
                        String result = getLines(in);

                        // преобразование данных запроса в модель
                        Gson gson = new Gson();
                        final WeatherRequest weatherRequest = gson.fromJson(result, WeatherRequest.class);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isExistWeather) {
                                    displayWeather(weatherRequest);
                                } else {
                                    // Если нельзя вывести погоду рядом, откроем вторую activity
                                    Intent intent = new Intent();
                                    intent.setClass(MainActivity.this, CityActivity.class);
                                    intent.putExtra("city", weatherRequest.getName());
                                    intent.putExtra("temperature", weatherRequest.getMain().getTemp());
                                    intent.putExtra("pressure", weatherRequest.getMain().getPressure());
                                    intent.putExtra("humidity", weatherRequest.getMain().getHumidity());
                                    intent.putExtra("windSpeed", weatherRequest.getWind().getSpeed());
                                    startActivity(intent);
                                }
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Fail connection", e);
                        e.printStackTrace();
                    } finally {
                        if (null != urlConnection) {
                            urlConnection.disconnect();
                        }
                    }
                }
            }).start();
        } catch (MalformedURLException e) {
            Log.e(TAG, "Fail URI", e);
            e.printStackTrace();
        }
    }

    private void displayWeather(WeatherRequest weatherRequest) {
        city.setText(weatherRequest.getName());
        temperature.setText(String.format("%f2", weatherRequest.getMain().getTemp()));
        pressure.setText(String.format("%d", weatherRequest.getMain().getPressure()));
        humidity.setText(String.format("%d", weatherRequest.getMain().getHumidity()));
        windSpeed.setText(String.format("%d", weatherRequest.getWind().getSpeed()));
    }

    private String getLines(BufferedReader in) {
        return in.lines().collect(Collectors.joining("\n"));
    }

    // Валидация
    private void validate(TextView tv, Pattern check, String message) {
        String value = tv.getText().toString();
        if (check.matcher(value).matches()) {    // Проверим на основе регулярных выражений
            hideError(tv);
        } else {
            showError(tv, message);
        }
    }

    // Показать ошибку
    private void showError(TextView view, String message) {
        view.setError(message);
    }

    // спрятать ошибку
    private void hideError(TextView view) {
        view.setError(null);
    }

}