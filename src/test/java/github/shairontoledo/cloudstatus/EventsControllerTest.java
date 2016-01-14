package github.shairontoledo.cloudstatus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import github.shairontoledo.cloudstatus.model.Event;
import github.shairontoledo.cloudstatus.model.Service;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class EventsControllerTest extends TestHttpRequest{

    private Service service;

    @Before
    public  void setUp() throws Exception {
        super.setUp();
        service = new Service();
        service.generateId();
        service.setName("Cool service");
        serviceRepository.save(service);
    }

    @Test
    public void testGetAll() throws Exception {

        IntStream.range(0,3).forEach(n ->{
            Event e = new Event();
            e.setWhen(new Date());
            e.setSeverity("warn");
            e.setName("This is a cool event");
            e.setServiceId("service:"+n);
            eventRepository.save(e);
        });

        TestRackInterface response = GET("/events");
        assertThat(response.status).isEqualTo(HttpStatus.OK);
        assertThatJson(response.body).isArray().ofLength(3);

        List<Event> list = new ObjectMapper().readValue(response.body, new TypeReference<List<Event>>(){});
        assertThat(list).hasSize(3);
        Event parsedEvent = list.get(0);
        assertThat(parsedEvent.getId()).isNotNull();
        assertThat(parsedEvent.getName()).isEqualTo("This is a cool event");
        assertThat(parsedEvent.getWhen()).isNotNull();
        assertThat(parsedEvent.getSeverity()).isEqualTo("warn");
        assertThat(parsedEvent.getServiceId()).contains("service:");

    }

    @Test
    public void testGet() throws Exception {
        Event event = newEventWithValidService();
        TestRackInterface response = GET(String.join("/","/events", event.getId()));
        assertThat(response.status).isEqualTo(HttpStatus.OK);
        assertThatJson(response.body)
                .node("id").isEqualTo(event.getId())
                .node("name").isEqualTo(event.getName())
                .node("severity").isEqualTo("downtime")
                .node("service_id").isEqualTo(service.getId());

    }

    @Test
    public void testCreateWithInvalidService() throws Exception {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("name", "event something");
        body.put("severity", "warn");
        body.put("when", "2014-07-05T04:00:25Z");
        body.put("service_id", "33333333");

        TestRackInterface response = POST("/events", body);
        assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND);
        assertThatJson(response.body)
                .node("message").isEqualTo("Service id not found");
    }

    @Test
    public void testCreateWithInvalidEventData() throws Exception {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("name", "event something");
        body.put("severity", "warn");
        body.put("service_id", service.getId());

        TestRackInterface response = POST("/events", body);
        assertThat(response.status).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThatJson(response.body)
                .node("message").isEqualTo("Field 'when' may not be null");
    }

    @Test
    public void testCreateWithValidService() throws Exception {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("name", "event something");
        body.put("severity", "warn");
        body.put("when", "2014-07-05T04:00:25Z");
        body.put("service_id", service.getId());

        TestRackInterface response = POST("/events", body);
        assertThat(response.status).isEqualTo(HttpStatus.OK);
        assertThatJson(response.body)
                .node("name").isEqualTo("event something")
                .node("service_id").isEqualTo(service.getId());
    }

    @Test
    public void testUpdateWithInvalidService() throws Exception {
        Event event = new Event();
        event.generateId();
        event.setName("Cool event");
        event.setSeverity("downtime");
        event.setWhen(new Date());
        event.setServiceId(service.getId());
        serviceRepository.addToCollection(service, event);

        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("service_id", "33333333");

        TestRackInterface response = PUT( String.join("/","/events", event.getId()), body);
        assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND);
        assertThatJson(response.body)
                .node("message").isEqualTo("Service id not found");
    }

    @Test
    public void testUpdateWithValidService() throws Exception {
        Event event = newEventWithValidService();

        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("service_id", service.getId());

        TestRackInterface response = PUT( String.join("/","/events", event.getId()), body);
        assertThat(response.status).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testUpdateWithNullFields() throws Exception {
        Event event = new Event();
        event.generateId();
        event.setName("Cool event");
        event.setSeverity(null);
        event.setWhen(new Date());
        event.setServiceId("something");
        serviceRepository.addToCollection(service, event);

        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.putNull("severity");

        TestRackInterface response = PUT( String.join("/","/events", event.getId()), body);
        assertThat(response.status).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    public void testDelete() throws Exception {
        Event event = newEventWithValidService();
        TestRackInterface response = DELETE( String.join("/","/events", event.getId()));
        assertThat(response.status).isEqualTo(HttpStatus.OK);
        assertThat(eventRepository.fetch(event.getId())).isNull();
    }

    @Test
    public void testInvalidServiceDelete() throws Exception {
        TestRackInterface response = DELETE("/events/doesnotexist");
        assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Event newEventWithValidService() {
        Event event = new Event();
        event.generateId();
        event.setName("Cool event");
        event.setSeverity("downtime");
        event.setWhen(new Date());
        event.setServiceId(service.getId());
        serviceRepository.addToCollection(service, event);
        return event;
    }
}