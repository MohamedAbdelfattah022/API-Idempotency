import asyncio
import aiohttp
import uuid
import time

BASE_URL = "http://localhost:8080/api/payments"
THREAD_COUNT = 20


async def send_request(session, endpoint, idempotency_key, payload, start_event):
    await start_event.wait()
    headers = {
        "Content-Type": "application/json",
        "Idempotency-Key": idempotency_key
    }
    try:
        async with session.post(f"{BASE_URL}/{endpoint}", json=payload, headers=headers) as response:
            text = await response.text()
            return response.status, text
    except Exception as e:
        return "ERROR", str(e)


async def simulate_concurrent_requests(endpoint, use_same_key=True):
    idempotency_key = str(uuid.uuid4()) if use_same_key else None
    payload = {
        "userId": "user-123",
        "amount": 100.50,
        "currency": "USD",
        "description": "Concurrent test payment"
    }

    start_event = asyncio.Event()

    async with aiohttp.ClientSession() as session:
        tasks = []
        for _ in range(THREAD_COUNT):
            key = idempotency_key if use_same_key else str(uuid.uuid4())
            tasks.append(send_request(
                session, endpoint, key, payload, start_event))

        task_group = asyncio.gather(*tasks)

        start_time = time.time()
        start_event.set()

        results = await task_group
        duration = time.time() - start_time

        for status, text in results:
            print(f"[{status}] {text}")
        print(
            f"\nCompleted {THREAD_COUNT} concurrent requests in {duration:.2f} seconds.\n")


if __name__ == "__main__":
    asyncio.run(simulate_concurrent_requests(
        "idempotent/lock", use_same_key=True))