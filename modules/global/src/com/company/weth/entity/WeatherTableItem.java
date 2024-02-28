package com.company.weth.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.*;

@NamePattern("%s|dateTime")
@Table(name = "WETH_WEATHER_TABLE_ITEM")
@Entity(name = "weth_WeatherTableItem")
public class WeatherTableItem extends StandardEntity {
    private static final long serialVersionUID = 3671326004574852309L;

    @Column(name = "CITY")
    protected String city;

    @Column(name = "DATETIME")
    protected String dateTime;

    @Column(name = "TEMPERATURE")
    protected Double temperature;

    @Column(name = "FEELS_LIKE")
    protected Double feelsLike;

    @Column(name = "PRESSURE")
    protected Double pressure;

    @Column(name = "HUMIDITY")
    protected Integer humidity;

    @Column(name = "DESCRIPTION")
    protected String description;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(Double feelsLike) {
        this.feelsLike = feelsLike;
    }

    public Double getPressure() {
        return pressure;
    }

    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    @Column(name = "WIND_SPEED")
    protected Double windSpeed;

    @Column(name = "WIND_DIRECTION")
    protected String windDirection;


}