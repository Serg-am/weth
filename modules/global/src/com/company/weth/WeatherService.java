package com.company.weth;

import com.company.weth.entity.WeatherTableItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haulmont.cuba.core.global.DataManager;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service("weatherService")
public class WeatherService {
    private static final String API_KEY = "22ebcb3caabd888803c94ede901228ed";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/forecast";

    private final static DateTimeFormatter INPUT_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final static DateTimeFormatter OUTPUT_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("MMM-dd HH:mm", Locale.US);
    @Inject
    protected DataManager dataManager;


    protected WeatherTableItemService weatherTableItemService;


    public void saveWeatherBD(String city, String dateTime, double temperature, double feelLike, double pressure,
                              int humidity, String description, double windSpeed, String windDirection){
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

        dataManager.commit(weatherTableItem);

        //weatherTableItemService.saveWeatherItem(weatherTableItem);
    }
    public String getWeatherForecast(String city) {
        String result;
        try {
            String jsonRawData = downloadJsonRawData(city);
            List<String> linesOfForecast = convertRawDataToList(jsonRawData);
            result = String.format("%s:%s%s", city, System.lineSeparator(), parseForecastDataFromList(linesOfForecast, city));
        } catch (IllegalArgumentException e) {
            return String.format("Не могу найти город \"%s\".", city);
        } catch (Exception e) {
            e.printStackTrace();
            return "В данный момент погодный сервис не работает, пожалуйста, попробуйте позже.";
        }
        return result;
    }

    private String downloadJsonRawData(String city) throws Exception {
        String urlString = BASE_URL + "?q=" + city + "&appid=" + API_KEY;
        URL urlObject = new URL(urlString);

        HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        int responseCode = connection.getResponseCode();
        if (responseCode == 404) {
            throw new IllegalArgumentException();
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    private List<String> convertRawDataToList(String data) throws Exception {
        List<String> weatherList = new ArrayList<>();

        JsonNode arrNode = new ObjectMapper().readTree(data).get("list");
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                String forecastTime = objNode.get("dt_txt").toString();
                if (forecastTime.contains("12:00")) { // Прогноз на 12:00 каждого дня
                    weatherList.add(objNode.toString());
                }
            }
        }
        return weatherList;
    }

    private String parseForecastDataFromList(List<String> weatherList, String city) throws Exception {
        StringBuilder sb = new StringBuilder();
        ObjectMapper objectMapper = new ObjectMapper();

        for (String line : weatherList) {
            String dateTime = objectMapper.readTree(line).get("dt_txt").asText();
            LocalDateTime forecastDateTime = LocalDateTime.parse(dateTime, INPUT_DATE_TIME_FORMAT);
            String formattedDateTime = forecastDateTime.format(OUTPUT_DATE_TIME_FORMAT);

            JsonNode mainNode = objectMapper.readTree(line).get("main");
            double temperature = mainNode.get("temp").asDouble() - 273.15; // Convert from Kelvin to Celsius
            double feelsLike = mainNode.get("feels_like").asDouble() - 273.15; // Convert from Kelvin to Celsius
            double pressure = mainNode.get("pressure").asDouble();
            int humidity = mainNode.get("humidity").asInt();

            JsonNode weatherArrNode = objectMapper.readTree(line).get("weather");
            String description = weatherArrNode.get(0).get("description").asText();

            JsonNode windNode = objectMapper.readTree(line).get("wind");
            double windSpeed = windNode.get("speed").asDouble();
            double windDegree = windNode.get("deg").asDouble();
            String windDirection = determineWindDirection(windDegree);

            sb.append(String.format("%s%nТемпература: %.1f °C%nТемпература 'как ощущается': %.1f °C%nДавление: %.1f hPa%nВлажность: %d%%%nОписание: %s%nСкорость ветра: %.1f м/с%nНаправление ветра: %s%n%n",
                    formattedDateTime, temperature, feelsLike, pressure, humidity, description, windSpeed, windDirection));
            saveWeatherBD(city,formattedDateTime, temperature, feelsLike, pressure, humidity, description, windSpeed, windDirection);
        }
        return sb.toString();
    }

    private String determineWindDirection(double degree) {
        String[] directions = {"С", "ССЗ", "СЗ", "ЗСЗ", "З", "ЗЮЗ", "ЮЗ", "ЮЮЗ", "Ю", "ЮЮВ", "ЮВ", "ВЮВ", "В", "ВСВ", "СВ", "ССВ"};
        int index = (int) Math.round(((degree % 360) / 22.5));
        return directions[index % 16];
    }
}

