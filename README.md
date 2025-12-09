# Splitwise Application

A comprehensive expense splitting application built with Spring Boot, following enterprise-level architecture patterns and best practices.

## Architecture Overview

The application follows a clean, layered architecture:

- **Controller Layer**: REST API endpoints for handling HTTP requests
- **Service Layer**: Business logic and transaction management
- **Repository Layer**: Data access using Spring Data JPA
- **Entity Layer**: Domain models with JPA annotations
- **DTO Layer**: Data Transfer Objects for API requests/responses
- **Strategy Pattern**: For different expense split types

## Features

1. **User Management**: Create, read, update, and delete users
2. **Group Management**: Create groups, add/remove members
3. **Expense Management**: Create expenses with multiple split types
4. **Balance Tracking**: Automatic balance sheet calculation and updates
5. **Split Types**: Support for EQUAL, UNEQUAL, PERCENTAGE, and EXACT splits

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **H2 Database** (In-Memory)
- **Lombok** (for reducing boilerplate)
- **Jakarta Validation**

## API Endpoints

### User Management

- `POST /api/users` - Create a new user
- `GET /api/users/{userId}` - Get user by ID
- `GET /api/users` - Get all users
- `PUT /api/users/{userId}` - Update user
- `DELETE /api/users/{userId}` - Delete user

### Group Management

- `POST /api/groups` - Create a new group
- `GET /api/groups/{groupId}` - Get group by ID
- `GET /api/groups` - Get all groups
- `POST /api/groups/{groupId}/members/{userId}` - Add member to group
- `DELETE /api/groups/{groupId}/members/{userId}` - Remove member from group

### Expense Management

- `POST /api/expenses` - Create a new expense
- `GET /api/expenses/{expenseId}` - Get expense by ID
- `GET /api/expenses` - Get all expenses
- `GET /api/expenses/group/{groupId}` - Get expenses by group
- `GET /api/expenses/user/{userId}` - Get expenses by user

### Balance Sheet

- `GET /api/balance-sheets/{userId}` - Get balance sheet for a user

## Request/Response Examples

### Create User

```json
POST /api/users
{
  "userId": "user1",
  "name": "John Doe",
  "email": "john@example.com",
  "mobileNumber": "1234567890"
}
```

### Create Group

```json
POST /api/groups
{
  "groupId": "group1",
  "groupName": "Trip to Goa",
  "createdByUserId": "user1"
}
```

### Create Expense (Equal Split)

```json
POST /api/expenses
{
  "description": "Dinner",
  "expenseAmount": 1000.0,
  "paidByUserId": "user1",
  "splitType": "EQUAL",
  "userIds": ["user1", "user2", "user3"]
}
```

### Create Expense (Percentage Split)

```json
POST /api/expenses
{
  "description": "Hotel Booking",
  "expenseAmount": 5000.0,
  "paidByUserId": "user1",
  "splitType": "PERCENTAGE",
  "splits": [
    {"userId": "user1", "amount": 50.0},
    {"userId": "user2", "amount": 30.0},
    {"userId": "user3", "amount": 20.0}
  ]
}
```

### Create Expense (Exact Split)

```json
POST /api/expenses
{
  "description": "Shopping",
  "expenseAmount": 1500.0,
  "paidByUserId": "user1",
  "splitType": "EXACT",
  "splits": [
    {"userId": "user1", "amount": 500.0},
    {"userId": "user2", "amount": 700.0},
    {"userId": "user3", "amount": 300.0}
  ]
}
```

## Running the Application

1. Ensure Java 17 is installed
2. Run the application:
   ```bash
   ./gradlew bootRun
   ```
3. The application will start on `http://localhost:8080`
4. H2 Console available at `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:mem:splitwisedb`
   - Username: `sa`
   - Password: (empty)

## Database

The application uses H2 in-memory database. All data will be lost when the application is restarted. For production use, configure a persistent database in `application.properties`.

## Error Handling

All errors are returned in a consistent format:

```json
{
  "success": false,
  "message": "Error message",
  "data": null
}
```

## Validation

The API validates all input data:
- Required fields must be provided
- Amounts must be positive
- User/Group IDs must be unique
- Split amounts must match total expense amount

## Project Structure

```
src/main/java/com/example/splitwise/
├── controller/          # REST controllers
├── service/            # Business logic
├── repository/         # Data access layer
├── entities/           # JPA entities
├── dto/               # Data Transfer Objects
├── enums/             # Enumerations
├── strategy/          # Split strategy implementations
└── exception/         # Exception handlers
```

## Design Patterns Used

1. **Repository Pattern**: For data access abstraction
2. **Service Layer Pattern**: For business logic separation
3. **DTO Pattern**: For API request/response objects
4. **Strategy Pattern**: For different expense split algorithms
5. **Factory Pattern**: For creating split strategy instances
6. **Builder Pattern**: Using Lombok builders for entity creation

