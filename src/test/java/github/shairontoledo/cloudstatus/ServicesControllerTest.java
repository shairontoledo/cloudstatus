package github.shairontoledo.cloudstatus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import github.shairontoledo.cloudstatus.model.Event;
import github.shairontoledo.cloudstatus.model.Service;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@ContextConfiguration(classes = {Application.class})
@WebAppConfiguration
public class ServicesControllerTest extends TestHttpRequest{

    @Test
    public void testGetAll() throws Exception {

        IntStream.range(0,3).forEach(n ->{
            newService("Cool service");
        });

        TestRackInterface response = GET("/services");
        assertThat(response.status).isEqualTo(HttpStatus.OK);
        assertThatJson(response.body).isArray().ofLength(3);

        List<Service> list = new ObjectMapper().readValue(response.body, new TypeReference<List<Service>>(){});
        assertThat(list).hasSize(3);
        Service parsedService = list.get(0);
        assertThat(parsedService.getId()).isNotNull();
        assertThat(parsedService.getName()).isEqualTo("Cool service");
    }

    @Test
    public void testGet() throws Exception {
        Service service = newService("Message in process");
        TestRackInterface response = GET(String.join("/","/services", service.getId()));
        assertThat(response.status).isEqualTo(HttpStatus.OK);
        assertThatJson(response.body)
                .node("id").isEqualTo(service.getId())
                .node("name").isEqualTo(service.getName());
    }

    @Test
    public void testCreateWithoutName() throws Exception {
        ObjectNode body = JsonNodeFactory.instance.objectNode();

        TestRackInterface response = POST("/services", body);
        assertThat(response.status).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThatJson(response.body)
                .node("message").isEqualTo("Field 'name' may not be null");
    }

    @Test
    public void testCreateAService() throws Exception {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("name", "Download sub-system");

        TestRackInterface response = POST("/services", body);
        assertThat(response.status).isEqualTo(HttpStatus.OK);
        assertThatJson(response.body)
                .node("name").isEqualTo("Download sub-system")
                .node("id").isString();
    }

    @Test
    public void testUpdateWithoutName() throws Exception {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        Service service = newService("Replication system");
        TestRackInterface response = PUT(String.join("/","/services", service.getId()), body);
        assertThat(response.status).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThatJson(response.body)
                .node("message").isEqualTo("Field 'name' may not be null");
    }

