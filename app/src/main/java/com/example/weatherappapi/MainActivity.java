package com.example.weatherappapi;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weatherappapi.model.Weather;
import com.example.weatherappapi.model.WeatherRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WEATHER";

    // Регулярные выражения позволяют проверить на соответствие шаблону
    // Это имя. Первая буква большая латинская, остальные маленькие латинские
    Pattern checkLogin = Pattern.compile("^[A-Z][a-z]{2,}$");

    private TextInputEditText cityInput;
    private EditText city;
    private EditText temperature;
    private EditText pressure;
    private EditText humidity;
    private EditText windSpeed;
//    private ImageView weatherIcon;
    private FloatingActionButton floatingActionButton;

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

    private void init() {
        cityInput = findViewById(R.id.textInputCity);
        city = findViewById(R.id.textCity);
        temperature = findViewById(R.id.textTemprature);
        pressure = findViewById(R.id.textPressure);
        humidity = findViewById(R.id.textHumidity);
        windSpeed = findViewById(R.id.textWindspeed);
        floatingActionButton = findViewById(R.id.floatingActionButton);
        Button search = findViewById(R.id.search);
        search.setOnClickListener(clickListener);
    }

    final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                String WEATHER_URL_PART_2 = cityInput.getText().toString();

                final URL uri = new URL(WEATHER_URL_PART_1 + WEATHER_URL_PART_2 + WEATHER_URL_PART_3 + BuildConfig.WEATHER_API_KEY);

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

                            // Возвращаемся к основному потоку
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    displayWeather(weatherRequest);
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
            validate(cityInput, checkLogin, "Это не город!");
        }

        private String getLines(BufferedReader in) {
            return in.lines().collect(Collectors.joining("\n"));
        }

        private void displayWeather(WeatherRequest weatherRequest) {
            city.setText(weatherRequest.getName());
            temperature.setText(String.format("%f2", weatherRequest.getMain().getTemp()));
            pressure.setText(String.format("%d", weatherRequest.getMain().getPressure()));
            humidity.setText(String.format("%d", weatherRequest.getMain().getHumidity()));
            windSpeed.setText(String.format("%d", weatherRequest.getWind().getSpeed()));
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

    };
}