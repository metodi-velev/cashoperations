# Cash Desk Module API

---

## Run the `cashoperations, cashreportingservice and cashdocumentsservice` microservices:
### 1. Open a command prompt in the `docker-compose\default` directory
### 2. Run `docker-compose up -d` in the terminal

---

## Project Description
The **Cash Desk Module API** provides endpoints for managing cash balances and transactions for a cashier system. The API enables fetching cash balances within a specified date range and supports proper validation and error handling. The most up-to-date branch at the moment is the **read-config-files-from-cashoperations-config-repo** branch. This branch demonstrates a microservice configuration setup using Spring Cloud Config Server. It features automatic, distributed configuration updates via Spring Cloud Bus and RabbitMQ with comprehensive monitoring through Spring Cloud's observability tools.

## Technologies Used
- Java 17 (upgraded to Java 21)
- Spring Boot
- Spring Transactions module `spring-tx`
- Maven
- Hibernate Validator (Jakarta Validation API)
- Lombok
- SLF4J Logging

---

## NEW API Endpoints in Cashdocumentsservice Microservice

### 1. Upload custom files (and generate daily cashoperations summary report)
**Endpoint:** `POST /api/v1/documents/uploader` <br>
**Example request:**
<br>`POST http://localhost:8082/cashdocumentsservice/api/v1/documents/uploader`

**Description:** Upload custom files selected by the user. If the user selects `getAndSaveDailySummary`
with `yes` value, then an asynchronous HTTP Request is sent with the help of WebClient to get
the cashoperations daily summary report from the `cashreportingservice` microservice. 
The daily summary report is then saved to the in memory H2 DB and to the file system.

**Request Parameters:**
| Parameter   | Type         | Required | Description                                      |
|------------|-------------|----------|--------------------------------------------------|
| `fileGroup` | `String` (ISO-8601) | Yes       | Example value: `"certificates"` |
| `files`   | `MultipartFile[]` (ISO-8601) | Yes       | Custom file(s) to be uploaded < 100KB each   |
| `getAndSaveDailySummary`  | `String`     | No       | Example value: `"yes"`, get cash operations daily summary from cashreportingservice using WebClient                   |

**Response:**
```json
HTTP Response Status 201 Created
```

---

### 2. Get and save to disk all files belonging to `fileGroup`. If more than
    one file is found, zip them and then save the zipped file.
**Endpoint:** `GET /api/v1/documents/downloader` <br>
**Example request:**
<br>`GET http://localhost:8082/cashdocumentsservice/api/v1/documents/downloader?fileGroup=certificates`

**Description:** Get and save to disk all files belonging to `fileGroup`. If more than
one file is found, zip them and then save the zipped file.

**Request Parameters:**
| Parameter   | Type         | Required | Description                                      |
|------------|-------------|----------|--------------------------------------------------|
| `fileGroup` | `String` (ISO-8601) | Yes       | Example value: `"certificates"` |

**Response:**
```json
HTTP Response Status 200 OK
HTTP Response body: ResponseEntity<byte[]>
```

---

### 3. Get the metadata to all files saved in the H2 DB.
**Endpoint:** `GET /api/v1/documents/metadata` <br>
**Example request:**
<br>`GET http://localhost:8082/cashdocumentsservice/api/v1/documents/metadata`

**Description:** Get the metadata to all files saved in the H2 DB.

**Response:**
```json 
HTTP Response Status 200 OK
```

```json
[
  {
    "id": 1,
    "fileGroup": "reports",
    "fileName": "daily_summary_20250928_131121.txt"
  },
  {
    "id": 2,
    "fileGroup": "certificates",
    "fileName": "Test Document.txt"
  },
  {
    "id": 3,
    "fileGroup": "reports",
    "fileName": "daily_summary_20250928_131844.txt"
  },
  {
    "id": 4,
    "fileGroup": "reports",
    "fileName": "daily_summary_20250928_134342.txt"
  }
]
```

---

## API Endpoints

