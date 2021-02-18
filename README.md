[![Quarkus](https://design.jboss.org/quarkus/logo/final/PNG/quarkus_logo_horizontal_rgb_1280px_default.png)](https://quarkus.io/)

# Quarkus PetClinic Sample Application

This backend version of the Spring Petclinic application only provides a REST API. **There is no UI**.
The [spring-petclinic-angular project](https://github.com/spring-petclinic/spring-petclinic-angular) is a Angular front-end application which consumes the REST API.

A modified version (specific to this project) of the [spring-petclinic-angular project](https://github.com/spring-petclinic/spring-petclinic-angular) can be found [HERE](https://github.com/mpbauer/spring-petclinic-angular)

## Understanding the Spring Petclinic application with a few diagrams

[See the presentation of the Spring Petclinic Framework version](http://slideshare.net/AntoineRey/spring-framework-petclinic-sample-application)

A local copy of the presentation can be found [here](docs/misc/springframeworkpetclinic-presentation.pdf)

### Petclinic ER Model

![alt petclinic-ermodel](docs/images/petclinic-ermodel.png)


## Running the petclinic application locally

### With Maven in `dev` mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

## Packaging and running the application with JVM

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-petclinic-rest-1.0.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

:bulb: If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

### With Docker

Before building the container image run:
```shell script
./mvnw package
```

Then, build the image with:
```shell script
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/quarkus-petclinic-rest-jvm .
```

Then run the container using:
```shell script
docker run -i --rm -p 8080:8080 quarkus/quarkus-petclinic-rest-jvm
```

You can then access petclinic here: http://localhost:9966/petclinic/

The application is now runnable using `java -jar target/quarkus-petclinic-rest-1.0.0-SNAPSHOT-runner.jar`.

## Creating a native executable

Creating a native executable requires [GraalVM](https://www.graalvm.org/). Make sure to install and configure a compatible version of GraalVM before you try to build a native image.
For more information on how to build native executables check out [this](https://quarkus.io/guides/building-native-image#:~:text=While%20Oracle%20GraalVM%20is%20available,install%20the%20Java%2011%20version.) link.

### With Maven

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

You can then execute your native executable with:
```shell script
./target/quarkus-petclinic-rest-1.0.0-SNAPSHOT-runner
```

**Optional:** If you don't have [GraalVM](https://www.graalvm.org/) installed, you can run the native executable build in a container using: 

```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

:exclamation: Building native images with `-Dquarkus.native.container-build=true` requires a running Docker installation. Keep in mind that the `Docker` build
will be performed in a Linux container and will not be executable on a MacOS or Windows machine. 

### With Docker

Before building the container image run:
```shell script
./mvnw package -Pnative
```

Then, build the image with:
```shell script
docker build -f src/main/docker/Dockerfile.native -t quarkus/quarkus-petclinic-rest-native .
```

Then run the container using:
```shell script
docker run -i --rm -p 8080:8080 quarkus/quarkus-petclinic-rest-native
```

You can then access petclinic here: http://localhost:9966/petclinic/


## Database configuration

The database support for this version of the [spring-petlinic-rest](https://github.com/spring-petclinic/spring-petclinic-rest) project was significantly reduced. As of now this project only supports [PostgreSQL](https://www.postgresql.org/) and [H2](https://www.h2database.com/html/main.html).

In its default configuration a `PostgreSQL` database is required to run the application.
For the execution of tests an embedded `H2` is started.


For local development you may want to start a `PostgreSQL` database with `Docker`:

````
docker run --name petclinic -p 5432:5432 -e POSTGRES_PASSWORD=pass -d postgres
````

## Security configuration

In its default configuration, Petclinic doesn't have authentication and authorization enabled

> :construction: This section is currently under heavy construction. At the moment JWT based authentication is not implemented yet

### Enable Authentication

In order to use the JWT based authentication functionality, you can turn it on by setting the following property 
in the `application.properties` file:
```properties
petclinic.security.enable=false
```

### Authorization
This will secure all APIs and in order to access them, basic authentication is required.
Apart from authentication, APIs also require authorization. This is done via roles that a user can have.
The existing roles are listed below with the corresponding permissions:


Role         | Controller
----------   | ----------------
OWNER_ADMIN  | OwnerController<br/>PetController<br/>PetTypeController (`getAllPetTypes()` & `getPetType()`)
VET_ADMIN    | PetTypeController<br/>SpecialityController</br>VetController
ADMIN        | UserController

