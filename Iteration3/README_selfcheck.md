# Self-check test

```text
Start M + S1
send (Msg1, W=1) - Ok
send (Msg2, W=2) - Ok
send (Msg3, W=3) - Wait
send (Msg4, W=1) - Ok
Start S2
Check messages on S2 - [Msg1, Msg2, Msg3, Msg4]
```

### Calling with writeConcern = 1
#### Calling replication
```shell
   curl -X POST --location "http://localhost:8080/leader/messages"     -H "Content-Type: application/json"     -d '{
   "message": {
   "value": "ADD_1",
   "deduplicationId": "dedup-2"
   },
   "writeConcern": 1
   }'
```

#### Logs
```text
leader-1     | 2025-11-10T16:18:56.232Z  INFO 1 --- [Iteration3-Leader] [nio-8080-exec-1] com.example.leader.LeaderController      :
leader-1     | 2025-11-10T16:18:56.233Z  INFO 1 --- [Iteration3-Leader] [nio-8080-exec-1] c.e.leader.LeaderReplicationService      : Leader saving is successful. Message[value=ADD_1, deduplicationId=dedup-2]
leader-1     | 2025-11-10T16:18:56.241Z  INFO 1 --- [Iteration3-Leader] [nio-8080-exec-1] c.e.leader.LeaderReplicationService      : Leader execution is finished. Waited for 0 followers.
follower1-1  | 2025-11-10T16:18:56.580Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-1] c.e.follower.FollowerReplicationService  : Follower replication is started. msgId=dedup-2, seq=1
follower2-1  | 2025-11-10T16:18:56.596Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-1] c.e.follower.FollowerReplicationService  : Follower replication is started. msgId=dedup-2, seq=1
follower2-1  | 2025-11-10T16:18:57.598Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-1] c.e.follower.FollowerReplicationService  : Follower saving msgId=[dedup-2] seq=[1] is successful.
follower2-1  | 2025-11-10T16:18:57.598Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-1] c.e.follower.FollowerReplicationService  : Follower replication is successful. msgId=dedup-2, seq=1
leader-1     | 2025-11-10T16:18:57.648Z  INFO 1 --- [Iteration3-Leader] [     virtual-53] c.example.leader.FollowerClientService   : follower2 Replication result: ReplicationResult[status=ASK]
follower1-1  | 2025-11-10T16:18:58.582Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-1] c.e.follower.FollowerReplicationService  : Follower saving msgId=[dedup-2] seq=[1] is successful.
follower1-1  | 2025-11-10T16:18:58.582Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-1] c.e.follower.FollowerReplicationService  : Follower replication is successful. msgId=dedup-2, seq=1
leader-1     | 2025-11-10T16:18:58.700Z ERROR 1 --- [Iteration3-Leader] [     virtual-51] c.example.leader.FollowerClientService   : Error in calling follower follower1. Retry 0. Error 500 : "{"type":"about:blank","title":"Internal Server Error","status":500,"instance":"/follower/messages"}"
follower1-1  | 2025-11-10T16:19:01.706Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-2] c.e.follower.FollowerReplicationService  : Follower replication is started. msgId=dedup-2, seq=1
follower1-1  | 2025-11-10T16:19:01.707Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-2] c.e.follower.FollowerReplicationService  : Message dedup-2 already replicated.
leader-1     | 2025-11-10T16:19:01.713Z  INFO 1 --- [Iteration3-Leader] [     virtual-51] c.example.leader.FollowerClientService   : follower1 Replication result: ReplicationResult[status=ASK]
```

#### Description
Leader finished execution before waiting for followers.

### Calling with writeConcern = 2
#### Calling replication
```shell
   curl -X POST --location "http://localhost:8080/leader/messages"     -H "Content-Type: application/json"     -d '{
   "message": {
   "value": "ADD_2",
   "deduplicationId": "dedup-3"
   },
   "writeConcern": 2
   }'
```
#### Logs
```text
leader-1     | 2025-11-10T16:20:05.367Z  INFO 1 --- [Iteration3-Leader] [nio-8080-exec-2] com.example.leader.LeaderController      :
leader-1     | 2025-11-10T16:20:05.368Z  INFO 1 --- [Iteration3-Leader] [nio-8080-exec-2] c.e.leader.LeaderReplicationService      : Leader saving is successful. Message[value=ADD_2, deduplicationId=dedup-3]
follower1-1  | 2025-11-10T16:20:05.376Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-4] c.e.follower.FollowerReplicationService  : Follower replication is started. msgId=dedup-3, seq=2
follower2-1  | 2025-11-10T16:20:05.375Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-3] c.e.follower.FollowerReplicationService  : Follower replication is started. msgId=dedup-3, seq=2
follower2-1  | 2025-11-10T16:20:06.376Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-3] c.e.follower.FollowerReplicationService  : Follower saving msgId=[dedup-3] seq=[2] is successful.
follower2-1  | 2025-11-10T16:20:06.377Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-3] c.e.follower.FollowerReplicationService  : Follower replication is successful. msgId=dedup-3, seq=2
leader-1     | 2025-11-10T16:20:06.416Z ERROR 1 --- [Iteration3-Leader] [     virtual-64] c.example.leader.FollowerClientService   : Error in calling follower follower2. Retry 0. Error 500 : "{"type":"about:blank","title":"Internal Server Error","status":500,"instance":"/follower/messages"}"
follower1-1  | 2025-11-10T16:20:07.376Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-4] c.e.follower.FollowerReplicationService  : Follower saving msgId=[dedup-3] seq=[2] is successful.
follower1-1  | 2025-11-10T16:20:07.377Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-4] c.e.follower.FollowerReplicationService  : Follower replication is successful. msgId=dedup-3, seq=2
leader-1     | 2025-11-10T16:20:07.380Z  INFO 1 --- [Iteration3-Leader] [     virtual-63] c.example.leader.FollowerClientService   : follower1 Replication result: ReplicationResult[status=ASK]
leader-1     | 2025-11-10T16:20:07.381Z  INFO 1 --- [Iteration3-Leader] [nio-8080-exec-2] c.e.leader.LeaderReplicationService      : Leader execution is finished. Waited for 1 followers.
follower2-1  | 2025-11-10T16:20:09.423Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-4] c.e.follower.FollowerReplicationService  : Follower replication is started. msgId=dedup-3, seq=2
follower2-1  | 2025-11-10T16:20:09.423Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-4] c.e.follower.FollowerReplicationService  : Message dedup-3 already replicated.
leader-1     | 2025-11-10T16:20:09.426Z  INFO 1 --- [Iteration3-Leader] [     virtual-64] c.example.leader.FollowerClientService   : follower2 Replication result: ReplicationResult[status=ASK]
```

