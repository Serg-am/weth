package com.company.weth;

import com.company.weth.entity.WeatherTableItem;
import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haulmont.cuba.core.global.DataManager;
import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service("weatherService")
public class WeatherService {
    private static final String API_KEY = "22ebcb3caabd888803c94ede901228ed";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private final static DateTimeFormatter OUTPUT_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("MMM-dd HH:mm", Locale.US);
    @Inject
    protected DataManager dataManager;

    public List<WeatherTableItem> getWeatherForecastForDays(String city, int numberOfDays) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(numberOfDays);
        return getWeatherForDate(city, startDate, endDate);
    }

    // Сохраняет данные о погоде в базу данных
    public void saveWeatherBD(String city, String dateTime, double temperature, double feelLike, double pressure,
                              int humidity, String description, double windSpeed, String windDirection){
        dataManager.commit(createWeatherTable(city, dateTime, temperature, feelLike, pressure, humidity, description, windSpeed, windDirection));
    }
    // Создает и наполняет сущность
    private WeatherTableItem createWeatherTable(String city, String dateTime, double temperature, double feelLike, double pressure, int humidity, String description, double windSpeed, String windDirection) {
        WeatherTableItem weatherTableItem = dataManager.create(WeatherTableItem.class);
        weatherTableItem.setCity(city);
        weatherTableItem.setDateTime(dateTime);
        weatherTableItem.setTemperature(temperature);
        weatherTableItem.setFeelsLike(feelLike);
        weatherTableItem.setPressure(pressure);
        weatherTableItem.setHumidity(humidity);
        weatherTableItem.setDescription(description);
        weatherTableItem.setWindSpeed(windSpeed);
        weatherTableItem.setWindDirection(windDirection);
        return weatherTableItem;
    }

    // Получает прогноз погоды для указанного города (в данный момент)
    public String getCurrentWeather(String city) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            LocalDateTime currentDateTime = LocalDateTime.now();
            String formattedDateTime = currentDateTime.format(OUTPUT_DATE_TIME_FORMAT);
            String jsonRawData = downloadJsonRawData("weather", city);
            return parseJsonNode(jsonRawData, city, objectMapper, formattedDateTime);
        } catch (Exception e) {
            Log.error("В данный момент погодный сервис не работает" + e.getMessage());
            return "В данный момент погодный сервис не работает, пожалуйста, попробуйте позже.";
        }
    }

    // Получает данные о погоде в заданном диапазоне дат для указанного города
    public List<WeatherTableItem> getWeatherForDate(String city, LocalDate startDate, LocalDate endDate) {
        try {
            String jsonRawData = downloadJsonRawData("forecast", city);
            return parseWeatherDataForDateRange(jsonRawData, city, startDate, endDate);
        } catch (Exception e) {
            Log.error(e.getMessage());
            return new ArrayList<>();
        }
    }

    // Загружает сырые данные JSON о погоде с опенВезер
    private String downloadJsonRawData(String endpoint, String city) throws Exception {
        String urlString = BASE_URL + endpoint + "?q=" + city + "&appid=" + API_KEY;
        URL urlObject = new URL(urlString);

        HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();
        }
    }

    // Извлекает данные из JSON и формирует сообщение о погоде
    private String parseJsonNode(String jsonData, String city, ObjectMapper objectMapper, String formattedDateTime) throws JsonProcessingException {
        JsonNode mainNode = objectMapper.readTree(jsonData).get("main");
        double temperature = mainNode.get("temp").asDouble() - 273.15;
        double feelsLike = mainNode.get("feels_like").asDouble() - 273.15;
        double pressure = mainNode.get("pressure").asDouble();
        int humidity = mainNode.get("humidity").asInt();

        JsonNode weatherArrNode = objectMapper.readTree(jsonData).get("weather");
        String description = weatherArrNode.get(0).get("description").asText();

        JsonNode windNode = objectMapper.readTree(jsonData).get("wind");
        double windSpeed = windNode.get("speed").asDouble();
        double windDegree = windNode.get("deg").asDouble();
        String windDirection = determineWindDirection(windDegree);

        saveWeatherBD(city,formattedDateTime, temperature, feelsLike, pressure, humidity, description, windSpeed, windDirection);

        return String.format("%s%nТемпература: %.1f °C%nТемпература 'как ощущается': %.1f °C%nДавление: %.1f hPa%nВлажность: %d%%%nОписание: %s%nСкорость ветра: %.1f м/с%nНаправление ветра: %s%n%n",
                formattedDateTime, temperature, feelsLike, pressure, humidity, description, windSpeed, windDirection);
    }

    // Определяет направление ветра по градусам
    private String determineWindDirection(double degree) {
        String[] directions = {"С", "ССЗ", "СЗ", "ЗСЗ", "З", "ЗЮЗ", "ЮЗ", "ЮЮЗ", "Ю", "ЮЮВ", "ЮВ", "ВЮВ", "В", "ВСВ", "СВ", "ССВ"};
        int index = (int) Math.round(((degree % 360) / 22.5));
        return directions[index % 16];
    }

    // Извлекает данные о погоде в заданном диапазоне дат из JSON. Тут еще нужно убрать дублирование кода
    public List<WeatherTableItem> parseWeatherDataForDateRange(String jsonData, String city, LocalDate startDate, LocalDate endDate) throws Exception {
        List<WeatherTableItem> weatherItems = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode arrNode = objectMapper.readTree(jsonData).get("list");

        for (JsonNode objNode : arrNode) {
            long timestamp = objNode.get("dt").asLong();
            LocalDateTime forecastDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());

            if (!forecastDateTime.toLocalDate().isBefore(startDate) && !forecastDateTime.toLocalDate().isAfter(endDate)) {
                String formattedDateTime = forecastDateTime.format(OUTPUT_DATE_TIME_FORMAT);
                JsonNode mainNode = objNode.get("main");

                double temperature = mainNode.get("temp").asDouble() - 273.15;
                double feelsLike = mainNode.get("feels_like").asDouble() - 273.15;
                double pressure = mainNode.get("pressure").asDouble();
                int humidity = mainNode.get("humidity").asInt();

                JsonNode weatherArrNode = objNode.get("weather");
                String description = weatherArrNode.get(0).get("description").asText();
                JsonNode windNode = objNode.get("wind");
                double windSpeed = windNode.get("speed").asDouble();
                double windDegree = windNode.get("deg").asDouble();
                String windDirection = determineWindDirection(windDegree);

                saveWeatherBD(city, formattedDateTime, temperature, feelsLike, pressure, humidity, description, windSpeed, windDirection);

                weatherItems.add(createWeatherTable(city, formattedDateTime, temperature, feelsLike, pressure, humidity, description, windSpeed, windDirection));
            }
        }
        return weatherItems;
    }
}