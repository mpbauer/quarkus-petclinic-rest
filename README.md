[![Quarkus](docs/images/quarkus-logo.png)](https://quarkus.io/)

# [Quarkus PetClinic Sample Application](https://github.com/mpbauer/quarkus-petclinic-rest)

This backend version of the Spring Petclinic application only provides a REST API. **There is no UI**.
The [spring-petclinic-angular project](https://github.com/spring-petclinic/spring-petclinic-angular) is a Angular
front-end application which consumes the REST API.

A modified version (specific to this project) of
the [spring-petclinic-angular project](https://github.com/spring-petclinic/spring-petclinic-angular) can be
found [HERE](https://github.com/mpbauer/spring-petclinic-angular)

---
**NOTE**

This project is based upon code from the commit reference `f97f1d44580d9950463043b451902f2e4955dea7` of the repository [spring-petclinic/spring-petclinic-rest](https://github.com/spring-petclinic/spring-petclinic-rest).

---

## Understanding the Spring Petclinic application with a few diagrams

[See the presentation of the Spring Petclinic Framework version](http://slideshare.net/AntoineRey/spring-framework-petclinic-sample-application)

A local copy of the presentation can be found [here](docs/misc/springframeworkpetclinic-presentation.pdf)

### Petclinic ER Model

![alt petclinic-ermodel](docs/diagrams/petclinic-ermodel.png)

## Running the petclinic application locally

### With Maven in `dev` mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

## Packaging and running the application with JVM

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-petclinic-rest-1.0.0-SNAPSHOT-runner.jar` file in the `/target` directory. Be aware that it’s
not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

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

You can then access petclinic here: http://localhost:8080/petclinic/

The application is now runnable using `java -jar target/quarkus-petclinic-rest-1.0.0-SNAPSHOT-runner.jar`.

## Creating a native executable

Creating a native executable requires [GraalVM](https://www.graalvm.org/). Make sure to install and configure a
compatible version of GraalVM before you try to build a native image. For more information on how to build native
executables check
out [this](https://quarkus.io/guides/building-native-image#:~:text=While%20Oracle%20GraalVM%20is%20available,install%20the%20Java%2011%20version.)
link.

### With Maven

You can create a native executable using:

```shell script
./mvnw package -Pnative
```

You can then execute your native executable with:

```shell script
./target/quarkus-petclinic-rest-1.0.0-SNAPSHOT-runner
```

**Optional:** If you don't have [GraalVM](https://www.graalvm.org/) installed, you can run the native executable build
in a container using:

```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

:exclamation: Building native images with `-Dquarkus.native.container-build=true` requires a running Docker
installation. Keep in mind that the `Docker` build will be performed in a Linux container and will not be executable on
a MacOS or Windows machine.

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

You can then access petclinic here: http://localhost:8080/petclinic/

## OpenAPI REST Documentation

The following URLs can be used to access a documentation about
the [quarkus-petclinic-rest](https://github.com/mpbauer/quarkus-petclinic-rest) application:

**OpenAPI UI**

Provides a graphical user interface with all existing REST endpoints:

```
http://localhost:8080/petclinic/q/swagger-ui/
```

**Open API Schema Document**

Provides an endpoint to download the Open API REST documentation:

```
http://localhost:8080/petclinic/q/openapi
```

## Health Checks

The `smallrye-health` dependency provides health checks out of the box. The following endpoints are provided:

- `/q/health/live` - The application is up and running (liveness probe)
- `/q/health/ready` - The application is ready to serve requests (readiness probe)
- `/q/health` - Accumulating all health check procedures in the application
- `q/health-ui` - Provides a graphical user interface for health information

Example:

```
http://localhost:8080/petclinic/q/health
```

## Metrics

The `smallrye-metrics` dependency provides metrics which can be accessed through the `/q/metrics` endpoint.

Example:

```
http://localhost:8080/petclinic/q/metrics
```

## Database configuration

The database support for this version of
the [spring-petlinic-rest](https://github.com/spring-petclinic/spring-petclinic-rest) project was significantly reduced.
As of now this project only supports [PostgreSQL](https://www.postgresql.org/)
and [H2](https://www.h2database.com/html/main.html).

In its default configuration a `PostgreSQL` database is required to run the application. For the execution of tests an
embedded `H2` is started.

For local development you may want to start a `PostgreSQL` database with `Docker`:

````
docker run --name petclinic -p 5432:5432 -e POSTGRES_PASSWORD=pass -d postgres
````

## Security configuration

A Role Based Access Control is enabled by default when running the application with the `prod` and `test` profile. When
you start the application in `dev` through `./mvnw quarkus:dev` authentication is disabled by default.

### Disable Authentication

In order to disable access control, you can turn it off by setting the following property in
the `application.properties` file:

```
petclinic.security.enable=false
```

### Authorization

This will secure all APIs and in order to access them, basic authentication is required. Apart from authentication, APIs
also require authorization. This is done via roles that a user can have. The existing roles are listed below with the
corresponding permissions:

Role         | Controller
----------   | ----------------
OWNER_ADMIN  | OwnerController<br/>PetController<br/>PetTypeController (`getAllPetTypes()` & `getPetType()`)
VET_ADMIN    | PetTypeController<br/>SpecialityController</br>VetController
ADMIN        | UserController

### Roles Based Access Control with predefined JSON Web Tokens

To make the use of this sample application as easy as possible a set of fixed JSON Web Tokens with different roles were
generated and signed with a private key that is located in `src/test/resources/privateKey.pem`. You can copy the token
and pass it via the `Authorization` header to the application.

**Example:**

```shell
curl --location --request GET 'http://localhost:8080/petclinic/api/owners' \
--header 'Content-Type: application/json' \
--header 'Accept: application/json' \
--header 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJncm91cHMiOlsiUk9MRV9PV05FUl9BRE1JTiJdLCJpYXQiOjE2MTQ4NTMxNDksImV4cCI6NDc3MDUyNjc0OSwianRpIjoiMGEwODZmZjItYjVmNC00MGQxLWEyZDUtMmM0YjA0ZTBkMDBhIiwiaXNzIjoiaHR0cHM6Ly9zcHJpbmctcGV0Y2xpbmljLmdpdGh1Yi5pby9pc3N1ZXIifQ.V0qEDupr5uOdi-re233jMEOWhP4w_yvCgWUrEapOcz-2WLe64fjvGFAXlpvdfjcslCGCSofB97CnQ8xzx0QPdWpDKN7E2b_JTYb7wFsrI71nIblw-2n7uyKFGKgSbjd4L7BNIbUP6Pcodgn1FsDZ6HPfJlEqWMf_ZEyZi9hA3qfPgpSgt9iQgW88gkyRZv7tJUwhr0ZY4qNB6ujbZtBF4Er0Wvm8VqR2_KK8PaKk2ydGnsTDmrPaXSVmTH0ZMMwMRrmM4OvT-WZdpcAzakA4adDBPC6URM_GIzKfVMZ3oVbUwBj6HOybbX8R9VSXegTfZTio1J-dF5fXlFYYILktpg'
```

> :exclamation: IMPORTANT: Never push a private key in a Git repository. In case you do, make sure do add additional
> security by encrypting security relevant data first before you push them. This application is just for showcasing
> the use of JSON Web Tokens in the Petclinic application but it should never be done this way for real applications.

#### List of valid JSON Web Tokens:

Role: `OWNER_ADMIN`

```
eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJncm91cHMiOlsiUk9MRV9PV05FUl9BRE1JTiJdLCJzdWIiOiJYMDAwMDEiLCJpYXQiOjE2MTQ4Nzg2NjgsImV4cCI6NDc3MDU1MjI2OCwianRpIjoiZDM0ZjYwM2ItNjhiYy00NTZmLThlNDUtMGY2M2FkMTIxODc0IiwiaXNzIjoiaHR0cHM6Ly9zcHJpbmctcGV0Y2xpbmljLmdpdGh1Yi5pby9pc3N1ZXIifQ.r3xIQDYMlOVqj4dxJvHAIgh7XTafxeE0vJ2SkVlZng85lH-OM-sCGlpgy1uCjUKggmx3BmvX86F0GNfDVSfntfTUM_N4Uq27_wOZQjQYPCI3FxDub2kvhf0Wz59_Ed1Ip6TgkIVJaQrdCnfMr8F8eRbXZnKUwkcndb1Z_PLIp8JPP095VGyax_ezGiM1171t5UcrD4rUpmN5LYJvN_DGpfscf71Z7hG9KUPuspEO-Tld-7_hrPQkHh6FFkOiPbIirzWtgSmDaEjnijQoBhDxco8Y_wXolmwLnIhAJeT0zYE3eXmMKbU6H59799AGrGm2kU7hHuDkQEx0OretgxOmpA
```

Role: `VET_ADMIN`

```
eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJncm91cHMiOlsiUk9MRV9WRVRfQURNSU4iXSwic3ViIjoiWDAwMDAyIiwiaWF0IjoxNjE0ODc4NjY5LCJleHAiOjQ3NzA1NTIyNjksImp0aSI6ImYzNDQxMjM0LWY1ZmYtNDdmYy1hMzgwLWVmMTNlZmU5ODBiNSIsImlzcyI6Imh0dHBzOi8vc3ByaW5nLXBldGNsaW5pYy5naXRodWIuaW8vaXNzdWVyIn0.nmn-DHn4Zkps2tqTz6hrsR94IO2Qfsbyio_EApEJ6BzReQ8wr4LBFHH391fWvYfFPXXg2_q1J8sL8RGRv76vfoR5S3BWCmrBkxkdGINS5R5r1xc7TljDyL2mX6a6MH1f8rUl_jZsieEwcpzZSB_d_aC1Uk8wCxS1DCpPWP2nLtzex_G1890V93wxoSVJpI9a9XcQhWPT-OkFU5gIw2H9GRTqFQGVYInEZv76oBUU0x52kSVqLuf-PJfFCaIEHnFdo8cxZOf4wOL6rXO0vEoV3tpzdKr1XNIgChgtW2gH16Di-r5_p9hc5zDgieYICgy8kEF4tA2yOuWtq3W2bOmutg
```

Role: `ADMIN`

```
eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJncm91cHMiOlsiUk9MRV9BRE1JTiJdLCJzdWIiOiJYMDAwMDMiLCJpYXQiOjE2MTQ4Nzg2NjksImV4cCI6NDc3MDU1MjI2OSwianRpIjoiOWM0YjhmZGEtYjViYS00YmY0LWEzNjMtNGRhNzg3YWEyY2JjIiwiaXNzIjoiaHR0cHM6Ly9zcHJpbmctcGV0Y2xpbmljLmdpdGh1Yi5pby9pc3N1ZXIifQ.pFRf_BiIJyHVWynlZIi_MHZY5KDHu09VG4Eb4GrvFopure1cZiviva-yrrY7IdmwNkf1aM991YApCiFnjl9D40c0PG5G5_734LU5uhAKe8ACBAIVG8mWtiGbBpCAdg_ghWCjIhmP17xnpyTpfHeJLqeIDDbxJ75tFnFV4Lu1R-JyhdEZjX8Ulsls2sNde4iBOl1ZILT2CuBYueL070qcrowOBnh6POnHacp8Kt2ZSWq6wAqcgIJGYYY2S7JKau2Qv8ZF_lUmyP58pUYrwkgxl95zrQypjQaYjUVAJosayutjKJwEpDfptvo_9s0kgns1R2EF7XN8NyNCQxyO9Cdgyw
```

Roles: `OWNER_ADMIN`, `VET_ADMIN`, `ADMIN`

```
eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJncm91cHMiOlsiUk9MRV9BRE1JTiIsIlJPTEVfVkVUX0FETUlOIiwiUk9MRV9PV05FUl9BRE1JTiJdLCJzdWIiOiJYMDAwMDQiLCJpYXQiOjE2MTQ4Nzg2NjksImV4cCI6NDc3MDU1MjI2OSwianRpIjoiZjUwM2FkMWItMTkyNy00ZDQ0LTlhNjAtOWU4ZThmNDM2YTA2IiwiaXNzIjoiaHR0cHM6Ly9zcHJpbmctcGV0Y2xpbmljLmdpdGh1Yi5pby9pc3N1ZXIifQ.w2-po_4b9S6Y08tNuv1dn0J0jyd84QiP2qTlWKgj3pGY9VUeXefIKRq9-MTyP-LvkDXNgCyQ6DbDRUt-A7uRdxiDiteirdvg0-7aRKDQBMWtJmEIDAVqZfz20q7ycGMmu2wOEPnDxtSyWJoHLacDIHDQPXyR2AnUHZ1weuFnlipI5OGjeFw7aqT5QXhnj8RwMpw77bqofgIWBTRLLe-VJUYrP8ARiqpoqfPUM6oiS7spSMvItu9IFKWpbzHd0aEHxjfMk6gI8E0q8FEsqgn9l60vV0_FGQNiEXorgGZmPDXSCCK29kgGesvCBk0RYBKrR7LkNEj3uAsXITZKAs9bMA
```

To see more details about the token you can decode on https://jwt.io

### Create your own JWT Tokens

If you don't want to use the already generated tokens you generate tokens with either your existing private key or by
creating a new keypair.

1) Generating Keys with OpenSSL

   It is also possible to generate a public and private key pair using the OpenSSL command line tool.

   openssl commands for generating keys
    ```shell
    openssl genrsa -out rsaPrivateKey.pem 2048
    openssl rsa -pubout -in rsaPrivateKey.pem -out publicKey.pem
    ```

   An additional step is needed for generating the private key for converting it into the PKCS#8 format.

   openssl command for converting private key
    ```shell
    openssl pkcs8 -topk8 -nocrypt -inform pem -in rsaPrivateKey.pem -outform pem -out privateKey.pem
    ```

2) Place the public key `publicKey.pem` to a point that is in the classpath like `META-INF/ressources` and configure the
   following property in `application.properties`:

    ```
    mp.jwt.verify.publickey.location=META-INF/resources/publicKey.pem 
    ```

3) Generate a JWT Token

   You can now generate your own JSON Web Token and sign it with your private key `privateKey.pem`. There are a lot of
   different options available on how to generate a JSON Web Token.

   **GeneratorTokenTest**:

   For this application a test class was written to generate new JSON Web Tokens on demand. You can generate new JSON
   Web Tokens by enabling the test class `com.mpbauer.serverless.samples.petclinic.security.GeneratorTokenTest.java`.
   The test classes will generate a new token with specific roles and print it to the console output.

For more details check out the Quarkus [Security-JWT-Quickstart](https://quarkus.io/guides/security-jwt) guide.

## GitHub Actions CI/CD configuration

This section explains the necessary setup details to build and deploy the `quarkus-petclinic-rest` application
to [Google Cloud Platform (GCP)](https://cloud.google.com/)
and [Google Kubernetes Engine](https://cloud.google.com/kubernetes-engine) where we use [Knative](https://knative.dev/)
for serverless deployments.

### Service Accounts

GitHub Actions is building the container images with [Google Cloud Build](https://cloud.google.com/build) and stores the
resulting container image in [Google Container Registry](https://cloud.google.com/container-registry). Afterwards the
image is going to be deployed to [Google Cloud Run](https://cloud.google.com/run)
and [Google Kubernetes Engine (GKE)](https://cloud.google.com/kubernetes-engine). To do this with a CI/CD tool like
[GitHub Actions](https://docs.github.com/en/actions) we need to create Service Accounts to manage and regulate access to
Google Cloud ressources.

### Prerequisites

Before you start you should have already set up a [GCP account](https://cloud.google.com/gcp) with
a [billing account](https://cloud.google.com/billing/docs/how-to/manage-billing-account) as well as a project. There are
several ways to set up a service account with GCP. You can either use the Google Cloud SDK or the Management Console in
your browser to create and configure service accounts.

### Create a Service Account with `gcloud`

1) Export these environment variables so that you can copy and paste the following commands:

    ```shell
    export PROJECT_ID=<YOUR PROJECT ID>
    export SERVICE_ACCOUNT_NAME=<ENTER A NAME FOR YOUR SERVICE ACCOUNT>
    ```

2) Log in with your Google account:

    ```shell
    gcloud auth login
    ```

3) Select the project configured via `$PROJECT_ID`:

    ```shell
    gcloud config set project $PROJECT_ID
    ```

4) Enable the necessary services:

    ```shell
    gcloud services enable cloudbuild.googleapis.com run.googleapis.com containerregistry.googleapis.com container.googleapis.com
    ```

5) Create a service account:

    ```shell
    gcloud iam service-accounts create $SERVICE_ACCOUNT_NAME \
      --description="GitHub Actions service account for Petclinic repositories" \
      --display-name="$SERVICE_ACCOUNT_NAME"
    ```

6) Give the service account Cloud Run Admin, Storage Admin, and Service Account User roles. You can’t set all of them at
   once, so you have to run separate commands:

    ```shell
    gcloud projects add-iam-policy-binding $PROJECT_ID \
      --member=serviceAccount:$SERVICE_ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com \
      --role=roles/cloudbuild.builds.editor
    
    gcloud projects add-iam-policy-binding $PROJECT_ID \
      --member=serviceAccount:$SERVICE_ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com \
      --role=roles/iam.serviceAccountUser
    
    gcloud projects add-iam-policy-binding $PROJECT_ID \
      --member=serviceAccount:$SERVICE_ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com \
      --role=roles/run.admin
     
    gcloud projects add-iam-policy-binding $PROJECT_ID \
      --member=serviceAccount:$SERVICE_ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com \
      --role=roles/viewer
      
    gcloud projects add-iam-policy-binding $PROJECT_ID \
      --member=serviceAccount:$SERVICE_ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com \
      --role=roles/storage.admin
      
    gcloud projects add-iam-policy-binding $PROJECT_ID \
      --member=serviceAccount:$SERVICE_ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com \
      --role=roles/container.admin  
    ```

7) Generate a key.json file with your credentials, so your GitHub workflow can authenticate with Google Cloud. After
   issuing the following command you can find the generated key in your current folder.

    ```shell
    gcloud iam service-accounts keys create key.json \
        --iam-account $SERVICE_ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com
    ```

   The following tutorial explains this in more
   detail: [link](https://cloud.google.com/community/tutorials/cicd-cloud-run-github-actions)

### Create a Service Account with [GCP Management Console](https://console.cloud.google.com/)

1) Open the [Google Cloud Management Console](https://console.cloud.google.com) in your browser

2) Select your project with the dropdown on the top navigation bar

   [![SETUP_GCP_SERVICE_ACCOUNT_00](docs/screenshots/setup-gcp-service-account/setup_gcp_service_account_00.png)](docs/screenshots/setup-gcp-service-account/setup_gcp_service_account_00.png)

3) Go
   to [IAM & Admin](https://console.cloud.google.com/iam-admin/) > [Service Accounts](https://console.cloud.google.com/iam-admin/serviceaccounts)
   and click `ADD`to create a new service account

4) Enter a name and a description (optional) for your new service account and click `CREATE`

   [![SETUP_GCP_SERVICE_ACCOUNT_01](docs/screenshots/setup-gcp-service-account/setup_gcp_service_account_01.png)](docs/screenshots/setup-gcp-service-account/setup_gcp_service_account_01.png)

5) Add the following roles to your service account and click `DONE`. The third step is not necessary and can be skipped.

    - `Cloud Build Editor (roles/cloudbuild.builds.editor)` - Required for Cloud Build
    - `Service Account User (roles/iam.serviceAccountUser)` - General Service Account permissions
    - `Cloud Run Admin (roles/run.admin)` - Required for Cloud Run
    - `Viewer (roles/viewer)` - Required as a workaround for successful deployments in GitHub Actions (see explanation
      below)
    - `Storage Admin (roles/storage.admin)` - Required for Container Registry
    - `Kubernetes Engine Admin (roles/container.admin)` - Required for GKE deployments

   > Explanation of the Viewer Role:
   >
   > Link: https://towardsdatascience.com/deploy-to-google-cloud-run-using-github-actions-590ecf957af0
   > Once the service account is created you will need to select the following roles. I tried a number of different ways to remove the very permissive project viewer role, but at the time of this writing this your service account will need this role or the deployment will appear to fail in Github even if it is successfully deployed to Cloud Run.

   [![SETUP_GCP_SERVICE_ACCOUNT_02](docs/screenshots/setup-gcp-service-account/setup_gcp_service_account_02.png)](docs/screenshots/setup-gcp-service-account/setup_gcp_service_account_02.png)

6) Now click on your newly created service account and click `ADD KEY` on the service account details page. This will
   create new credentials which will be later used to authenticate your service account. Select `JSON`and click `CREATE`
   to generate and download your service account credentials.

   [![SETUP_GCP_SERVICE_ACCOUNT_03](docs/screenshots/setup-gcp-service-account/setup_gcp_service_account_03.png)](docs/screenshots/setup-gcp-service-account/setup_gcp_service_account_03.png)

7) Your service account credentials have been generated and downloaded on your machine. Make sure to keep them safe!

   [![SETUP_GCP_SERVICE_ACCOUNT_04](docs/screenshots/setup-gcp-service-account/setup_gcp_service_account_04.png)](docs/screenshots/setup-gcp-service-account/setup_gcp_service_account_04.png)

### Setup Google Kubernetes Engine

1) Export these environment variables so that you can copy and paste the following commands:

    ```shell
    export PROJECT_ID=<YOUR PROJECT ID>
    export SERVICE_ACCOUNT_NAME=<ENTER A NAME FOR YOUR SERVICE ACCOUNT>
    ```

2) Log in with your Google account:

    ```shell
    gcloud auth login
    ```

3) Select the project configured via `$PROJECT_ID`:

    ```shell
    gcloud config set project $PROJECT_ID
    ```

4) Enable the necessary services (if not already enabled):

    ```shell
    gcloud services enable container.googleapis.com
    ```

5) Create GKE Cluster

    ```shell
    gcloud beta container --project "$PROJECT_ID" clusters create "petclinic-cluster" \
    --zone "europe-west3-c" \
    --no-enable-basic-auth \
    --cluster-version "1.18.12-gke.1210" \
    --release-channel "regular" \
    --machine-type "n2-standard-4" \
    --image-type "COS" \
    --disk-type "pd-standard" \
    --disk-size "100" \
    --metadata disable-legacy-endpoints=true \
    --scopes "https://www.googleapis.com/auth/devstorage.read_only","https://www.googleapis.com/auth/logging.write","https://www.googleapis.com/auth/monitoring","https://www.googleapis.com/auth/servicecontrol","https://www.googleapis.com/auth/service.management.readonly","https://www.googleapis.com/auth/trace.append" \
    --num-nodes "1" \
    --enable-stackdriver-kubernetes \
    --enable-ip-alias \
    --network "projects/$PROJECT_ID/global/networks/default" \
    --subnetwork "projects/$PROJECT_ID/regions/europe-west3/subnetworks/default" \
    --default-max-pods-per-node "110" \
    --no-enable-master-authorized-networks \
    --addons HorizontalPodAutoscaling,HttpLoadBalancing,GcePersistentDiskCsiDriver \
    --enable-autoupgrade \
    --enable-autorepair \
    --max-surge-upgrade 1 \
    --max-unavailable-upgrade 0 \
    --enable-shielded-nodes \
    --node-locations "europe-west3-c"
    ```

   After the cluster was successfuly created you should see the following output:

    ```
    NAME               LOCATION        MASTER_VERSION    MASTER_IP      MACHINE_TYPE   NODE_VERSION      NUM_NODES  STATUS
    petclinic-cluster  europe-west3-c  1.18.12-gke.1210  X.X.X.X        n2-standard-4  1.18.12-gke.1210  1          RUNNING
    ```

6) Connect to your Kubernetes Cluster

    ```shell
    gcloud container clusters get-credentials petclinic-cluster --region europe-west3 --project $PROJECT_ID
    ```

