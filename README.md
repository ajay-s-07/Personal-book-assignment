# Personal Book List with Google Books Integration

## How to Run (Local)
**Prerequisites**
- Java 8+ (recommended: Java 17+)
- Maven 

**Steps**
1. Clone the repository and open a terminal in the project root (folder containing `pom.xml`).
2. Start the application:
   - Using Maven:
     - `mvn spring-boot:run`
3. Application will start on:
   - `http://localhost:8080`

---

## What I Newly Added (Assignment Requirement)

### New Endpoint: Add a book to personal list by Google Volume ID
- **POST** `/books/{googleId}`

**Behavior**
- Fetches a single Google Books volume via `GoogleBookService.getBookById(googleId)` which calls Google Books:
  - `GET /volumes/{id}`
- Maps fields from Google response to `Book`:
  - `id` (Google volume ID)
  - `title`
  - first author
  - `pageCount`
- Persists the mapped `Book` using `BookRepository`
- Returns **201 Created** with the saved `Book`

---

## Endpoints

### Get all saved books (H2 in-memory)
- **GET** `/books`

### Search Google Books (upstream schema passthrough)
- **GET** `/google?q={query}&maxResults={n}&startIndex={n}`

### Add book by Google ID (NEW)
- **POST** `/books/{googleId}`

---

## How to Test
Run all tests:
- `mvn test`

**Test Notes**
- Google Books API calls are mocked using **MockWebServer** to avoid quota/flakiness.
- Mock payload file: `src/test/resources/single_volume.json`
- Tests cover:
  - `GET /books` returns persisted books
  - `POST /books/{googleId}` happy path → 201 + persisted
  - `POST /books/{googleId}` invalid ID → 400 + nothing persisted

---

## Notes
- H2 is in-memory, so data resets on application restart.
- `/google` endpoint returns upstream Google schema as-is; real Google API may rate-limit, so tests mock it.

