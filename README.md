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


## Maven build
The full Maven build can be obtained through the command:
```bash
mvn clean verify
```
Mutation testing is disabled by default. To
With it, all unit, integration and E2E tests with BDD will be performed. Also the required database Docker containers will be started and stopped automatically during the tests. It will also be generated the JaCoCo code coverage report. 

## Run in production

When running the application, it is necessary to specify the database of choice. This can be done by entering the parameter `--database`, with a value of choice between `mysql` (default) and `mongo`.

However, before running the application it will be necessary to run the container for the chosen database. In the case of MongoDB, a Replica set must be launched, which is essential for running transactions. This can be done with the following command:

```bash
docker run -d -p 27017:27017 -p 27018:27018 -p 27019:27019 candis/mongo-replica-set
```

In case of MySQL it will only be necessary:

```bash
docker run -d -p 3306:3306 -e MYSQL_DATABASE=" totem" -e MYSQL_ROOT_PASSWORD="" -e MYSQL_ALLOW_EMPTY_PASSWORD="yes" mysql :8.0.28
```

Alternatively both containers can be launched with the `docker-compose up` command, using our configuration. 

## Startup exceptions

When running the tests, the following [exception](https://jira.mongodb.org/browse/JAVA-2091) may occur:
```
Exception: Interrupted acquiring a permit to retrieve an item from the pool
```
This can be thrown when Mongo requests to close the connection, but the thread has already been interrupted.

We also noticed, in the CI, a warning regarding `An illegal reflective access operation`, that seems to be due to [the use of AssertJ Swing with Java11](https://github.com/assertj/assertj-swing/issues/226). And in particular by a reflective access done by `org.assertj.swing.dependency.fest_reflect`.
