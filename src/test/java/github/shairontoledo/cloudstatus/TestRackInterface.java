package github.shairontoledo.cloudstatus;

import org.springframework.http.HttpStatus;

import java.util.Map;

class TestRackInterface {
    public String body;
    public HttpStatus status;
    public Map<String, String> headers;

    public TestRackInterface(String body, HttpStatus status, Map<String, String> headers) {
        this.body = body;
        this.status = status;
        this.headers = headers;
    }
}

