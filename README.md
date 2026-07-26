# OrderLedger

OrderLedger is a Spring Boot backend application for managing products, orders, inventory, coupons, order status transitions, and order status history.

## Features

- Product creation, listing, retrieval, and update
- Order creation with multiple products
- Automatic inventory reduction
- Insufficient stock validation
- Duplicate product prevention inside the same order
- Coupon validation and percentage-based discounts
- Coupon expiration and usage-limit checks
- Order status state machine
- Inventory restoration when an order is cancelled
- Immutable order status history
- Optimistic locking for concurrent updates
- Database migrations with Flyway
- Swagger/OpenAPI documentation
- Integration and controller validation tests

## Technologies

- Java 21
- Spring Boot 3.2.5
- Spring Web
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- Jakarta Bean Validation
- Lombok
- Springdoc OpenAPI
- JUnit 5
- AssertJ
- Mockito
- Maven

## Project Structure

```text
src
├── main
│   ├── java/com/adil/orderledger
│   │   ├── controller
│   │   ├── dto
│   │   ├── exception
│   │   ├── model
│   │   ├── repository
│   │   └── service
│   └── resources
│       ├── db/migration
│       └── application.yaml
└── test
    ├── java/com/adil/orderledger
    │   ├── controller
    │   └── service
    └── resources
        └── application-test.yaml
```

## Main Business Rules

### Inventory

When an order is created:

- Requested stock must be available
- Product stock is reduced automatically
- If any item fails, the entire transaction is rolled back
- When an order is cancelled, its stock is restored

### Duplicate Products

The same product cannot appear more than once in a single order.

This rule is protected at both:

- Application level
- Database level with a unique constraint

### Coupons

A coupon must:

- Exist
- Be active
- Not be expired
- Have remaining usage capacity

Coupon codes are normalized before lookup.

### Order Status Workflow

Supported transitions:

```text
CREATED
├── PAID
└── CANCELLED

PAID
├── SHIPPED
└── CANCELLED

SHIPPED
└── COMPLETED
```

`COMPLETED` and `CANCELLED` are terminal statuses.

Every successful status change creates a history record.


## Main Features

- Create, retrieve, list, and update products
- Create orders with one or multiple products
- Store product price snapshots inside order items
- Automatically calculate order totals
- Automatically reduce product stock after order creation
- Prevent orders when available stock is insufficient
- Restore product stock when an order is cancelled
- Prevent the same product from appearing more than once in one order
- Apply percentage-based coupon discounts
- Validate coupon activation status
- Validate coupon expiration date
- Validate coupon usage limits
- Track coupon usage count
- Manage order status transitions through a state machine
- Prevent invalid order status transitions
- Store immutable order status history
- Roll back the entire transaction when order creation fails
- Return structured validation and business error responses
- Manage database schema changes with Flyway migrations
- Provide Swagger/OpenAPI documentation


## Bonus Features

- Optimistic locking for concurrent product updates
- Optimistic locking for concurrent coupon usage
- Database-level unique constraint for duplicate order items
- Database-level business constraints for prices, stock, quantities, and discounts
- Case-insensitive coupon code processing
- Separate service layer for product business logic
- Centralized global exception handling
- Integration tests with a dedicated PostgreSQL test database
- Transaction rollback tests
- Coupon integration tests
- Order status workflow tests
- Product creation and update tests
- Controller validation tests with MockMvc
- Environment-variable-based database configuration
- Separate application configuration for integration tests
- Java 21 and Spring Boot 3 architecture
- Maven Wrapper support for consistent builds


## Environment Variables

The application uses environment variables for database configuration.

```text
DB_URL=jdbc:postgresql://localhost:5432/orderledger
DB_USERNAME=postgres
DB_PASSWORD=your_password
```

For integration tests:

```text
TEST_DB_URL=jdbc:postgresql://localhost:5432/orderledger_test
TEST_DB_USERNAME=postgres
TEST_DB_PASSWORD=your_password
```

Do not commit real passwords to GitHub.

## Database Setup

Create the development database:

```sql
CREATE DATABASE orderledger;
```

Create the integration test database:

```sql
CREATE DATABASE orderledger_test;
```

Flyway automatically creates and updates the schema when the application starts.

## Running the Application

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux or macOS:

```bash
./mvnw spring-boot:run
```

## Running Tests

Windows:

```powershell
.\mvnw.cmd clean test
```

Linux or macOS:

```bash
./mvnw clean test
```

## API Documentation

After starting the application, Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## Main API Endpoints

### Products

```text
POST /api/products
GET  /api/products
GET  /api/products/{id}
PUT  /api/products/{id}
```

### Orders

```text
POST  /api/orders
GET   /api/orders/{id}
PATCH /api/orders/{id}/status
GET   /api/orders/{id}/history
```

## Example Product Request

```json
{
  "name": "Mechanical Keyboard",
  "unitPrice": 120.50,
  "stockQuantity": 10
}
```

## Example Order Request

```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ],
  "couponCode": "WELCOME10"
}
```

## Example Status Update Request

```json
{
  "newStatus": "PAID",
  "reason": "Payment completed"
}
```

## Tests

The project includes tests for:

- Application context loading
- Successful order creation
- Stock reduction
- Transaction rollback
- Insufficient stock
- Duplicate order items
- Missing products
- Valid and invalid state transitions
- Cancellation stock restoration
- Active, inactive, expired, missing, and exhausted coupons
- Coupon usage count
- Product creation and update
- Missing product update
- Request validation and HTTP 400 responses

