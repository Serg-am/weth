package com.company.weth.web.screens;

import com.company.weth.WeatherService;
import com.company.weth.entity.WeatherTableItem;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;

import javax.inject.Inject;

@UiController("weth_WeatherForecast")
@UiDescriptor("weather-forecast.xml")
public class WeatherForecast extends Screen {
    @Inject
    protected DataManager dataManager;
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
        String weatherForecast = weatherService.getWeatherForecast(city);
        addWeatherForecastToTable(weatherForecast);

        String currentWeather = weatherService.getCurrentWeather(city);
        currentWeatherLabel.setValue(currentWeather);
    }

    private void clearTable() {
        weatherTableDc.getMutableItems().clear();
    }

    // Метод для добавления прогноза погоды в таблицу
    private void addWeatherForecastToTable(String forecastData) {
        int index = forecastData.indexOf(":");
        String city = index != -1 ? forecastData.substring(0, index) : forecastData;
        String modifiedText = forecastData.replace(city + ":\n", "");
        String[] rows = modifiedText.split("\n\n");
        for (String row : rows) {
            String[] columns = parseWeatherInfo(row);
            WeatherTableItem item = createWeatherTableItem(city, columns);
            weatherTableDc.getMutableItems().add(item);
        }
    }
    // Метод для разбора строки с информацией о погоде
    private String[] parseWeatherInfo(String weatherInfo) {
        String[] lines = weatherInfo.split("\n");
        String[] data = new String[8];
        int index = 0;

        String[] firstLineParts = lines[0].split(" ");
        data[index++] = firstLineParts[0] + " " + firstLineParts[1];

        for (int i = 1; i < lines.length; i++) {
            String[] parts = lines[i].split(":");
            String value = parts[1].trim().replaceAll("°C", "").replaceAll("hPa", "").replaceAll("%", "").replaceAll("м/с", "");
            data[index++] = value;
        }

        return data;
    }

    // Метод для создания объекта WeatherTableItem на основе данных о погоде. В WeatherScreenDate последние методы ушли в сервис
    private WeatherTableItem createWeatherTableItem(String city, String[] data) {
        WeatherTableItem item = dataManager.create(WeatherTableItem.class);
        item.setCity(city);
        item.setDateTime(data[0]);
        item.setTemperature(Double.parseDouble(data[1].replace(",", ".")));
        item.setFeelsLike(Double.parseDouble(data[2].replace(",", ".")));
        item.setPressure(Double.parseDouble(data[3].replace(",", ".")));
        item.setHumidity(Integer.valueOf(data[4]));
        item.setDescription(data[5]);
        item.setWindSpeed(Double.parseDouble(data[6].replace(",", ".")));
        item.setWindDirection(data[7]);
        return item;
    }
}