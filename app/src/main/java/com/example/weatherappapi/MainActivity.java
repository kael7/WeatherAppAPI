package com.example.weatherappapi;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.EditText;
import android.widget.Toast;

import com.example.weatherappapi.Fragments.HistoryFragment;
import com.example.weatherappapi.Fragments.HomeFragment;
import com.example.weatherappapi.Fragments.SettingsFragment;
import com.example.weatherappapi.Old.BoundService;
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

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private HomeFragment homeFragment;
    private HistoryFragment historyFragment;
    private SettingsFragment settingsFragment;

    private AppBarConfiguration mAppBarConfiguration;
    private ListAdapter adapter;

    private EditText city;
    private EditText temperature;
    private EditText pressure;
    private EditText humidity;
    private EditText windSpeed;

    private boolean binded = false;
    private WeatherService weatherService;

    ServiceConnection weatherServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WeatherService.LocalWeatherBinder binder = (WeatherService.LocalWeatherBinder) service;
            weatherService = binder.getService();
            binded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binded = false;
        }
    };

    // When Activity starting.
    @Override
    protected void onStart() {
        super.onStart();

        // Create Intent object for WeatherService.
        Intent intent = new Intent(this, WeatherService.class);

        // Call bindService(..) method to bind service with UI.
        this.bindService(intent, weatherServiceConnection, Context.BIND_AUTO_CREATE);
    }

    // Activity stop
    @Override
    protected void onStop() {
        super.onStop();
        if (binded) {
            // Unbind Service
            this.unbindService(weatherServiceConnection);
            binded = false;
        }
    }

    // When user click on 'Search' button.
    public void showWeather(String location)  {
        String[] weather= this.weatherService.getWeatherToday(location);
        this.city.setText(weather[0]);
        this.temperature.setText(weather[1]);
        this.pressure.setText(weather[2]);
        this.humidity.setText(weather[3]);
        this.windSpeed.setText(weather[4]);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // создадим фрагменты
        homeFragment = new HomeFragment();
        historyFragment = new HistoryFragment();
        settingsFragment = new SettingsFragment();

        Toolbar toolbar = initToolbar();
        initFab();
        initDrawer(toolbar);
//        initList();
        init();

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_container, homeFragment);
        ft.commit();
    }

    public void init() {
        city = findViewById(R.id.textCity);
        temperature = findViewById(R.id.textTemprature);
        pressure = findViewById(R.id.textPressure);
        humidity = findViewById(R.id.textHumidity);
        windSpeed = findViewById(R.id.textWindspeed);
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
                                adapter.addItem(String.format("New element %d", adapter.getItemCount()));
                                return true;
                            case R.id.clear_popup:
                                adapter.clearItems();
                                return true;
                            case R.id.custom_id:
                                Snackbar.make(view, "Menu item added - clicked", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                                return true;
                        }
                        return true;
                    }
                });
                menu.show();
            }
        });
    }

    private void initList() {
        RecyclerView recyclerView = findViewById(R.id.recycler_list);

        // Эта установка повышает производительность системы
        recyclerView.setHasFixedSize(true);

        // Будем работать со встроенным менеджером
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Устанавливаем адаптер
        adapter = new ListAdapter(initData(), this);
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
                adapter.addItem(String.format("New element %d", adapter.getItemCount()));
                return true;
            case R.id.update_context:
                adapter.updateItem(String.format("Updated element %d", adapter.getMenuPosition()), adapter.getMenuPosition());
                return true;
            case R.id.remove_context:
                adapter.removeItem(adapter.getMenuPosition());
                return true;
            case R.id.clear_context:
                adapter.clearItems();
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
            adapter.addItem("New element");
            return true;
        }

        if (id == R.id.action_clear) {
            adapter.clearItems();
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            showWeather(query);
                        } catch (NullPointerException e) {
                            System.out.println("Error");
                        }
                    }
                });
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
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                ft.replace(R.id.fragment_container, homeFragment);
                ft.commit();
                break;
            case R.id.nav_history:
                FragmentManager fm2 = getFragmentManager();
                FragmentTransaction ft2 = fm2.beginTransaction();

                ft2.replace(R.id.fragment_container, historyFragment);
                ft2.commit();
                break;
            case R.id.nav_settings:
                FragmentManager fm3 = getFragmentManager();
                FragmentTransaction ft3 = fm3.beginTransaction();

                ft3.replace(R.id.fragment_container, settingsFragment);
                ft3.commit();
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

    private void failConnection(){
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