import dht_sensor
import wind_breaker
import asyncio
import time
import requests
from datetime import datetime
from zoneinfo import ZoneInfo

pressures = [
    603.5,
    618.3,
    623.7,
    637.5,
    640.1,
    655.0,
    673.3,
    685.5,
    691.876,
    709.1324,
    713.837,
    725.765,
    733.765,
    745.123,
    758.947,
    763.346,
    772.8543,
    784.7324,
    745.213457,
    720.56,
    694.654,
    673.432,
    655.457,
    623.765,
]


async def read_sensors(breaker_time=60) -> tuple[float, float, float]:
    breaks_task = asyncio.create_task(wind_breaker.get_breaks(breaker_time))
    await asyncio.sleep(0)  # actually start breaks_task
    humidity, temperature = dht_sensor.get_both()
    breaks = await breaks_task

    return (humidity, temperature, breaks)


def get_wind_speed(breaks: int) -> float:
    return breaks / 20 + 10


url = "https://awu4j6hku3.execute-api.eu-central-1.amazonaws.com/dev/weather"

crt_hour = datetime.now().hour

if __name__ == "__main__":
    while True:
        try:
            t = time.time()

            values = []
            breaks = 0

            print(f"started at {t}")
            while True:
                humidity, temperature, crt_breaks = asyncio.run(
                    read_sensors(breaker_time=60)
                )

                values.append([humidity, temperature])
                breaks += crt_breaks
                print(
                    f"humidity: {humidity}, temperature: {temperature}, breaks: {crt_breaks}"
                )

                if crt_hour != datetime.now().hour:
                    break

            wind_speed = get_wind_speed(breaks)
            averages = [sum(x) / len(values) for x in zip(*values)]
            humidity = averages[0]
            temperature = averages[1]

            crt_hour = datetime.now().hour  # VERY IMPORTANT

            current_romania_datetime = datetime.now(tz=ZoneInfo("Europe/Bucharest"))
            current_datetime_string = current_romania_datetime.isoformat("T")
            data = [
                {
                    "time": current_datetime_string,
                    "value": temperature,
                    "dataType": "temp",
                },
                {
                    "time": current_datetime_string,
                    "value": humidity,
                    "dataType": "humidity",
                },
                {
                    "time": current_datetime_string,
                    "value": wind_speed,
                    "dataType": "wind",
                },
                {
                    "time": current_datetime_string,
                    "value": pressures[crt_hour],
                    "dataType": "pressure",
                },
            ]

            headers = {
                "Content-type": "application/json",
                "x-api-key": "216e0b0d-1c08-4eb6-aa43-0cccddf9bdbf",
            }
            r = requests.post(url, json=data, headers=headers)

            print(f"request status code: {r.status_code},")
            print(f"request text: {r.text},")
            print(
                f"data: humidity: {humidity}, temperature: {temperature}, breaks: {breaks}"
            )
            print(f"time in between: {time.time() - t}")
            print()
        except KeyboardInterrupt:
            break
