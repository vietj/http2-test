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

- java -jar target/http2-test-3.3.0-SNAPSHOT.jar http-backend --delay 20
- java -jar target/http2-test-3.3.0-SNAPSHOT.jar http-frontend http://backend:8080
- java -jar target/http2-test-3.3.0-SNAPSHOT.jar http-client --requests 10000 http://frontend:8080

### A client accesses the front end with HTTP/2 that access the backend with HTTP/1

- java -jar target/http2-test-3.3.0-SNAPSHOT.jar http-backend --delay 20
- java -jar target/http2-test-3.3.0-SNAPSHOT.jar http-frontend http://backend:8080
- java -jar target/http2-test-3.3.0-SNAPSHOT.jar http-client --requests 10000 --protocol HTTP_2 http://frontend:8080

### A client accesses the front end with HTTP/2 that access the backend with HTTP/2

- java -jar target/http2-test-3.3.0-SNAPSHOT.jar http-backend --delay 20
- java -jar target/http2-test-3.3.0-SNAPSHOT.jar http-frontend --protocol HTTP_2 HTTP_2 http://backend:8080
- java -jar target/http2-test-3.3.0-SNAPSHOT.jar http-client --requests 10000 --protocol HTTP_2 http://frontend:8080

### A client accesses the backend with HTTP/2

- java -jar target/http2-test-3.3.0-SNAPSHOT.jar http-backend --delay 20
- java -jar target/http2-test-3.3.0-SNAPSHOT.jar http-client --requests 10000 --protocol HTTP_2 http://backend:8080

etc...

You can play with:

- protocols in client and frontend
- concurrency : the max concurrency in HTTP/2
- the connection pool size in the front end server
- number of requests
- backend think time



