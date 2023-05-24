import time
import RPi.GPIO as board
import asyncio

breaker_pin = 17
board.setmode(board.BCM)
board.setup(breaker_pin, board.IN)


async def func(time_to_spend: int) -> int:
    start_time = time.time()
    count, last = 0, board.input(breaker_pin)

    while time.time() - time_to_spend <= start_time:
        crt = board.input(breaker_pin)
        if (crt != last):
            last = crt
            count += 1

    # print(board.input(breaker_pin))
    # time.sleep(1)

    return count

async def get_breaks(time_spent: int):
    return await func(time_spent)


if __name__ == "__main__":
    minute_breaks, break_count = 0, 0
    while True:
        try:
            res = asyncio.run(get_breaks(60))
            break_count += 1
            minute_breaks += res
            print(res)

            if (break_count == 60):
                print(f"minute breaks: {minute_breaks}")
                minute_breaks = 0
                break_count = 0

        except KeyboardInterrupt:
            print('stopped')
            break