7) Create namespaces for the Petclinic application

   Create a Knative namespace for native executables on `dev` stage
    ```shell
    kubectl create namespace petclinic-native-knative-dev
    ```

   Create a Knative namespace for native images on `prod` stage
    ```shell
    kubectl create namespace petclinic-native-knative-prod
    ```

   Create a Knative namespace for JVM images on `dev` stage
    ```shell
    kubectl create namespace petclinic-jvm-knative-dev
    ```

   Create a Knative namespace for JVM images on `prod` stage
    ```shell
    kubectl create namespace petclinic-jvm-knative-prod
    ```

### Setup Knative

1) Install the service component

    ```shell
    kubectl apply --filename https://github.com/knative/serving/releases/download/v0.21.0/serving-crds.yaml
    ```

2) Install the core components of Serving

    ```shell
    kubectl apply --filename https://github.com/knative/serving/releases/download/v0.21.0/serving-core.yaml
    ```

3) Install a properly configured Istio

    ```shell
    kubectl apply --filename https://github.com/knative/net-istio/releases/download/v0.21.0/istio.yaml
    ```

4) Install the Knative Istio controller

    ```shell
    kubectl apply --filename https://github.com/knative/net-istio/releases/download/v0.21.0/net-istio.yaml
    ```

5) Configure DNS with MagicDNS (xip.io)

    ```shell
    kubectl apply --filename https://github.com/knative/serving/releases/download/v0.21.0/serving-default-domain.yaml
    ```

   You might have to wait a few minutes and retry it later if you receive a connection time out error.

