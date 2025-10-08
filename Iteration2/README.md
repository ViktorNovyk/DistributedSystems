# Iteration 2

## Project Setup

### Prerequisites
Make sure you have the following installed:

- **Java 25**  
  _(or use [SDKMAN!](https://sdkman.io); a `.sdkmanrc` file is included)_
- **Maven 3.9**  
  _(Maven Wrapper `mvnw` is included)_
- **Docker** running in the background
- Navigate to the **`Iteration2`** folder before running commands

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

The leader logic can be found here - [LeaderReplicationService](./src/main/java/com/example/leader/LeaderReplicationService.java)

The leader acts as a sequencer, assigning each incoming request a unique sequence number. This sequence number ensures consistent message ordering across all followers during replication.

Additionally, the leader keeps track of processed requests using the provided deduplication ID. If a request has already been processed, the leader skips invoking the followers, thereby avoiding redundant work.

The leader also supports specifying a writeConcern value, allowing to tune semi-synchronous replication.
### Follower Mode
The follower logic can be found here - [FollowerReplicationService](./src/main/java/com/example/follower/FollowerReplicationService.java)

The follower keeps track of processed requests using the provided deduplication ID. If a request has already been processed, the follower skips replication.

If the follower receives a request with a sequence number higher than that of the last processed request, it stores the request in a buffer and retries processing it once the missing ordered request becomes available.

There is also a [test](./src/test/java/com/example/FollowerReplicationServiceTest.java) that runs follower replication concurrently to verify that messages are processed in the correct order.
### Docker compose
The [docker-compose.yml](src/main/docker/docker-compose.yml) defines how the leader and followers are booted.

To add more followers, duplicate a follower service (update a service name and a port) in the file and add the leader's environment variable:
LEADER_FOLLOWER_URLS_3 (LEADER_FOLLOWER_URLS_4 etc.) - e.g., http://follower3:8080,http://follower4:8080.
