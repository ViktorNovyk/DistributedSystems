# Iteration 1

## Project Setup

### Prerequisites
Make sure you have the following installed:

- **Java 21**  
  _(or use [SDKMAN!](https://sdkman.io); a `.sdkmanrc` file is included)_
- **Maven 3.9**  
  _(Maven Wrapper `mvnw` is included)_
- **Docker** running in the background
- Navigate to the **`Iteration1`** folder before running commands

---

## Build & Run

### 1. Build the Docker Image
```bash
./mvnw package spring-boot:build-image
```

### 2. Start Leader and Followers
```bash
docker compose -f src/main/docker/docker-compose.yml up
```

### 3. Send an HTTP Request
A sample request is available in [`request.http`](./requests.http).  
You can execute it to interact with the application.

---

## Implementation Details

The application runs in **two modes**, depending on the active profile:

### Leader Mode
- Activates the **leader controller** and **replication service**:
    - [`LeaderController`](src/main/java/com/example/leader/LeaderController.java)
    - [`LeaderReplicationService`](src/main/java/com/example/leader/LeaderReplicationService.java)
- The leader sends requests to followers **concurrently** using:
    - `ExecutorService`
    - `CompletableFuture`

### Follower Mode
- Activates the **follower controller** and **replication service**:
    - [`FollowerController`](src/main/java/com/example/follower/FollowerController.java)
    - [`FollowerReplicationService`](src/main/java/com/example/follower/FollowerReplicationService.java)
- The follower introduces a **random delay** to simulate slow responses.

### Docker compose
The [docker-compose.yml](src/main/docker/docker-compose.yml) defines how the leader and followers are booted.

To add more followers, duplicate a follower service (update a service name and a port) in the file and add the leader's environment variable:
LEADER_FOLLOWER_URLS_3 (LEADER_FOLLOWER_URLS_4 etc.) - e.g., http://follower3:8080,http://follower4:8080.
