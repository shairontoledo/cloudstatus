package github.shairontoledo.cloudstatus;


import org.springframework.http.HttpStatus;

public class HttpRequestException extends Exception {

    private String message;
    private HttpStatus httpStatus;

    public HttpRequestException(String message, HttpStatus httpStatus){

        this.message = message;
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }


}
