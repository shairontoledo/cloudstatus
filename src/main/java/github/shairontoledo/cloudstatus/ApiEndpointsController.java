package github.shairontoledo.cloudstatus;


import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@RestController
public class ApiEndpointsController extends BaseController{

    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public ApiEndpointsController(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @RequestMapping(value="/api_endpoints", method= RequestMethod.GET)
    public ArrayNode endpoints() {

        ArrayNode body = JsonNodeFactory.instance.arrayNode();
        this.handlerMapping.getHandlerMethods().keySet().forEach(k ->{

            String method = k.getMethodsCondition().toString().replaceAll("\\[|\\]","");
            body.add(method +" "+k.getPatternsCondition().toString().replaceAll("\\[|\\]",""));

        });
        return body;
    }
}
