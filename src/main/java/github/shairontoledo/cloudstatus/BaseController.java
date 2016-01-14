package github.shairontoledo.cloudstatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.catalina.connector.RequestFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@CrossOrigin
@ControllerAdvice
public class BaseController implements Filter{
    @Autowired
    Validator validator;

    @Autowired
    TokenManager tokenManager;

    public void validate(Object target, BindingResult result) throws HttpRequestException{
        validator.validate(target, result);
        if (result.hasErrors()){
            FieldError fe = result.getFieldError();
            throw new HttpRequestException(
                    String.format("Field '%s' %s", fe.getField(), fe.getDefaultMessage()), HttpStatus.BAD_REQUEST);
        }
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(HttpNotFoundException.class)
    @ResponseBody
    HashMap<String,String> handleNotFoundRequest(HttpServletResponse response, HttpNotFoundException ex){
        return errorMessage(response, ex);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpRequestException.class)
    @ResponseBody
    HashMap<String,String> handleBadRequest(HttpServletResponse response, HttpRequestException ex){
        return errorMessage(response, ex);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseBody
    HashMap<String,String> handleUnauthorizedRequest(HttpServletResponse response, HttpRequestException ex){
        return errorMessage(response, ex);
    }

    private HashMap<String, String> errorMessage(HttpServletResponse response, HttpRequestException ex) {
        HashMap<String,String> hash = new HashMap(2);
        hash.put("message", ex.getMessage());
        response.setStatus(ex.getHttpStatus().value());
        return hash;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }
    List<String> REQUIRE_AUTHORIZATION_METHODS = Arrays.asList(new String[]{"POST", "DELETE", "PUT"});
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String method = ((RequestFacade) request).getMethod();
        if (REQUIRE_AUTHORIZATION_METHODS.contains(method)){
            String token = null;
            String authorizationHeader = ((RequestFacade) request).getHeader("Authorization");
            if (authorizationHeader != null){
                token = authorizationHeader.replaceAll("Token\\s+", "").trim();
            }else{
                token = request.getParameter("token");
            }
            if (token == null || token.equals("") || tokenManager.fromToken(token) == null){
                unauthorizedResponse(response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private void unauthorizedResponse(ServletResponse response) throws IOException {
        HttpServletResponse resp = (HttpServletResponse) response;
        HashMap<String, String> errorMessage = errorMessage(resp,
                new HttpRequestException("Access to the resource is not authorized", HttpStatus.UNAUTHORIZED));

        resp.setStatus(401);
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorMessage));
        response.getWriter().close();
    }

    @Override
    public void destroy() {

    }

    private class UnauthorizedException extends ServletException {
        public UnauthorizedException(){
            super("Authorization for this resource not found");
        }
    }
}
