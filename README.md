# btc-wallet

BTCWallet API is created to allow storing BTC transaction and fetching the transaction histories by a specific period of time.

Cassandra DB is selected for its high availability and higher speed in write performance compared to MySQL.
It is also horizontally scalable as Cassandra makes it easy to increase data it can maintain on demand by adding number of nodes.

Kafka messaging is integrated to the write operation. Kafka producer will produce transaction message received from API call 
and kafka consumer will consume the message and store to DB. With this messaging mechanism, the server will be able to handle
a heavy volume of write operations. Two or more Kafka clusters can be added to allow replication and ensure the redundancy.
See `application.conf` for Kafka settings which will allow parallel processing, replication, etc.

To increase performance of read operation, cache (Scaffeine) is being used. During the get operation, it will fetch from cached value
which are cached by search period or build cache when not exist. More optimizations such as tweaking the cache key
or leveraging in-memory cache can be added.

In addition to the above, to prevent loss of request/data during deployment, the server should be in multiple data center in different locations and
deployments can be done in sequence. Logging and reporting can be added for traffic monitoring.

## Development

Before running the project, run docker compose to run Cassandra DB, Kafka, and Zookeeper locally.
```
docker-compose up -d
```

Compile & Run project locally:
```
sbt compile
sbt run
```
Run tests: `sbt test`

## Save Record & Get Transaction Histories

Postman: https://www.getpostman.com/collections/1320449d046fbd59e119

### Save Record

endpoint: `/wallet/save`\
Example request
```
{
    "datetime": "2022-10-05T14:45:11+07:00",
    "amount": 13.322
}
```

### Get Transaction Histories

endpoint: `/wallet/get`\
Example request
```
{
    "startDateTime": "2022-10-05T14:35:05+07:00",
    "endDateTime": "2022-10-05T15:58:05+07:00"
}
```