package github.shairontoledo.cloudstatus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import github.shairontoledo.cloudstatus.model.Event;
import github.shairontoledo.cloudstatus.model.Service;
import github.shairontoledo.cloudstatus.model.Status;
import github.shairontoledo.cloudstatus.persistence.DataObjectRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Date;
import java.util.List;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@ContextConfiguration(classes = {Application.class})
@WebAppConfiguration
public class StatusResourcesTest extends TestHttpRequest{

    @Autowired
    private DataObjectRepository<Status> statusRepository;

    @Test
    public void testGetAllStatus() throws Exception {

        newStatus("green", "Everything ok now");

        TestRackInterface response = GET("/statuses");
        assertThat(response.status).isEqualTo(HttpStatus.OK);
        assertThatJson(response.body).isArray().ofLength(1);

        List<Status> list = new ObjectMapper().readValue(response.body, new TypeReference<List<Status>>(){});
        assertThat(list).hasSize(1);
        Status parsedStatus = list.get(0);
        assertThat(parsedStatus.getId()).isNotNull();
        assertThat(parsedStatus.getMessage()).isEqualTo("Everything ok now");
        assertThat(parsedStatus.getType()).isEqualTo("green");
        assertThat(parsedStatus.getEventId()).asString().contains("event:");
        assertThat(parsedStatus.getServiceId()).asString().contains("service:");
        assertThat(parsedStatus.getId()).asString().contains("status:");

    }

    @Test
    public void testGetOneStatus() throws Exception {

        Status status = newStatus("green", "Everything ok now");

        TestRackInterface response = GET("/statuses/"+status.getId());
        assertThat(response.status).isEqualTo(HttpStatus.OK);

        assertThatJson(response.body)
                .node("id").isEqualTo(status.getId())
                .node("service_id").isEqualTo(status.getServiceId())
                .node("event_id").isEqualTo(status.getEventId())
                .node("message").isEqualTo(status.getMessage())
                .node("type").isEqualTo(status.getType());

    }

    @Test
    public void testGetStatusFromEvent() throws Exception {
        Status status = newStatus("green", "Everything ok now");

        TestRackInterface response = GET(String.format("/services/%s/events/%s/statuses/%s",
                status.getServiceId(),
                status.getEventId(),
                status.getId()));
        assertThat(response.status).isEqualTo(HttpStatus.OK);

        assertThatJson(response.body)
                .node("id").isEqualTo(status.getId())
                .node("service_id").isEqualTo(status.getServiceId())
                .node("event_id").isEqualTo(status.getEventId())
                .node("message").isEqualTo(status.getMessage())
                .node("type").isEqualTo(status.getType());
    }

    @Test
    public void testGetStatusFromEventWithInvalidEvent() throws Exception {
        Status status = newStatus("green", "Everything ok now");

        TestRackInterface response = GET(String.format("/services/%s/events/invalid/statuses/%s",
                status.getServiceId(),
                status.getEventId(),
                status.getId()));
        assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    public void testCreateStatus() throws Exception {
        Status mockStatus = newStatus("green", "Everything ok now");

        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("type", "yellow");
        body.put("message", "Something wrong now");
        TestRackInterface response = POST(String.format("/services/%s/events/%s/statuses",
                mockStatus.getServiceId(),
                mockStatus.getEventId()), body);
        assertThat(response.status).isEqualTo(HttpStatus.OK);

        Status newStatus = new ObjectMapper().readValue(response.body, new TypeReference<Status>(){});
        assertThat(newStatus).isNotNull();

        assertThat(newStatus.getId()).isNotNull();
        assertThat(newStatus.getType()).isEqualTo("yellow");
        assertThat(newStatus.getWhen()).isNotNull();
        assertThat(newStatus.getMessage()).isEqualTo("Something wrong now");
        assertThat(statusRepository.fetch(newStatus.getId())).isNotNull();

    }

    @Test
    public void testCreateStatusWithoutMessage() throws Exception {
        Status mockStatus = newStatus("green", "Everything ok now");

        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("type", "yellow");

        TestRackInterface response = POST(String.format("/services/%s/events/%s/statuses",
                mockStatus.getServiceId(),
                mockStatus.getEventId()), body);
        assertThat(response.status).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThatJson(response.body)
                .node("message").isEqualTo("Field 'message' may not be null");
    }

    @Test
    public void testCreateStatusWithoutType() throws Exception {
        Status mockStatus = newStatus("green", "Everything ok now");

        ObjectNode body = JsonNodeFactory.instance.objectNode();

        body.put("message", "Something wrong now");
        TestRackInterface response = POST(String.format("/services/%s/events/%s/statuses",
                mockStatus.getServiceId(),
                mockStatus.getEventId()), body);
        assertThat(response.status).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThatJson(response.body)
                .node("message").isEqualTo("Field 'type' may not be null");
    }

    @Test
    public void testCreateStatusWithInvalidEventId() throws Exception {
        Status mockStatus = newStatus("green", "Everything ok now");

        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("type", "yellow");
        body.put("message", "Something wrong now");
        TestRackInterface response = POST(String.format("/services/%s/events/%s/statuses",
                mockStatus.getServiceId(),
                "invalid"), body);
        assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND);
        assertThatJson(response.body)
                .node("message").isEqualTo("Event id not found");
    }

    @Test
    public void testCreateStatusWithInvalidServiceId() throws Exception {
        Status mockStatus = newStatus("green", "Everything ok now");

        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("type", "yellow");
        body.put("message", "Something wrong now");
        TestRackInterface response = POST(String.format("/services/%s/events/%s/statuses",
                "invalid",
                mockStatus.getEventId()), body);
        assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND);
        assertThatJson(response.body)
                .node("message").isEqualTo("Service id not found");
    }

    @Test
    public void testDelete() throws Exception {
        Status mockStatus = newStatus("green", "Everything ok now");
        TestRackInterface response = DELETE(String.format("/services/%s/events/%s/statuses/%s",
                mockStatus.getServiceId(),
                mockStatus.getEventId(),
                mockStatus.getId()));
        assertThat(response.status).isEqualTo(HttpStatus.OK);
        assertThat(statusRepository.fetch(mockStatus.getId())).isNull();
    }

    @Test
    public void testInvalidServiceDelete() throws Exception {
        Status mockStatus = newStatus("green", "Everything ok now");
        TestRackInterface response = DELETE(String.format("/services/%s/events/%s/statuses/%s",
                mockStatus.getServiceId(),
                mockStatus.getEventId(),
                "invalid"));
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

    private Status newStatus(String type, String message){
        Event event = newEventWithValidService();
        Status status = new Status();
        status.setMessage(message);
        status.setType(type);
        status.setServiceId(event.getServiceId());
        status.setEventId(event.getId());
        statusRepository.save(status);
        eventRepository.addToCollection(event, status);
        return status;
    }

    private Service newService(String message) {
        Service service = new Service();
        service.generateId();
        service.setName(message);
        serviceRepository.save(service);
        return service;
    }

}
