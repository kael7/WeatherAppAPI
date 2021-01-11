package com.example.weatherappapi;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.weatherappapi.dao.WeatherDao;
import com.example.weatherappapi.fragments.HistoryFragment;
import com.example.weatherappapi.fragments.HomeFragment;
import com.example.weatherappapi.fragments.SettingsFragment;
import com.example.weatherappapi.interfaces.OpenWeather;
import com.example.weatherappapi.model.WeatherHistory;
import com.example.weatherappapi.model.WeatherRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.weatherappapi.Constants.ABS_ZERO;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private OpenWeather openWeather;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private HomeFragment homeFragment;
    private HistoryFragment historyFragment;
    private SettingsFragment settingsFragment;
    private AppBarConfiguration mAppBarConfiguration;
    private WeatherRecyclerAdapter adapter;
    private WeatherSource weatherSource;
    private BatteryLevelReceiver batteryLevelReceiver;
    private NetworkConnectionReceiver networkConnectionReceiver;

    private EditText city;
    private EditText temperature;
    private EditText pressure;
    private EditText humidity;
    private EditText windSpeed;
    private SharedPreferences sharedPref;
    private EditText editCityKey;
    private EditText editApiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Программная регистрация ресивера
        registerReceiver(batteryLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_LOW));
        registerReceiver(networkConnectionReceiver, new IntentFilter(Intent.ACTION_MANAGE_NETWORK_USAGE));

        // создадим фрагменты
        homeFragment = new HomeFragment();
        historyFragment = new HistoryFragment();
        settingsFragment = new SettingsFragment();
        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, homeFragment);
        fragmentTransaction.commit();

        Toolbar toolbar = initToolbar();
        initFab();
        initDrawer(toolbar);
