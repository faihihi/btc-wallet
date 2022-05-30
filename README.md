# btc-wallet

BTCWallet API is created to allow storing BTC transaction and fetching the transaction histories by a specific period of time.

Cassandra DB is selected for its high availability and higher speed in write performance compared to MySQL.
It is also horizontally scalable as Cassandra makes it easy to increase data it can maintain on demand by adding number of nodes.

## Development

Before running the project, run Cassandra locally on port `9042`.\
You can also change the host and port in the `application.conf`

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