#### Description
Leader finished execution after waiting for a single follower.

### Calling with writeConcern = 3
#### Pausing follower2
```shell
docker pause iteration3-follower2-1
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

#### Unpausing follower2
```shell
docker unpause iteration3-follower2-1
```

#### Logs
```text
leader-1     | 2025-11-10T16:46:18.332Z  INFO 1 --- [Iteration3-Leader] [nio-8080-exec-1] com.example.leader.LeaderController      :
leader-1     | 2025-11-10T16:46:18.333Z  INFO 1 --- [Iteration3-Leader] [nio-8080-exec-1] c.e.leader.LeaderReplicationService      : Leader saving is successful. Message[value=ADD_4, deduplicationId=dedup-4]
follower1-1  | 2025-11-10T16:46:18.650Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-1] c.e.follower.FollowerReplicationService  : Follower replication is started. msgId=dedup-4, seq=1
follower1-1  | 2025-11-10T16:46:18.650Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-1] c.e.follower.FollowerReplicationService  : Follower saving msgId=[dedup-4] seq=[1] is successful.
follower1-1  | 2025-11-10T16:46:18.650Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-1] c.e.follower.FollowerReplicationService  : Follower replication is successful. msgId=dedup-4, seq=1
leader-1     | 2025-11-10T16:46:20.711Z  INFO 1 --- [Iteration3-Leader] [     virtual-51] c.example.leader.FollowerClientService   : follower1 Replication result: ReplicationResult[status=ASK]
leader-1     | 2025-11-10T16:46:28.461Z ERROR 1 --- [Iteration3-Leader] [     virtual-53] c.example.leader.FollowerClientService   : Error in calling follower follower2. Retry 0. Error I/O error on POST request for "http://follower2:8080/follower/messages": Read timed out
leader-1     | 2025-11-10T16:46:41.466Z ERROR 1 --- [Iteration3-Leader] [     virtual-53] c.example.leader.FollowerClientService   : Error in calling follower follower2. Retry 1. Error I/O error on POST request for "http://follower2:8080/follower/messages": Read timed out
follower2-1  | 2025-11-10T16:46:50.066Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-3] c.e.follower.FollowerReplicationService  : Follower replication is started. msgId=dedup-4, seq=1
follower2-1  | 2025-11-10T16:46:50.066Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-1] c.e.follower.FollowerReplicationService  : Follower replication is started. msgId=dedup-4, seq=1
follower2-1  | 2025-11-10T16:46:50.067Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-2] c.e.follower.FollowerReplicationService  : Follower replication is started. msgId=dedup-4, seq=1
follower2-1  | 2025-11-10T16:46:50.067Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-3] c.e.follower.FollowerReplicationService  : Follower saving msgId=[dedup-4] seq=[1] is successful.
follower2-1  | 2025-11-10T16:46:50.067Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-3] c.e.follower.FollowerReplicationService  : Follower replication is successful. msgId=dedup-4, seq=1
follower2-1  | 2025-11-10T16:46:50.067Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-1] c.e.follower.FollowerReplicationService  : Message dedup-4 already replicated.
follower2-1  | 2025-11-10T16:46:50.067Z  INFO 1 --- [Iteration3-Follower] [nio-8080-exec-2] c.e.follower.FollowerReplicationService  : Message dedup-4 already replicated.
leader-1     | 2025-11-10T16:46:50.113Z  INFO 1 --- [Iteration3-Leader] [     virtual-53] c.example.leader.FollowerClientService   : follower2 Replication result: ReplicationResult[status=ASK]
leader-1     | 2025-11-10T16:46:50.114Z  INFO 1 --- [Iteration3-Leader] [nio-8080-exec-1] c.e.leader.LeaderReplicationService      : Leader execution is finished. Waited for 2 followers.
```

#### Description
Leader finished execution after waiting for two followers with a couple of retries on follower2.

### Calling follower2 to get a list of messages
```shell
curl -X GET --location "http://localhost:8082/follower/messages" \
    -H "Content-Type: application/json"
```

Response:
```json
[
    {
        "value": "ADD_1",
        "deduplicationId": "dedup-2"
    },
    {
        "value": "ADD_2",
        "deduplicationId": "dedup-3"
    },
    {
        "value": "ADD_4",
        "deduplicationId": "dedup-4"
    }
]
```