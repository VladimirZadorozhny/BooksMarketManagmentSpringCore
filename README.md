# Books Market Console Application - Pet Project

A small Spring Boot console application that simulates a tiny “books market” (library).
It is built as a study/practice app, runs in the terminal, uses plain SQL via Spring JDBC
and demonstrates a simple layered design (domain → repositories → services → console UI).

---

### Features
- Users: list, find by id/name/email, create
- Authors: list, find by id/name, create
- show available/unavailable books
- Rent/return books with validation and row locking (SELECT … FOR UPDATE)
- Interactive console menu (no web UI)

### Tech stack
- Java 17, Spring Boot 3.5.x
- Spring JDBC (JdbcClient) — no ORM
- MySQL 8.x
- Flyway for DB migrations
- JUnit and Mockito for testing
- Maven

#### Using Maven Wrapper (recommended)
The project includes Maven Wrapper files (`mvnw`, `mvnw.cmd`). If `mvn` is not available or Maven is not installed, use the wrapper:

- Linux/macOS: `./mvnw <goal>`
- Windows PowerShell: `./mvnw <goal>`
- Windows CMD: `mvnw.cmd <goal>`

Examples:
- Run app: `./mvnw spring-boot:run`
- Run tests: `./mvnw test`
- Refresh dependencies and build: `./mvnw -U clean package`

### Project layout
src/main/java/org/mystudying/booksmarket2
  ├─ domain/        # Plain domain objects: User, Author, Book, Booking
  ├─ repositories/  # SQL + JdbcClient
  ├─ services/      # Business logic + transactions
  ├─ ConsoleUI.java # Terminal menu (CommandLineRunner)
  └─ BooksMarket2Application.java
src/main/resources
  ├─ application.properties
  └─ db/migration/  # Flyway SQL migrations (V1__*.sql, V2__*.sql, ...)
docker-compose.yml

---

### Option A — Run with Docker Compose (easiest)
1) Start MySQL in Docker
   docker compose up -d

   This creates a booksmarket database with user "user1" and password "user1" on port 3307.

2) Run the app (from project root)
   mvn spring-boot:run
   # or with Maven Wrapper:
   ./mvnw spring-boot:run   # for Linux/macOS or Windows PowerShell
   mvnw.cmd spring-boot:run # for Windows CMD

3) What happens on first start:
- Spring Boot connects to MySQL using the credentials above (configurable via env vars below).
- Flyway runs migrations from src/main/resources/db/migration:
  - V1__init_schema.sql creates tables
  - V2__seed_data.sql inserts demo data
- The console menu appears, e.g.:
   --- Books Market Main Menu ---
   1. User Management
   2. Author Management
   3. Book Management
   0. Exit

To stop the database:
   docker compose down

---

### Option B — Run against your local MySQL
1) Create a database named booksmarket (or choose another name and adjust the URL).

2) Set connection parameters (recommended via environment variables):
   Windows PowerShell
   $env:DB_URL="jdbc:mysql://localhost:3307/booksmarket"
   $env:DB_USER="user1"
   $env:DB_PASSWORD="user1"

   Linux/macOS
   export DB_URL="jdbc:mysql://localhost:3307/booksmarket"
   export DB_USER="user1"
   export DB_PASSWORD="user1"

3) Run the app
   mvn spring-boot:run
   or with Maven Wrapper
   ./mvnw spring-boot:run   for Linux/macOS or Windows PowerShell and
   mvnw.cmd spring-boot:run  for Windows CMD

Flyway will automatically create tables and insert demo data on the first run.

Note: While \`src/main/resources/sql/booksMarket.sql\` can still be used to manually reset the database, Flyway is the recommended approach for migrations.

---

### Configuration notes
The \`application.properties\` file reads database settings from environment variables, providing safe defaults:
   spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3307/booksmarket}
   spring.datasource.username=${DB_USER:user1}
   spring.datasource.password=${DB_PASSWORD:user1}
   spring.flyway.enabled=true
   spring.flyway.baseline-on-migrate=true

---

### Running tests
   mvn test
   or with Maven Wrapper
   ./mvnw test           for Linux/macOS or Windows PowerShell
   and mvnw.cmd test         for Windows CMD

Tests expect a running MySQL instance.

---

### Troubleshooting
- Can’t connect to DB: check DB_URL, DB_USER, DB_PASSWORD, and that MySQL is listening on 3307.
- Build complains about Flyway version: Run \`mvn -U clean package\` (or \`./mvnw -U clean package\`) to update dependencies.
- MySQL in Docker keeps old data: remove the named volume with "docker compose down -v" (this resets the DB!).
- If MySQL ever boot‑loops: docker compose down -v to reset the volume, then docker compose up -d.
- Port conflict (e.g., on 3307): Either free the port or remap it to another host port (e.g., 3308) and adjust \`DB_URL\` accordingly.
---

