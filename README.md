# Loan Payment Kafka Application

A Spring Boot application that processes loan payments using Apache Kafka as a message broker. This application receives loan account numbers with due amounts and processes payments asynchronously.

## Features

- **Kafka Producer**: Sends loan payment requests to a Kafka topic
- **Kafka Consumer**: Consumes and processes loan payments from Kafka topic
- **Payment Processing**: Validates accounts and processes payments
- **REST API**: Exposes endpoints for submitting payments and retrieving records
- **Batch Processing**: Supports single and batch payment submissions
- **In-Memory Storage**: Stores payment records and account balances
- **Statistics**: Provides payment processing statistics

## Prerequisites

- Java 17 or higher
- Apache Kafka 3.x (running on localhost:9092)
- Maven 3.6+

## Installation & Setup

### 1. Clone/Download the Project

```bash
cd C:\LoanPaymentKafkaApplication
```

### 2. Start Kafka (if not already running)

Make sure Kafka is running on `localhost:9092`. If using Docker:

```bash
docker run -d \
  -p 9092:9092 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  confluentinc/cp-kafka:7.4.0
```

Or use a Docker Compose file to start both Zookeeper and Kafka.

### 3. Build the Application

```bash
mvn clean package
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

Or run the JAR file directly:

```bash
java -jar target/loan-payment-kafka-app-1.0.0.jar
```

The application will start on `http://localhost:8080`

## API Endpoints

### 1. Submit a Single Loan Payment

**POST** `/api/loan-payments/submit`

Request Body:
```json
{
  "accountNumber": "12345678",
  "dueAmount": 500.00
}
```

Response:
```json
{
  "message": "Loan payment submitted successfully",
  "paymentId": "uuid-string",
  "accountNumber": "12345678",
  "amount": 500.00,
  "status": "PENDING"
}
```

### 2. Submit Batch Loan Payments

**POST** `/api/loan-payments/submit-batch`

Request Body:
```json
[
  {
    "accountNumber": "12345678",
    "dueAmount": 500.00
  },
  {
    "accountNumber": "87654321",
    "dueAmount": 1000.00
  }
]
```

Response:
```json
{
  "message": "Batch loan payments submitted successfully",
  "count": 2,
  "status": "PENDING"
}
```

### 3. Get Payment Record by ID

**GET** `/api/loan-payments/{paymentId}`

Response:
```json
{
  "accountNumber": "12345678",
  "dueAmount": 500.00,
  "paymentDate": "2026-04-18T10:30:00",
  "status": "COMPLETED",
  "paymentId": "uuid-string",
  "processedAt": "2026-04-18T10:30:01"
}
```

### 4. Get All Payment Records

**GET** `/api/loan-payments/records/all`

Response: Returns a map of all payment records

### 5. Initialize Account Balance

**POST** `/api/loan-payments/account/{accountNumber}/balance`

Query Parameters:
- `balance`: The balance amount (e.g., 10000.00)

Example: `POST /api/loan-payments/account/12345678/balance?balance=10000.00`

Response:
```json
{
  "message": "Account balance initialized successfully",
  "accountNumber": "12345678",
  "balance": 10000.00
}
```

### 6. Get Account Balance

**GET** `/api/loan-payments/account/{accountNumber}/balance`

Response:
```json
{
  "accountNumber": "12345678",
  "balance": 9500
}
```

### 7. Get Payment Statistics

**GET** `/api/loan-payments/statistics`

Response:
```json
{
  "totalPayments": 10,
  "completedPayments": 9,
  "failedPayments": 1,
  "successRate": 90.0
}
```

### 8. Health Check

**GET** `/api/loan-payments/health`

Response:
```json
{
  "status": "UP",
  "service": "Loan Payment Kafka Application",
  "topic": "loan-payments-topic"
}
```

## Configuration

Edit `src/main/resources/application.yml` to customize:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: loan-payment-group

kafka:
  topic:
    loan-payments: loan-payments-topic
```

## How It Works

1. **Submit Payment Request**: User submits a loan payment via REST API
2. **Producer Sends to Kafka**: Payment request is sent to the Kafka topic with account number as key
3. **Consumer Listens**: Kafka consumer picks up the message from the topic
4. **Payment Processing**: Payment service validates the account and processes the payment
5. **Status Update**: Payment status is updated and stored in memory
6. **Retrieve Status**: User can retrieve the payment status using the payment ID

## Architecture

```
REST API (LoanPaymentController)
    ↓
Kafka Producer (LoanPaymentProducerService)
    ↓
Kafka Topic (loan-payments-topic)
    ↓
Kafka Consumer (LoanPaymentConsumerService)
    ↓
Payment Service (PaymentService)
    ↓
In-Memory Storage
```

## Technologies Used

- **Spring Boot** 3.1.5
- **Spring Kafka**
- **Apache Kafka**
- **Lombok**
- **Maven**

## Logging

Logs are saved to `logs/loan-payment-app.log` with a maximum size of 10MB.

View logs:
```bash
tail -f logs/loan-payment-app.log
```

## Development

### Adding a New Payment Status

1. Modify `LoanPayment` model if needed
2. Update payment processing logic in `PaymentService`
3. Update consumer logic in `LoanPaymentConsumerService`

### Integrating with Real Payment Gateway

Replace the mock `executePayment()` method in `PaymentService` with actual payment gateway integration.

## Testing

Run tests:
```bash
mvn test
```

## Troubleshooting

### Connection Refused (Kafka)
- Ensure Kafka is running on localhost:9092
- Check Kafka broker logs for errors

### Consumer Not Processing Messages
- Verify the topic name in `application.yml`
- Check consumer group is running
- Review application logs in `logs/loan-payment-app.log`

### Payment Processing Failed
- Verify account number format (8-12 digits)
- Check due amount is greater than zero
- Review payment service logs

## Future Enhancements

- Database integration for persistent storage
- Real payment gateway integration
- Message encryption for sensitive data
- Dead letter queue for failed payments
- Payment retry mechanism
- Transaction logging and audit trail
- Message compression

## License

This project is provided as-is for educational and development purposes.

## Support

For issues or questions, please review the logs and check the configuration settings.
