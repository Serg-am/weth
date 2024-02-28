package com.company.weth.web.screens;

import com.company.weth.WeatherService;
import com.company.weth.entity.WeatherTableItem;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.Table;
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

    public void onGetWeatherBtnClick() {
        String city = "Moscow"; // Получите город из формы, если необходимо
        clearTable();
        String weatherForecast = weatherService.getWeatherForecast(city);
        addWeatherForecastToTable(weatherForecast);
    }

    private void clearTable() {
        weatherTableDc.getMutableItems().clear();
    }

    private void addWeatherForecastToTable(String forecastData) {
        System.out.println(forecastData);
        int index = forecastData.indexOf(":");
        // Если найден пробел, извлекаем первое слово
        String city = index != -1 ? forecastData.substring(0, index) : forecastData;
        String modifiedText = forecastData.replace(city + ":\n", "");
        String[] rows = modifiedText.split("\n\n");
        for (String row : rows) {
            String[] columns = parseWeatherInfo(row);
            WeatherTableItem item = createWeatherTableItem(city, columns);
            weatherTableDc.getMutableItems().add(item);
        }
    }
    private String[] parseWeatherInfo(String weatherInfo) {
        String[] lines = weatherInfo.split("\n");
        String[] data = new String[8]; // Массив для хранения разобранных данных
        int index = 0; // Индекс для заполнения массива данных

        // Обработка первой строки
        String[] firstLineParts = lines[0].split(" ");
        data[index++] = firstLineParts[0] + " " + firstLineParts[1]; // Добавляем дату и время

        // Обработка остальных строк
        for (int i = 1; i < lines.length; i++) {
            // Разбиваем строку по двоеточию и извлекаем вторую часть (значение)
            String[] parts = lines[i].split(":");
            // Удаляем лишние пробелы из начала и конца строки, а также символ градуса, если присутствует
            String value = parts[1].trim().replaceAll("°C", "").replaceAll("hPa", "").replaceAll("%", "").replaceAll("м/с", "");
            // Добавляем значение в массив данных
            data[index++] = value;
        }

        return data;
    }

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