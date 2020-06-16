# doorkeeper
Application for managing gradual returning of employees after the COVID-19 home office period

## API
The REST endpoints require a valid JWT token in `X-Token` header to identify the user.
Example tokens (valid until 2021) can be found in [json test data file](https://github.com/kmozsi/doorkeeper/blob/master/src/test/resources/test_tokens.json).

A valid JWT token's payload must contain:
- **userId:** Unique identifier of the user
- **roles:** Array of authorization roles (available roles: EMPLOYEE, HR)
- **exp:** expiration of the token

The secret key for generating a valid token can be found in [application.yaml](https://github.com/kmozsi/doorkeeper/blob/master/src/main/resources/application.yaml)

## Running the application
### Run with maven
Run the following commands in the project's root directory:
```
mvn spring-boot:run
```

### Run with docker
Run the following commands in the project's root directory:
```
mvn clean install -P docker
docker run -p 8080:8080 -t com.karanteam/doorkeeper:1.0.0
```

## Testing the application
### Unit tests
There are several unit tests that can be checked with maven:
```
mvn test
```

### Postman tests
Some test cases are implemented with postman. To use it, import [postman collection](https://github.com/kmozsi/doorkeeper/blob/master/src/test/resources/Doorkeper.postman_collection.json) and use the [data file](https://github.com/kmozsi/doorkeeper/blob/master/src/test/resources/test_tokens.json) when running. 
The data file contains valid json tokens with the user's identifier.

## Tech stack
### OpenApi code generation
-  OpenApi generator Java options:
https://openapi-generator.tech/docs/generators/java/

- OpenApi configuration:
https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-maven-plugin

### H2 Database
Only for development.
H2 console is available at http://localhost:8080/h2-console/

## What to do next
- Features
  - **Delete registration:** Users could be able to cancel booking
  - **Date handling:** Users could be able to book for another day
  - **Lunch time:** Users could leave the office for a limited time, but still have an active booking. This could allow the user to go for a quick lunch :blush:
- Technical
  - Introduce persistence and replace H2 database
  