### 1. Fetch Cash Balances
**Endpoint:** `GET /api/v1/cash-balance` <br>
**Example request with all filters applied:** 
<br>`GET http://localhost:8080/api/v1/cash-balance?dateFrom=2025-02-28T00:50:00&dateTo=2025-03-28T00:00:00&cashier=linda`

**Description:** Retrieves the cash balances for a given cashier within a specific date range.

**Request Parameters:**
| Parameter   | Type         | Required | Description                                      |
|------------|-------------|----------|--------------------------------------------------|
| `dateFrom` | `String` (ISO-8601) | No       | Start date in format `yyyy-MM-dd'T'HH:mm:ss` |
| `dateTo`   | `String` (ISO-8601) | No       | End date in format `yyyy-MM-dd'T'HH:mm:ss`   |
| `cashier`  | `String`     | No       | The identifier of the cashier                   |

**Validation Rules:**
- `dateFrom` and `dateTo` must follow the format `yyyy-MM-dd'T'HH:mm:ss`.
- `dateTo` must be after `dateFrom`.
- If `dateTo` is before `dateFrom`, an `InvalidDateRangeException` is thrown.

**Response:**
```json
[
  {
    "timestamp": "2025-02-28T02:59:47.6906304",
    "cashier": "LINDA",
    "balances": {
      "EUR": [
        {
          "quantity": 100,
          "value": 10,
          "totalAmount": 2000,
          "timestamp": "2025-02-28T02:42:44"
        },
        {
          "quantity": 20,
          "value": 50,
          "totalAmount": 2000,
          "timestamp": "2025-02-28T02:42:44"
        }
      ],
      "BGN": [
        {
          "quantity": 10,
          "value": 50,
          "totalAmount": 1000,
          "timestamp": "2025-02-28T02:42:44"
        },
        {
          "quantity": 50,
          "value": 10,
          "totalAmount": 1000,
          "timestamp": "2025-02-28T02:42:44"
        }
      ]
    }
  }
]
```

**Error Handling:**
- `400 Bad Request` if `dateTo` is before `dateFrom`.
- `404 Not Found` if no records exist.

---

## Validation & Exception Handling
- Uses `@ExceptionHandler` in `GlobalExceptionHandler` to return consistent error responses.
- Logs errors with SLF4J but does not log `null` values.

**Example Error Response:**
```json
{
  "timestamp": "2024-02-28T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "dateTo must be after dateFrom",
  "path": "/api/v1/cash-balance"
}
```

---

### POST /api/v1/cash-operation
### 2. Deposits and Withdrawals
**Endpoint:** `POST /api/v1/cash-operation` <br>
**Example request for a deposit:** <br>
`POST http://localhost:8080/api/v1/cash-operation`

#### Description
Registers a cash operation (deposit/withdrawal) for a cashier.

#### Request Body
| Field           | Type                 | Required | Description                                                                                                  |
|-----------------|----------------------|----------|--------------------------------------------------------------------------------------------------------------|
| `cashierName`   | `string`             | Yes      | Identifier of the cashier.                                                                                   |
| `amount`        | `number`             | Yes      | Amount of the transaction (for deposits or for withdrawals).                                                 |
| `currency`      | `string`             | Yes      | Currency type (e.g., `BGN`, `EUR`).                                                                          |
| `operationType` | `string`             | Yes | `DEPOSIT` or `WITHDRAWAL` operation.                                                                         |
| `denominations` | `List<Denomination>` | Yes | List of the denomination of the operation. I.g., `quantity: 2, value: 50` - 2 banknotes of 50 euros or levs. |

#### Sample Request
```http
POST {{baseUrl}}/api/v1/cash-operation
Content-Type: application/json
```
#### Request Body
```json
{
   "cashierName": "LINDA",
   "currency": "EUR",
   "operationType": "DEPOSIT",
   "amount": 200,
   "denominations": [
      { "quantity": 5, "value": 20 },
      { "quantity": 2, "value": 50 }
   ]
}
```

#### Sample Response - Status `200` OK
```text
  Operation successful.
```