    @Test
    public void testUpdateInvalidId() throws Exception {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        Service service = newService("Replication system");
        TestRackInterface response = PUT("/services/invalidid", body);
        assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND);
        assertThatJson(response.body)
                .node("message").isEqualTo("Service id not found");
    }

    @Test
    public void testUpdate() throws Exception {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("name","Replication service updated");
        Service service = newService("Replication service");
        TestRackInterface response = PUT(String.join("/","/services", service.getId()), body);
        assertThat(response.status).isEqualTo(HttpStatus.OK);
        assertThatJson(response.body)
                .node("name").isEqualTo("Replication service updated");
    }


    @Test
    public void testDelete() throws Exception {
        Service service = newService("Video service");
        TestRackInterface response = DELETE( String.join("/","/services", service.getId()));
        assertThat(response.status).isEqualTo(HttpStatus.OK);
        assertThat(serviceRepository.fetch(service.getId())).isNull();
    }

    @Test
    public void testInvalidServiceDelete() throws Exception {
        TestRackInterface response = DELETE("/services/doesnotexist");
        assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testGetAllEvents() throws Exception {
        Service service = newService("Messaging service");
        IntStream.range(0,3).forEach(n ->{
            Event e = new Event();
            e.setWhen(new Date());
            e.setSeverity("warn");
            e.setName("This is a cool event");
            e.setServiceId(service.getId());
            serviceRepository.addToCollection(service, e);
        });

        TestRackInterface response = GET(String.format("/services/%s/events",service.getId()));
        assertThat(response.status).isEqualTo(HttpStatus.OK);
        assertThatJson(response.body).isArray().ofLength(3);

        List<Event> list = new ObjectMapper().readValue(response.body, new TypeReference<List<Event>>(){});
        assertThat(list).hasSize(3);
        Event parsedEvent = list.get(0);
        assertThat(parsedEvent.getId()).isNotNull();
        assertThat(parsedEvent.getName()).isEqualTo("This is a cool event");
        assertThat(parsedEvent.getWhen()).isNotNull();
        assertThat(parsedEvent.getSeverity()).isEqualTo("warn");
        assertThat(parsedEvent.getServiceId()).isEqualTo(service.getId());

    }

    @Test
    public void testGetEvent() throws Exception {
        Event event = newEventWithValidService();
        TestRackInterface response = GET(String.format("/services/%s/events/%s",event.getServiceId(), event.getId()));
        assertThat(response.status).isEqualTo(HttpStatus.OK);
        assertThatJson(response.body)
                .node("id").isEqualTo(event.getId())
                .node("name").isEqualTo(event.getName())
                .node("severity").isEqualTo("downtime")
                .node("service_id").isEqualTo(event.getServiceId());

    }

    @Test
    public void testCreateEventWithInvalidService() throws Exception {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("name", "event something");
        body.put("severity", "warn");
        body.put("when", "2014-07-05T04:00:25Z");
        body.put("service_id", "33333333");

        TestRackInterface response = POST("/services/invalid/events", body);
        assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND);
        assertThatJson(response.body)
                .node("message").isEqualTo("Service id not found");
    }


    @Test
    public void testCreateEventWithInvalidEventData() throws Exception {
        Service service = newService("to invalid request");
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("name", "event something");
        body.put("severity", "warn");
        body.put("service_id", service.getId());

        TestRackInterface response = POST(String.format("/services/%s/events",service.getId()), body);
        assertThat(response.status).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThatJson(response.body)
                .node("message").isEqualTo("Field 'when' may not be null");
    }

    @Test
    public void testCreateWithValidService() throws Exception {
        Service service = newService("to invalid request");
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("name", "event something");
        body.put("severity", "warn");
        body.put("when", "2014-07-05T04:00:25Z");
        body.put("service_id", service.getId());

        TestRackInterface response =POST(String.format("/services/%s/events",service.getId()), body);
        assertThat(response.status).isEqualTo(HttpStatus.OK);
        assertThatJson(response.body)
                .node("name").isEqualTo("event something")
                .node("service_id").isEqualTo(service.getId());
    }

    @Test
    public void testUpdateEventWithInvalidService() throws Exception {
        Event event = newEventWithValidService();

        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("name", "foobar");

        TestRackInterface response = PUT(String.format("/services/invalid/events/%s",  event.getId()), body);
        assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND);
        assertThatJson(response.body)
                .node("message").isEqualTo("Service id not found");
    }

    @Test
    public void testUpdateEventWithValidService() throws Exception {
        Event event = newEventWithValidService();

        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("name","Updated name");

        TestRackInterface response = PUT(String.format("/services/%s/events/%s", event.getServiceId(), event.getId()), body);
        assertThat(response.status).isEqualTo(HttpStatus.OK);
        Event updated = eventRepository.fetch(event.getId());
        assertThat(updated.getName()).isEqualTo("Updated name");
    }

    @Test
    public void testUpdateEventWithNullFields() throws Exception {
        Event event = newEventWithValidService();
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.putNull("severity");

        TestRackInterface response = PUT(String.format("/services/%s/events/%s", event.getServiceId(), event.getId()), body);
        assertThat(response.status).isEqualTo(HttpStatus.OK);
        assertThat(eventRepository.fetch(event.getId()).getSeverity()).isNotNull();
    }


    @Test
    public void testDeleteEvent() throws Exception {
        Event event = newEventWithValidService();
        TestRackInterface response = DELETE(String.format("/services/%s/events/%s", event.getServiceId(), event.getId()));
        assertThat(response.status).isEqualTo(HttpStatus.OK);
        assertThat(eventRepository.fetch(event.getId())).isNull();
    }

    @Test
    public void testInvalidDeleteEvent() throws Exception {
        Service service = newService("to delete events");
        TestRackInterface response = DELETE(String.format("/services/%s/events/doesnotexist", service.getId()));
        assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Event newEventWithValidService() {
        Service service = newService("New service");
        return newEventWithValidService(service);
    }


    private Event newEventWithValidService(Service service) {
        Event event = new Event();
        event.generateId();
        event.setName("Cool event");
        event.setSeverity("downtime");
        event.setWhen(new Date());
        event.setServiceId(service.getId());
        eventRepository.save(event);
        serviceRepository.addToCollection(service, event);
        return event;
    }


    private Service newService(String message) {
        Service service = new Service();
        service.generateId();
        service.setName(message);
        serviceRepository.save(service);
        return service;
    }

}