6) Monitor the Knative components until all of the components show a STATUS of `Running` or `Completed:

    ```shell
    kubectl get pods --namespace knative-serving
    ```

   #### Common Knative Commands:

   Show details about all Knative Services:

    ```shell
    kubectl get ksvc --all-namespaces
    ```

   Show details about `quarkus-petclinic-rest application`

    ```shell
    kubectl get ksvc quarkus-petclinic-rest --namespace <CHOOSE NAMESPACE FROM LIST BELOW>
    ```

   **Available Namespaces:**

    - `petclinic-native-knative-dev` - Namespace for Petclinic `development` containers running as a native executable
    - `petclinic-native-knative-prod` - Namespace for Petclinic `production` containers running as a native executable
    - `petclinic-native-knative-dev` - Namespace for Petclinic `development` containers running with a Java Virtual
      Machine (JVM)
    - `petclinic-native-knative-prod` - Namespace for Petclinic `production` containers running with a Java Virtual
      Machine (JVM)

### GitHub configuration

In GitHub, you need to set up the following secrets via your repositories settings tab:

- `GCP_PROJECT_ID` - The GCP project id which was defined in `$PROJECT_ID` during the service account creation step
- `GCP_SERVICE_ACCOUNT_EMAIL` - The email from the previously created service account
- `GCP_SERVICE_ACCOUNT_CREDENTIALS` - The content from the `key.json` file that was previously created
- `GCP_DB_HOST` - The hostname or public IP address of the database server
- `GCP_DB_PORT` - The port of the database server
- `GCP_DB_DATABASE` - The database of your db server you are using for the application
- `GCP_DB_USERNAME` - The db username for your application
- `GCP_DB_PASSWORD` - The db user password for your application

In the end your secrets should look like this:

[![SETUP_GITHUB_GCP_SECRETS_00](docs/screenshots/setup-github-secrets/setup_github_gcp_secrets_00.png)](docs/screenshots/setup-github-secrets/setup_github_gcp_secrets_00.png)

## Google Cloud Run Endpoints - Development

| Application                   | URL
| ----------------------------- |:-------------:
| Quarkus Petclinic (JVM)       | https://quarkus-petclinic-rest-jvm-dev-s2xflp6dzq-ey.a.run.app
| Quarkus Petclinic (Native)    | https://quarkus-petclinic-rest-native-dev-s2xflp6dzq-ey.a.run.app

## Google Cloud Run Endpoints - Production

| Application                   | URL
| ----------------------------- |:-------------:
| Quarkus Petclinic (JVM)       | https://quarkus-petclinic-rest-jvm-prod-s2xflp6dzq-ey.a.run.app
| Quarkus Petclinic (Native)    | https://quarkus-petclinic-rest-native-prod-s2xflp6dzq-ey.a.run.app
