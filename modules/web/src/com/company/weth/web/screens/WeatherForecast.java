package com.company.weth.web.screens;

import com.company.weth.WeatherService;
import com.company.weth.entity.WeatherTableItem;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;

import javax.inject.Inject;
import java.util.List;

@UiController("weth_WeatherForecast")
@UiDescriptor("weather-forecast.xml")
public class WeatherForecast extends Screen {
    @Inject
    private Table weatherTable;
    @Inject
    private Button getWeatherBtn;
    @Inject
    private CollectionContainer<WeatherTableItem> weatherTableDc;
    @Inject
    private WeatherService weatherService;
    @Inject
    private TextField<String> cityField;
    @Inject
    private Label<String> currentWeatherLabel;

    public void onGetWeatherBtnClick() {
        String city = cityField.getValue();
        clearTable();
        int numberOfDays = 5; // Устанавливаем количество дней для прогноза
        List<WeatherTableItem> weatherData = weatherService.getWeatherForecastForDays(city, numberOfDays);
        addWeatherForecastToTable(weatherData);

        String currentWeather = weatherService.getCurrentWeather(city);
        currentWeatherLabel.setValue(currentWeather);
    }

    private void clearTable() {
        weatherTableDc.getMutableItems().clear();
    }

    // Метод для добавления прогноза погоды в таблицу
    private void addWeatherForecastToTable(List<WeatherTableItem> forecastData) {
        for (WeatherTableItem item : forecastData) {
            weatherTableDc.getMutableItems().add(item);
        }
    }
}