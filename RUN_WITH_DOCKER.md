## Run the Project with Docker Compose

- Prerequisites: Docker Desktop/Engine with Compose v2 available.
- Unzip the project and enter the folder:
  - `unzip splitwise.zip`
  - `cd splitwise`
- Build and start both containers:
  - `docker compose up --build -d`
  - Compose builds the Spring Boot app image and starts PostgreSQL (`splitwise-db`) plus the app (`splitwise-app`). The app waits for the DB healthcheck before starting.
- Verify the services:
  - `docker compose ps` to confirm containers are healthy.
  - `docker compose logs -f app` to watch the application boot and see migrations/DDL.
- Use the API at `http://localhost:8080`. Default DB credentials/host/port are set in `docker-compose.yml` and `application.properties` (override with env vars if needed).
- Test the endpoints with the included Postman collection: `Splitwise_API.postman_collection.json`.
- Stop and clean up:
  - `docker compose down -v` to stop containers and remove the PostgreSQL data volume.
