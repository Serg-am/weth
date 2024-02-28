package com.company.weth.core;

import com.company.weth.WeatherTableItemService;
import com.company.weth.entity.WeatherTableItem;
import com.haulmont.cuba.core.global.DataManager;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service(WeatherTableItemServiceBean.NAME)
public class WeatherTableItemServiceBean implements WeatherTableItemService {
    public static final String NAME = "weth_WeatherTableItemServiceBean";
    @Inject
    private DataManager dataManager;

    @Override
    public void saveWeatherItem(WeatherTableItem weatherItem) {
        dataManager.commit(weatherItem);
    }
}