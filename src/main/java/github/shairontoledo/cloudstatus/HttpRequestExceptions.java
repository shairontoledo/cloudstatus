package github.shairontoledo.cloudstatus;


import org.springframework.http.HttpStatus;

class HttpBadRequestException extends HttpRequestException {
    public HttpBadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

class HttpNotFoundException extends HttpRequestException {
    public HttpNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
