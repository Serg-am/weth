package com.company.weth;

import com.company.weth.entity.WeatherTableItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haulmont.cuba.core.global.DataManager;
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

@Service("weatherService")
public class WeatherService {
    private static final String API_KEY = "22ebcb3caabd888803c94ede901228ed";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";

    private final static DateTimeFormatter INPUT_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final static DateTimeFormatter OUTPUT_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("MMM-dd HH:mm", Locale.US);
    @Inject
    protected DataManager dataManager;

    public void saveWeatherBD(String city, String dateTime, double temperature, double feelLike, double pressure,
                              int humidity, String description, double windSpeed, String windDirection){
        dataManager.commit(createWeatherTable(city, dateTime, temperature, feelLike, pressure, humidity, description, windSpeed, windDirection));
    }

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

    public String getWeatherForecast(String city) {
        try {
            String jsonRawData = downloadJsonRawData("forecast", city);
            List<String> linesOfForecast = convertRawDataToList(jsonRawData);
            return String.format("%s:%s%s", city, System.lineSeparator(), parseForecastDataFromList(linesOfForecast, city));
        } catch (Exception e) {
            e.printStackTrace();
            return "В данный момент погодный сервис не работает, пожалуйста, попробуйте позже.";
        }
    }

    public String getCurrentWeather(String city) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            LocalDateTime currentDateTime = LocalDateTime.now();
            String formattedDateTime = currentDateTime.format(OUTPUT_DATE_TIME_FORMAT);
            String jsonRawData = downloadJsonRawData("weather", city);
            return parseJsonNode(jsonRawData, city, objectMapper, formattedDateTime);
        } catch (Exception e) {
            e.printStackTrace();
            return "В данный момент погодный сервис не работает, пожалуйста, попробуйте позже.";
        }
    }

    public List<WeatherTableItem> getWeatherForDate(String city, LocalDate startDate, LocalDate endDate) {
        try {
            String jsonRawData = downloadJsonRawData("forecast", city);
            return parseWeatherDataForDateRange(jsonRawData, city, startDate, endDate);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


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


    private List<String> convertRawDataToList(String data) throws Exception {
        List<String> weatherList = new ArrayList<>();

        JsonNode arrNode = new ObjectMapper().readTree(data).get("list");
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                String forecastTime = objNode.get("dt_txt").toString();
                if (forecastTime.contains("12:00")) {
                    weatherList.add(objNode.toString());
                }
            }
        }
        return weatherList;
    }

    private String parseForecastDataFromList(List<String> weatherList, String city) throws Exception {
        StringBuilder sb = new StringBuilder();

        for (String line : weatherList) {
            ObjectMapper objectMapper = new ObjectMapper();
            String dateTime = objectMapper.readTree(line).get("dt_txt").asText();
            LocalDateTime forecastDateTime = LocalDateTime.parse(dateTime, INPUT_DATE_TIME_FORMAT);
            String formattedDateTime = forecastDateTime.format(OUTPUT_DATE_TIME_FORMAT);
            sb.append(parseJsonNode(line, city, objectMapper, formattedDateTime));
        }
        return sb.toString();
    }
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

    private String determineWindDirection(double degree) {
        String[] directions = {"С", "ССЗ", "СЗ", "ЗСЗ", "З", "ЗЮЗ", "ЮЗ", "ЮЮЗ", "Ю", "ЮЮВ", "ЮВ", "ВЮВ", "В", "ВСВ", "СВ", "ССВ"};
        int index = (int) Math.round(((degree % 360) / 22.5));
        return directions[index % 16];
    }

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