## Validation and Error Handling

- If the `amount` in the request does not match the overall denominations sum in the request, the API will throw a custom `InvalidAmountException` and will return a `400 Bad Request` status code.
- `amount` must be a positive value, not null and at leat 10.
- `currency` must be a valid currency code (e.g., `BGN`, `EUR`).

#### Sample Error Response
```json
{
   "apiPath": "uri=/api/v1/cash-operation",
   "errorCode": "BAD_REQUEST",
   "errorMessage": "Invalid deposit request. Amount 300 does not match overall denominations sum 200.",
   "errorTime": "2025-02-28T10:59:27.9152112"
}
```
---

## Postman for API calls
A sample postman collection and an environment can be found in the folder `cashoperations\postman`:
- `Cash Desk Module API.postman_collection.json` contains sample API calls.
- `Cash Desk Environment.postman_environment.json` contains the environment settings for the API calls.
- Both must be imported in Postman to properly test the API calls.
- Set the request header key `FIB-X-AUTH` to the `{{authKey}}` value defined in the environment settings. The value for this header is also defined in the `application.properties` file. 

---

## Logging transactions and balances
All transactions and cashiers' balances are logged in the text files `transactions.txt` and `balances.txt` directly in the project's main folder `cashoperations`.

---

## How to Run the Project
### Prerequisites
Ensure you have the following installed:
- Java 17
- Maven

### Running Locally
1. Clone the repository:
   ```sh
   git clone https://github.com/metodi-velev/cashoperations.git
   cd cashoperations
   ```

2. Build the project:
   ```sh
   mvn clean install
   ```
3. Run the Spring Boot application:
   ```sh
   mvn spring-boot:run
   ```

### Running Tests
To execute tests:
```sh
mvn test
```

To execute integration tests named `*IT.java`:
```sh
mvn verify
```
---

## Performance Benchmark Analysis: Java 17 Platform Threads vs Java 21 Virtual Threads

### 📊 Performance Comparison Summary

| Metric | Java 17 (Platform Threads) | Java 21 (Virtual Threads) | Improvement |
|--------|----------------------------|---------------------------|-------------|
| **Throughput** | 1,552.1 req/sec | **1,696.1 req/sec** | **+9.3%** |
| **Mean Response Time** | 29.01 ms | **26.52 ms** | **-8.6%** |
| **Median Response Time** | 32 ms | **28 ms** | **-12.5%** |
| **90th Percentile (pct1)** | 45 ms | **37 ms** | **-17.8%** |
| **95th Percentile (pct2)** | 51 ms | **43 ms** | **-15.7%** |
| **99th Percentile (pct3)** | 70 ms | **66 ms** | **-5.7%** |
| **Total Requests** | 465,238 | **508,639** | **+9.3%** |
| **Max Response Time** | 707 ms | **576 ms** | **-18.5%** |

### 🔍 Key Insights

#### 1. **Throughput Improvement (+9.3%)**
Virtual threads handle the 10→10,000 user spike more efficiently, processing **43,401 more requests** in the same 5-minute test period.

#### 2. **Latency Reduction (8.6-17.8%)**
- **Average latency decreased** from 29ms to 26.5ms
- **Tail latency (p90/p95) improved significantly** - virtual threads better handle congestion
- **Maximum response time reduced** by 131ms (18.5%) - fewer extreme outliers

#### 3. **Virtual Thread Efficiency**
The results confirm this is an **I/O-bound workload** (file write operations), where virtual threads excel by:
- Eliminating thread pool contention
- Reducing context switching overhead
- Handling many more concurrent connections with less memory

### 🚀 Further Optimization Opportunities

#### 1. **JVM Tuning for Virtual Threads**
```bash
    # Add these to your Java 21 virtual threads setup
    -XX:+UseZGC  # Better for large heaps and low latency
    -XX:ZAllocationSpikeTolerance=5.0
    -XX:SoftMaxHeapSize=3g  # More aggressive GC
    -XX:-UseBiasedLocking  # Not needed with virtual threads
```

