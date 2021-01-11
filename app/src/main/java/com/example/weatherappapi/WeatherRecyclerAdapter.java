package com.example.weatherappapi;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherappapi.model.Weather;
import com.example.weatherappapi.model.WeatherHistory;

import java.util.List;

public class WeatherRecyclerAdapter extends RecyclerView.Adapter<WeatherRecyclerAdapter.ViewHolder>{
    private List<String> data;
    private Activity activity;
    private int menuPosition;
    private WeatherSource dataSource;

    public WeatherRecyclerAdapter(WeatherSource dataSource, Activity activity){
        this.dataSource = dataSource;
        this.activity = activity;
    }

    //region Переопределение методов адаптера
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        List<WeatherHistory> weatherHistories = dataSource.getWeatherHistories();
        WeatherHistory weatherHistory = weatherHistories.get(position);
        holder.textElement.setText(weatherHistory.city);

        // Заполнение элементов холдера
//        TextView textElement = holder.getTextElement();
//        textElement.setText(data.get(position));

        // Тут определяем, какой пункт меню был нажат
        holder.cardView.setOnLongClickListener(view -> {
            menuPosition = position;
            return false;
        });

        // Регистрируем контекстное меню
        if (activity != null){
            activity.registerForContextMenu(holder.cardView);
        }

    }

    @Override
    public int getItemCount() {
        return (int) dataSource.getCountWeathers();
    }

    public long getMenuPosition() {
        return menuPosition;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        View cardView;
        private TextView textElement;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView;
            textElement = cardView.findViewById(R.id.textElement);
        }

        public TextView getTextElement() {
            return textElement;
        }
    }
}
