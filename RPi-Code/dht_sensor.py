#!/usr/bin/python3

import Adafruit_DHT

def get_humidity() -> float:
    humidity, _ = Adafruit_DHT.read_retry(11, 27)
    return humidity

def get_temperature() -> float:
    _, temperature = Adafruit_DHT.read_retry(11, 27)
    return temperature

def get_both() -> tuple[float, float]:
    humidity, temperature = Adafruit_DHT.read_retry(11, 27)

    return (humidity, temperature)

if __name__ == '__main__':
    while True:
        try:
            print('Temp: {}C  Humid: {}%'.format(get_temperature(), get_humidity()))
        except KeyboardInterrupt:
            print('Stopped')
            break