//        initRecyclerView();
        initGui();
        initPreferences();
        initRetorfit();
        initEvents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryLevelReceiver);
        unregisterReceiver(networkConnectionReceiver);
    }


    private void initRetorfit() {
        Retrofit retrofit;
        retrofit = new Retrofit.Builder()
                // Базовая часть адреса
                .baseUrl("https://api.openweathermap.org/")
                // Конвертер, необходимый для преобразования JSON в объекты
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        // Создаём объект, при помощи которого будем выполнять запросы
        openWeather = retrofit.create(OpenWeather.class);
    }

    private void requestRetrofit(String city, String keyApi) {
        openWeather.loadWeather(city, keyApi).enqueue(new Callback<WeatherRequest>() {
            @Override
            public void onResponse(Call<WeatherRequest> call, Response<WeatherRequest> response) {
                if (response.body() != null) {
                    float result = response.body().getMain().getTemp() + ABS_ZERO;
                    temperature.setText(Float.toString(result));
                    pressure.setText(response.body().getMain().getPressure());
                    humidity.setText(response.body().getMain().getHumidity());
                    windSpeed.setText((int) response.body().getWind().getSpeed());
                }
            }

            @Override
            public void onFailure(Call<WeatherRequest> call, Throwable t) {
                temperature.setText("Error");
            }
        });
    }

    // Создаём обработку клика кнопки
    private void initEvents() {

        Button button = findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePreferences();            // Сохраняем настройки
                requestRetrofit(editCityKey.getText().toString(), editApiKey.getText().toString());
            }
        });


    }

    private void initPreferences() {
        sharedPref = getPreferences(MODE_PRIVATE);
        loadPreferences();                   // Загружаем настройки
    }

    // Сохраняем настройки
    private void savePreferences() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("apiKey", editApiKey.getText().toString());
        editor.putString("cityKey", editCityKey.getText().toString());
        editor.commit();
    }

    // Загружаем настройки
    private void loadPreferences() {
        String cityKey = null;
        String apiKey = BuildConfig.WEATHER_API_KEY;

        String loadedApiKey = sharedPref.getString("apiKey", apiKey);
        String loadedCityKey = sharedPref.getString("cityKey", cityKey);
        editApiKey.setText(loadedApiKey);
        editCityKey.setText(loadedCityKey);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        savePreferences();
    }

    // Инициализируем пользовательские элементы
    private void initGui() {
        city = findViewById(R.id.textCity);
        temperature = findViewById(R.id.textTemprature);
        pressure = findViewById(R.id.textPressure);
        humidity = findViewById(R.id.textHumidity);
        windSpeed = findViewById(R.id.textWindspeed);

        editApiKey = findViewById(R.id.editApiKey);
        editCityKey = findViewById(R.id.editCity);
    }

    private Toolbar initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        return toolbar;
    }

    private void initDrawer(Toolbar toolbar) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu menu = new PopupMenu(MainActivity.this, view);
                getMenuInflater().inflate(R.menu.popup_menu, menu.getMenu());
                menu.getMenu().findItem(R.id.update_popup).setVisible(false);
                menu.getMenu().add(0, R.id.custom_id, 12, "Menu item added");
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        switch (id) {
                            case R.id.add_popup:
//                                adapter.addItem(String.format("New element %d", adapter.getItemCount()));
                                return true;
                            case R.id.clear_popup:
//                                adapter.clearItems();
                                return true;
                            case R.id.custom_id:
//                                Snackbar.make(view, "Menu item added - clicked", Snackbar.LENGTH_LONG)
//                                        .setAction("Action", null).show();
                                return true;
                        }
                        return true;
                    }
                });
                menu.show();
            }
        });
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_list);
        // Эта установка повышает производительность системы
        recyclerView.setHasFixedSize(true);
        // Будем работать со встроенным менеджером
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        WeatherDao weatherDao = App
                .getInstance()
                .getWeatherDao();
        weatherSource = new WeatherSource(weatherDao);

        // Устанавливаем адаптер
        adapter = new WeatherRecyclerAdapter(weatherSource, this);
        recyclerView.setAdapter(adapter);
    }

    private List<String> initData() {
        String[] values = new String[]{"Almaty", "Shymkent", "Karagandy",
                "Taraz", "Nur-Sultan", "Pavlodar", "Oskemen", "Semeı",
                "Aktobe", "Aktau"};
        List<String> list = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            list.add(values[i]);
        }
        return list;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();
        int id = item.getItemId();
        switch (id) {
            case R.id.add_context:
//                adapter.addItem(String.format("New element %d", adapter.getItemCount()));
                return true;
            case R.id.update_context:
//                adapter.updateItem(String.format("Updated element %d", adapter.getMenuPosition()), adapter.getMenuPosition());
                return true;
            case R.id.remove_context:
//                adapter.removeItem(adapter.getMenuPosition());
                WeatherHistory weatherForRemove = weatherSource
                        .getWeatherHistories()
                        .get((int) adapter.getMenuPosition());
                weatherSource.removeWeather(weatherForRemove.id);
                adapter.notifyItemRemoved((int) adapter.getMenuPosition());
                return true;
            case R.id.clear_context:
//                adapter.clearItems();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Обработка выбора пункта меню приложения (Activity)
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_add) {
//            adapter.addItem("New element");
            return true;
        }
        if (id == R.id.action_clear) {
//            adapter.clearItems();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem search = menu.findItem(R.id.action_search);
        // Строка поиска
        final SearchView searchText = (SearchView) search.getActionView();
        searchText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // Реагирует на конец ввода поиска
            @Override
            public boolean onQueryTextSubmit(String query) {
                Snackbar.make(searchText, query, Snackbar.LENGTH_LONG).show();
//                adapter.addItem(query);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            showWeather(query);
//                        } catch (NullPointerException e) {
//                            System.out.println("Error");
//                        }
//                    }
//                });
                return true;
            }

            // Реагирует на нажатие каждой клавиши
            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
                fragmentManager = getFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, homeFragment);
                fragmentTransaction.commit();
                break;
            case R.id.nav_history:
                fragmentManager = getFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, historyFragment);
                fragmentTransaction.commit();
                break;
            case R.id.nav_settings:
                fragmentManager = getFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, settingsFragment);
                fragmentTransaction.commit();
                break;
            case R.id.nav_share:
                //TODO:
                break;
            case R.id.nav_send:
                //TODO:
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void failConnection() {
        // Создаём билдер и передаём контекст приложения
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        // В билдере указываем заголовок окна (можно указывать как ресурс,
        // так и строку)
        builder.setTitle(R.string.exclamation)
                // Указываем сообщение в окне (также есть вариант со
                // строковым параметром)
                .setMessage(R.string.press_button)
                // Можно указать и пиктограмму
                .setIcon(R.mipmap.ic_launcher_round)
                // Из этого окна нельзя выйти кнопкой Back
                .setCancelable(false)
                // Устанавливаем кнопку (название кнопки также можно
                // задавать строкой)
                .setPositiveButton(R.string.button,
                        // Ставим слушатель, нажатие будем обрабатывать
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(MainActivity.this, "Кнопка нажата", Toast.LENGTH_SHORT).show();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
        Toast.makeText(MainActivity.this, "Диалог открыт", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // На Android OS версии 2.6 и выше нужно создать канал нотификации.
    // На старых версиях канал создавать не надо
    private void initNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel("2", "name", importance);
            notificationManager.createNotificationChannel(mChannel);
        }
    }


}