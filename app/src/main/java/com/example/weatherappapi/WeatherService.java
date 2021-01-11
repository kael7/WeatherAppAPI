package com.example.weatherappapi;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.weatherappapi.model.WeatherRequest;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import static com.example.weatherappapi.Constants.TAG;

public class WeatherService extends Service {
    private static String LOG_TAG = "WeatherService";
    private volatile WeatherRequest weatherRequest;
    private String[] weatherData;

    private final IBinder binder = new LocalWeatherBinder();

    public class LocalWeatherBinder extends Binder {

        public WeatherService getService() {
            return WeatherService.this;
        }
    }

    public WeatherService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOG_TAG, "onBind");
        return this.binder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(LOG_TAG, "onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(LOG_TAG, "onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
    }

    // Returns the weather information corresponding to the location of the current date.
    public String[] getWeatherToday(String location) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("https://api.openweathermap.org/data/2.5/weather?q=");
            sb.append(location);
            sb.append(",&appid=");
            sb.append(BuildConfig.WEATHER_API_KEY);

            final URL uri = new URL(sb.toString());
            new Thread(new Runnable() {
                @Override
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
                        weatherRequest = gson.fromJson(result, WeatherRequest.class);

                        weatherData = new String[]{weatherRequest.getName(),
                                String.valueOf(weatherRequest.getMain().getTemp()),
                                String.valueOf(weatherRequest.getMain().getPressure()),
                                String.valueOf(weatherRequest.getMain().getHumidity()),
                                String.valueOf(weatherRequest.getWind().getSpeed())};

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

        return weatherData;
    }

    private String getLines(BufferedReader in) {
        return in.lines().collect(Collectors.joining("\n"));
    }


}
