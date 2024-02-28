-- begin WETH_WEATHER_TABLE_ITEM
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
    TEMPERATURE double precision,
    FEELS_LIKE double precision,
    PRESSURE double precision,
    HUMIDITY integer,
    DESCRIPTION varchar(255),
    WIND_SPEED double precision,
    WIND_DIRECTION varchar(255),
    --
    primary key (ID)
)^
-- end WETH_WEATHER_TABLE_ITEM
