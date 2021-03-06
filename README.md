# Shop Totem 
### Advanced Techniques and Tools for Software Development
[![Java CI with Maven on ubuntu-latest](https://github.com/FrancescoScandiffio/shop-totem/actions/workflows/maven_ubuntu.yml/badge.svg)](https://github.com/FrancescoScandiffio/shop-totem/actions/workflows/maven_ubuntu.yml)
[![Java CI with Maven on MacOS](https://github.com/FrancescoScandiffio/shop-totem/actions/workflows/maven_macos.yml/badge.svg)](https://github.com/FrancescoScandiffio/shop-totem/actions/workflows/maven_macos.yml)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=FrancescoScandiffio_shop-totem&metric=bugs)](https://sonarcloud.io/summary/new_code?id=FrancescoScandiffio_shop-totem)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=FrancescoScandiffio_shop-totem&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=FrancescoScandiffio_shop-totem)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=FrancescoScandiffio_shop-totem&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=FrancescoScandiffio_shop-totem)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=FrancescoScandiffio_shop-totem&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=FrancescoScandiffio_shop-totem)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=FrancescoScandiffio_shop-totem&metric=coverage)](https://sonarcloud.io/summary/new_code?id=FrancescoScandiffio_shop-totem)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=FrancescoScandiffio_shop-totem&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=FrancescoScandiffio_shop-totem)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=FrancescoScandiffio_shop-totem&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=FrancescoScandiffio_shop-totem)
[![Coveralls coverage](https://coveralls.io/repos/github/FrancescoScandiffio/shop-totem/badge.svg?branch=main)](https://coveralls.io/github/FrancescoScandiffio/shop-totem?branch=main)

Shop Totem is a desktop application that simulates a supermarket shopping system. The implementation replicates some real-world constraints, such as limiting the amount of products that can be bought based on the stock available in the warehouse (the database). It can also use either a relational database or a non-relational database, specifically MySQL and MongoDB, depending on the value of a command line parameter specified at startup.

## Pre-requisites
In order to run this project is required Java8 and Docker. Having Docker-compose is recommended in order to start databases easily, although it is not mandatory. The databases will try to connect to the ports:
- MySQL: 3306
- MongoDB: 27017, 27018 and 27019 (due to the replica set)

## Maven build
The full Maven build can be obtained through the command:
```bash
mvn clean verify
```
With it, all unit, integration and E2E tests with BDD will be performed. Also the required database Docker containers will be started and stopped automatically during the tests. It will also be generated the JaCoCo code coverage report. Mutation testing is disabled by default but can be executed with:
```
mvn org.pitest:pitest-maven:mutationCoverage
```
## Run in production

Before running the application, it will be necessary to run the container for the chosen database. For simplicity both databases can be launched with the use of our docker-compose. That is, by simply running `docker-compose up`.  

Alternatively only the singular containers can be launched. 
In case of MongoDB, a Replica set must be run, which is essential for executing transactions. This can be done with the following command:

```bash
docker run -d -p 27017:27017 -p 27018:27018 -p 27019:27019 candis/mongo-replica-set
```

In case of MySQL it will only be required:

```bash
docker run -d -p 3306:3306 -e MYSQL_DATABASE=" totem" -e MYSQL_ROOT_PASSWORD="" -e MYSQL_ALLOW_EMPTY_PASSWORD="yes" mysql :8.0.28
```

After that, when running the application, it is necessary to specify the database to use. This can be done by entering the parameter `--database`, with a value of choice between `mysql` (default) and `mongo`.

It is also possible to run multiple instances of the application simultaneously. However, for convenience, at the start of each new application the databases are reset and repopulated. For this reason it is advised to open all the applications at once, to avoid resetting data already inserted. In any case, if this happens, without closing the application it is possible to cancel the order. After that, the application will automatically retrieve the data from the database and be ready to use again.
