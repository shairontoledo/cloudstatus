package github.shairontoledo.cloudstatus;


import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import github.shairontoledo.cloudstatus.model.Event;
import github.shairontoledo.cloudstatus.model.Service;
import github.shairontoledo.cloudstatus.persistence.DataObjectRepository;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


public class TestHttpRequest{

    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    @Autowired
    WebApplicationContext webApplicationContext;
    @Autowired
    DataObjectRepository<Event> eventRepository;
    @Autowired
    DataObjectRepository<Service> serviceRepository;


    private HttpMessageConverter mappingJackson2HttpMessageConverter;


    MockMvc mockMvc;


    @Before
    public  void setUp() throws Exception {
        if (this.mockMvc == null)
            this.mockMvc = webAppContextSetup(this.webApplicationContext).build();
        eventRepository.clear();
        serviceRepository.clear();
    }

    public MockMvc getMockMvc() {
        return mockMvc;
    }

    public TestRackInterface GET(String resource) throws Exception {
        return withoutDataRequest(resource, get(resource));
    }

    public TestRackInterface DELETE(String resource) throws Exception {
        return withoutDataRequest(resource, delete(resource));
    }

    public TestRackInterface POST(String resource, ContainerNode<ObjectNode> body) throws Exception {
        return withDataRequest(resource, body, post(resource));
    }
    public TestRackInterface PUT(String resource, ContainerNode<ObjectNode> body) throws Exception {
        return withDataRequest(resource, body, put(resource));
    }

    private TestRackInterface withDataRequest(String resource, ContainerNode<ObjectNode> body, MockHttpServletRequestBuilder requestBuilder) throws Exception {
        MvcResult result = getMockMvc().perform(requestBuilder
                .content(body.toString())
                .contentType(contentType))
                .andReturn();

        return toSimpleResponse(result);
    }

    private TestRackInterface withoutDataRequest(String resource,  MockHttpServletRequestBuilder requestBuilder) throws Exception {
        MvcResult result = getMockMvc().perform(requestBuilder
                .contentType(contentType))
                .andReturn();

        return toSimpleResponse(result);
    }

    private TestRackInterface toSimpleResponse(MvcResult result) {
        MockHttpServletResponse resp = result.getResponse();

        Map<String,String> headers = new HashMap(resp.getHeaderNames().size());
        for (String header: resp.getHeaderNames() ){
            headers.put(header,resp.getHeader(header));
        }
        return new TestRackInterface(new String(resp.getContentAsByteArray()), HttpStatus.valueOf(resp.getStatus()), headers);
    }

}
