# HMAC Auth with WebClient Demo - POST example
This project accompanies a blog found at https://andrew-flower.com/blog/Custom-HMAC-Auth-with-Spring-WebClient

It is an example of how to do HMAC-based auth using Spring's WebClient.

Specifically this example shows how to do POST requests (where the body needs to be signed too) 
by creating a wrapper Encoder and HttpConnector.

## Running the Application
Navigate to http://requestbin.net/ and create a bin for testing.
Take note of the unique path that comes after `r/`

The just run the application and pass in the unique path. (eg. in the case of `http://requestbin.net/r/xyxyxy`)

    ./gradlew bootRun --args /xyxyxy
