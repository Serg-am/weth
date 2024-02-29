package com.company.weth.web.screens;

import com.company.weth.WeatherService;
import com.company.weth.entity.WeatherTableItem;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.DateField;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;
import com.haulmont.cuba.gui.screen.Screen;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@UiController("weth_WeatherScreenDate")
@UiDescriptor("weather-screen-date.xml")
public class WeatherScreenDate extends Screen {

    @Inject
    private TextField<String> cityField;

    @Inject
    private DateField<Date> startDateField;

    @Inject
    private DateField<Date> endDateField;

    @Inject
    private Table<WeatherTableItem> weatherTable;

    @Inject
    private Button getWeatherBtn;

    @Inject
    private WeatherService weatherService;

    @Inject
    private CollectionContainer<WeatherTableItem> weatherTableItemsDc;


    @Inject
    private Label<String> currentWeatherLabel;

    public void onGetWeatherBtnClick() {
        String city = cityField.getValue();
        LocalDate startDate = startDateField.getValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = endDateField.getValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();


        if (city != null && startDate != null && endDate != null) {
            List<WeatherTableItem> weatherData = weatherService.getWeatherForDate(city, startDate, endDate);
            weatherTableItemsDc.setItems(weatherData);

            String currentWeather = weatherService.getCurrentWeather(city);
            currentWeatherLabel.setValue(currentWeather);
        }
    }
}
