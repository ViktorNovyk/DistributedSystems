# heartbeat and quorum check

```text
Test 1:
Pause both followers
Call replication
Get quorum error

Test 2:
Pause one follower
Call replication with write concern 3
Get error because not enough followers
```

### Test 1
#### Pause both followers
```shell
docker pause iteration3-follower1-1
docker pause iteration3-follower2-1
```

#### Wait for heartbeat execution
```text

 leader-1     | 2025-11-11T20:11:47.167Z  INFO 1 --- [Iteration3-Leader] [   scheduling-1] com.example.leader.HeartbeatService      : Follower follower2 changed status from HEALTHY to SUSPECTED
 leader-1     | 2025-11-11T20:11:47.175Z  INFO 1 --- [Iteration3-Leader] [   scheduling-1] com.example.leader.HeartbeatService      : Updated states: {follower2=SUSPECTED, follower1=HEALTHY}
 leader-1     | 2025-11-11T20:11:57.185Z  INFO 1 --- [Iteration3-Leader] [   scheduling-1] com.example.leader.HeartbeatService      : Follower follower2 changed status from SUSPECTED to UNHEALTHY
 leader-1     | 2025-11-11T20:11:57.193Z  INFO 1 --- [Iteration3-Leader] [   scheduling-1] com.example.leader.HeartbeatService      : Updated states: {follower2=UNHEALTHY, follower1=HEALTHY}
 leader-1     | 2025-11-11T20:13:17.289Z  INFO 1 --- [Iteration3-Leader] [   scheduling-1] com.example.leader.HeartbeatService      : Follower follower1 changed status from HEALTHY to SUSPECTED
 leader-1     | 2025-11-11T20:13:17.289Z  INFO 1 --- [Iteration3-Leader] [   scheduling-1] com.example.leader.HeartbeatService      : Updated states: {follower2=UNHEALTHY, follower1=SUSPECTED}
 leader-1     | 2025-11-11T20:13:37.309Z  INFO 1 --- [Iteration3-Leader] [   scheduling-1] com.example.leader.HeartbeatService      : Follower follower1 changed status from SUSPECTED to UNHEALTHY
 leader-1     | 2025-11-11T20:13:37.310Z  INFO 1 --- [Iteration3-Leader] [   scheduling-1] com.example.leader.HeartbeatService      : Updated states: {follower2=UNHEALTHY, follower1=UNHEALTHY}
```
#### Calling replication
```shell
 curl -X POST --location "http://localhost:8080/leader/messages"     -H "Content-Type: application/json"     -d '{
 "message": {
 "value": "ADD_4",
 "deduplicationId": "dedup-4"
 },
 "writeConcern": 3
 }'
```

#### Replication result
```json
 {"type":"about:blank","title":"Service Unavailable","status":503,"detail":"503 SERVICE_UNAVAILABLE \"No quorum\"","instance":"/leader/messages"}
```

### Test 2
#### Pause one followers
```shell
docker pause iteration3-follower1-1
```

#### Calling replication
```shell
 curl -X POST --location "http://localhost:8080/leader/messages"     -H "Content-Type: application/json"     -d '{
 "message": {
 "value": "ADD_4",
 "deduplicationId": "dedup-4"
 },
 "writeConcern": 3
 }'
```

#### Replication result
```json
{"type":"about:blank","title":"Bad Request","status":400,"detail":"400 BAD_REQUEST \"Not enough health nodes to comply with writeConcern\"","instance":"/leader/messages"}
```