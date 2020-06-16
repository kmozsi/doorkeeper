# doorkeeper
Application for managing gradual returning of employees after the COVID-19 home office period

### OpenApi code generation
-  OpenApi generator Java options:
https://openapi-generator.tech/docs/generators/java/

- OpenApi configuration:
https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-maven-plugin

### H2 Database
H2 console is available at http://localhost:8080/h2-console/

### Postman tests
Some test cases are implemented with postman. To use it, import [postman collection](https://github.com/kmozsi/doorkeeper/blob/master/src/test/resources/Doorkeper.postman_collection.json) and use the [data file](https://github.com/kmozsi/doorkeeper/blob/master/src/test/resources/test_tokens.json) when running. 
The data file contains valid json tokens with the user's identifier.

### Run with docker
Run the following commands in the project's root directory:
```
mvn clean install -P docker
docker run -p 8080:8080 -t com.karanteam/doorkeeper:1.0.0
```
