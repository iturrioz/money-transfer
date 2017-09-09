# Money transfer service

A simple implementation of a service that provides money transfer between different accounts.

This implementation ignores any security requirement on the API level.
The data store has been implemented in-memory to keep it simple.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

This project uses Play! which requires Java 1.8. To check that you have the latest JDK, please run:

```
java -version
```

## Running the tests

```
sbt test
```

### Checking the code coverage

First run the tests again with the coverage instrumentation:

```
sbt clean coverage test coverageReport
```

The report will be available at:

```
money-transfer/target/scala-2.12/scoverage-report/index.html
```

## Deployment

## Built With

Run the following command to make a release build:

```
sbt dist
```

The build will be available at:

```
money-transfer/target/universal/money-transfer-0.1-SNAPSHOT.zip
```

Unzip the file and run the following command from the extracted directory:

```
bin/money-transfer
```

## Authors

* **Kepa Iturrioz** - *Initial work* - [iturrioz](https://github.com/iturrioz)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

