# Cash Desk Module API

## Project Description
The **Cash Desk Module API** provides endpoints for managing cash balances and transactions for a cashier system. The API enables fetching cash balances within a specified date range and supports proper validation and error handling.

## Technologies Used
- Java 17
- Spring Boot
- Spring Transactions module `spring-tx`
- Maven
- Hibernate Validator (Jakarta Validation API)
- Lombok
- SLF4J Logging

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
| `currency`      | `string`             | Yes      | Currency type (e.g., `EUR`, `USD`).                                                                          |
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
- `currency` must be a valid currency code (e.g., `EUR`, `USD`).

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

---

## Logging transactions and balances
All transactions and cashiers' balances are logged in the text files `transactions.txt` and `balances.txt` directly in the project's main folder `cashoperations`.

---

## How to Run the Project
### Prerequisites
Ensure you have the following installed:
- Java 17
- Maven
- Docker (for database and Redis setup)

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

