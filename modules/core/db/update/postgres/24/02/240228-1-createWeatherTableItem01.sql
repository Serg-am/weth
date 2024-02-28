create table WETH_WEATHER_TABLE_ITEM (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    CITY varchar(255),
    DATETIME varchar(255),
    TEMPERATURE varchar(255),
    FEELS_LIKE varchar(255),
    PRESSURE varchar(255),
    HUMIDITY varchar(255),
    DESCRIPTION varchar(255),
    WIND_SPEED varchar(255),
    WIND_DIRECTION varchar(255),
    --
    primary key (ID)
);