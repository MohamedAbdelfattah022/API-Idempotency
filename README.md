# API Idempotency Demo

A Spring Boot application demonstrating idempotency for payment processing APIs to prevent duplicate
transactions from network retries or user errors.

## Features

- **Idempotent Payment Processing** - Prevents duplicate payments using idempotency keys
- **Distributed Lock Support** - Optional locking mechanism for concurrent request handling
- **Response Caching** - Stores and returns cached responses for duplicate requests
- **Unsafe Endpoint** - Included for comparison/testing purposes

## How It Works

1. **Client sends request** with an `Idempotency-Key` header
2. **System generates hash** of the idempotency key + request body
3. **Check for existing record** with matching key and hash
4. **If found**: Return cached response with original status code
5. **If not found**: Process payment and store the response
6. **Lock variant** uses distributed locking to handle concurrent requests

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Spring Data JPA
- H2 Database (in-memory)
- Lombok

### Running the Application

```bash
# Clone the repository
git clone <repository-url>

# Navigate to project directory
cd Idempotency

# Run with Maven
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### 1. Unsafe Payment Endpoint (No Protection)

```http
POST /api/payments/unsafe
Content-Type: application/json

{
  "amount": 100.00,
  "currency": "USD",
  "userId": "user123"
}
```

### 2. Idempotent Payment Endpoint

```http
POST /api/payments/idempotent
Content-Type: application/json
Idempotency-Key: <unique-key>

{
  "amount": 100.00,
  "currency": "USD",
  "userId": "user123"
}
```

### 3. Idempotent Payment with Lock

```http
POST /api/payments/idempotent/lock
Content-Type: application/json
Idempotency-Key: <unique-key>

{
  "amount": 100.00,
  "currency": "USD",
  "userId": "user123"
}
```

## Testing Concurrent Requests

A Python test script is included to simulate concurrent requests and verify idempotency behavior:

```bash
# Install dependencies
pip install aiohttp

# Run the test script
python test.py
```

The script sends 20 concurrent requests with the same idempotency key to demonstrate that only one payment is processed,
while the others return the cached response.

## H2 Console

Access the H2 database console at: `http://localhost:8080/h2-console`

- **JDBC URL**: `jdbc:h2:mem:idempotency_demo`
- **Username**: `sa`
- **Password**: _(empty)_

## Project Structure

```
src/main/java/com/mohamed/idempotency/
├── controller/      # REST API endpoints
├── dto/             # Request/Response objects
├── entity/          # JPA entities
├── repository/      # Database repositories
├── service/         # Business logic
└── utils/           # Utility classes
```