package com.company.weth;

import com.company.weth.entity.WeatherTableItem;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component(WeatherTableItemService.NAME)
public interface WeatherTableItemService {
    String NAME = "weatherTableItemService";

    void saveWeatherItem(WeatherTableItem weatherItem);
}