#### 2. **Database Connection Pool Optimization**
```bash
    # HikariCP or similar configuration
    spring:
      datasource:
        hikari:
          maximum-pool-size: 200  # Increase from default 10
          minimum-idle: 20
          connection-timeout: 30000
          idle-timeout: 600000
          max-lifetime: 1800000
```

#### 3. **SQL and Application Layer**
```bash
    // Enable these for better virtual thread performance
    -Djdk.tracePinnedThreads=true  // Detect pinned threads
    -Djdk.virtualThreadScheduler.parallelism=4  // Match CPU cores
    -Djdk.virtualThreadScheduler.maxPoolSize=256
```

#### 4. **Garbage Collection Tuning**
```bash
    # More aggressive GC settings for response time
    -XX:ZCollectionInterval=10  # More frequent collections
    -XX:ZUncommitDelay=300      # Uncommit unused memory faster
    -XX:SoftRefLRUPolicyMSPerMB=0  # Aggressive soft reference clearing
```

#### 5. **Application-Level Improvements**
- **Implement connection pooling** at application level if not already done
- **Use prepared statement caching**
- **Batch operations** where possible
- **Add second-level caching** (Redis, Hazelcast, etc.)
- **Optimize SQL queries** - check for missing indexes

#### 6. **JMeter Test Configuration**
- **Increase ramp-up time** to 60-120 seconds for smoother load increase
- **Use throughput shaping timer** for more realistic load patterns
- **Add think times** between requests (100-300ms)
- **Test with different connection pool sizes** to find optimal setting

#### 7. **Monitoring and Profiling**
```bash
    # Add these JVM flags for profiling
    -XX:+FlightRecorder
    -XX:StartFlightRecording=filename=recording.jfr
    -Dorg.jboss.logging.provider=slf4j
    -Djava.util.logging.manager=org.jboss.logmanager.LogManager
```

#### **Expected Next Steps**
1. **Run longer tests** (15-30 minutes) to see if the improvement sustains
2. **Test with different user patterns** (steady state vs spikes)
3. **Monitor database performance**  - it might become the next bottleneck
4. **Consider connection pool sizing** - you might be able to handle even more concurrent connections

#### **Conclusion**

The 9.3% throughput improvement and 8-18% latency reduction demonstrate that virtual threads are <br>
working effectively for your I/O-bound workload. The optimization potential is significant, and with the <br>
additional tuning suggestions, you could likely achieve 15-25%+ overall improvement compared to the <br>
original Java 17 platform thread implementation.

The results confirm that virtual threads provide substantial benefits for high-concurrency, I/O-intensive <br> 
applications like your cash operation service.

---
## Docker

### Prerequisites
- Install [Docker Desktop](https://www.docker.com/products/docker-desktop) and ensure it is running on your machine.
- Ensure `docker-compose` is available on your system.

To build and start the application container:

```sh
docker-compose up --build
```

To stop the application and remove the containers, networks and volumes created by Docker Compose run:

```sh
docker-compose down
```
If you need to rebuild the Docker image without using the cache:

```sh
docker-compose build --no-cache
```

```sh
docker-compose up
```

To follow the logs of the running containers:

```sh
docker-compose logs -f

```

---

## Monitoring & Logging for Future Releases
- **Prometheus & Grafana:** Can be integrated for API performance monitoring.
- **Logging:** Uses SLF4J and ensures no `null` values are logged.
- **ELK Stack:** Can be configured for external exception logging.

---

## Security & Authentication
- API authentication via a custom header `X-API-KEY`.
- JWT authentication with Spring Security is an option for future versions.
- Role-Based Access Control (RBAC) implementation could be considered.

---

## Contribution Guidelines
1. Fork the repository.
2. Create a feature branch (`feature-new`).
3. Commit changes with proper messages.
4. Push to the branch and create a pull request.

---

## License
MIT License

---

## Contact
For issues or feature requests, please create an issue in the repository or contact `metodi.velev@example.com`. 🚀

