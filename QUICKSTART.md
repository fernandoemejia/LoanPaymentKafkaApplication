# Quick Start Guide - Loan Payment Kafka Application

## 5-Minute Quick Start

### Step 1: Start Kafka (using Docker)

```bash
docker-compose up -d
```

Wait for Kafka to be healthy (about 10 seconds).

### Step 2: Build the Application

```bash
mvn clean package -DskipTests
```

### Step 3: Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Step 4: Submit a Test Payment

Open a terminal or use Postman/cURL:

```bash
curl -X POST http://localhost:8080/api/loan-payments/submit \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "12345678",
    "dueAmount": 500.00
  }'
```

You should see a response with a payment ID and status "PENDING".

### Step 5: Check Payment Status

Get the payment ID from the previous response and use it:

```bash
curl http://localhost:8080/api/loan-payments/{paymentId}
```

The status should change to "COMPLETED" after a moment.

### Step 6: Initialize Account Balance (Optional)

```bash
curl -X POST "http://localhost:8080/api/loan-payments/account/12345678/balance?balance=10000"
```

### Step 7: Check Payment Statistics

```bash
curl http://localhost:8080/api/loan-payments/statistics
```

## What's Happening Behind the Scenes?

1. When you POST to `/submit`, the REST API receives your request
2. The **LoanPaymentProducerService** sends the payment to Kafka
3. The Kafka **Consumer** picks up the message
4. The **PaymentService** processes the payment
5. The payment status is stored in memory
6. You can retrieve the status anytime using the payment ID

## Using Postman

Import the following requests into Postman:

### Submit Payment
```
POST http://localhost:8080/api/loan-payments/submit
Body (raw JSON):
{
  "accountNumber": "12345678",
  "dueAmount": 500.00
}
```

### Get Payment
```
GET http://localhost:8080/api/loan-payments/{paymentId}
```

### Get All Payments
```
GET http://localhost:8080/api/loan-payments/records/all
```

### Get Statistics
```
GET http://localhost:8080/api/loan-payments/statistics
```

### Health Check
```
GET http://localhost:8080/api/loan-payments/health
```

## Troubleshooting

### Kafka Not Starting
- Ensure Docker is running
- Check if ports 9092 and 2181 are available
- Run: `docker-compose logs kafka`

### Application Won't Start
- Check if port 8080 is available
- Review logs: `tail -f logs/loan-payment-app.log`
- Ensure Java 17+ is installed: `java -version`

### Consumer Not Processing Messages
- Check Kafka is running: `docker-compose ps`
- Review application logs for errors
- Ensure Kafka topic exists

## Testing with Multiple Payments

Create a test script:

```bash
#!/bin/bash

for i in {1..5}; do
  ACCOUNT="1234567$i"
  AMOUNT=$((100 + RANDOM % 900))
  
  curl -X POST http://localhost:8080/api/loan-payments/submit \
    -H "Content-Type: application/json" \
    -d "{
      \"accountNumber\": \"$ACCOUNT\",
      \"dueAmount\": $AMOUNT.00
    }"
  
  sleep 1
done
```

## Next Steps

1. **Explore the Code**: Check out the service classes in `src/main/java/com/loanpayment/`
2. **Modify Configuration**: Edit `src/main/resources/application.yml`
3. **Integrate with Database**: Replace in-memory storage with database
4. **Connect Real Payment Gateway**: Modify `PaymentService.executePayment()`
5. **Add More Features**: Implement retry logic, DLQ, etc.

## Stop the Application

```bash
# Stop application
Ctrl+C

# Stop Kafka and Zookeeper
docker-compose down
```

## Success Indicators

✅ Application starts without errors  
✅ You can submit payments via REST API  
✅ Payment status changes from PENDING to COMPLETED  
✅ Statistics endpoint shows processed payments  

You're ready to go! For more details, see [README.md](README.md)
