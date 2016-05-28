# HTTP/1 versus HTTP/2

several commands are available:

## Client

An http client

## Backend server

A backend server

## Frontend server

A front end server (that usually accesses the backend server)

## Examples

### A client accesses the front end with HTTP/1 that access the backend with HTTP/1

- java -jar target/http2-test-3.3.0-SNAPSHOT.jar http-backend --port 8081
- java -jar target/http2-test-3.3.0-SNAPSHOT.jar http-frontend --port 8080
- java -jar target/http2-test-3.3.0-SNAPSHOT.jar http-client --requests 10000 --port 8080

### A client accesses the front end with HTTP/2 that access the backend with HTTP/1

- java -jar target/http2-test-3.3.0-SNAPSHOT.jar http-backend --port 8081
- java -jar target/http2-test-3.3.0-SNAPSHOT.jar http-frontend --port 8080
- java -jar target/http2-test-3.3.0-SNAPSHOT.jar http-client --requests 10000 --port 8080 --protocol HTTP_2

### A client accesses the front end with HTTP/2 that access the backend with HTTP/2

- java -jar target/http2-test-3.3.0-SNAPSHOT.jar http-backend --port 8081
- java -jar target/http2-test-3.3.0-SNAPSHOT.jar http-frontend --port 8080 --backend-protocol HTTP_2
- java -jar target/http2-test-3.3.0-SNAPSHOT.jar http-client --requests 10000 --port 8080 --protocol HTTP_2

### A client accesses the backend with HTTP/2

- java -jar target/http2-test-3.3.0-SNAPSHOT.jar http-backend --port 8080
- java -jar target/http2-test-3.3.0-SNAPSHOT.jar http-client --requests 10000 --port 8080 --protocol HTTP_2

etc...

You can play with:

- protocols in client and frontend
- concurrency : the max concurrency in HTTP/2
- the connection pool size in the front end server
- number of requests
- backend think time



