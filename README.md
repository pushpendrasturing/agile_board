# AgileBoard (H2, Java 17)

- Java 17, Spring Boot 3.3.x
- In-memory H2 database (no external DB). H2 console at `/h2-console`.

## Run locally
```bash
mvn spring-boot:run
```

## Build JAR
```bash
mvn package
java -jar target/agileboard-0.0.1-SNAPSHOT.jar
```

## Docker
```bash
docker build -t agileboard-h2 .
docker run -p 8080:8080 agileboard-h2
```

## Quick API try
1) Register
```bash
curl -s -X POST localhost:8080/api/auth/register -H 'Content-Type: application/json' -d '{"username":"u1","email":"u1@x.com","password":"p"}'
```
2) Login (copy token):
```bash
curl -s -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' -d '{"username":"u1","password":"p"}'
```
3) Create project
```bash
curl -s -X POST localhost:8080/api/projects -H "Authorization: Bearer <TOKEN>" -H 'Content-Type: application/json' -d '{"key":"PRJ","name":"Project 1"}'
```
