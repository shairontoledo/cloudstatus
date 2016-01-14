package github.shairontoledo.cloudstatus;

import github.shairontoledo.cloudstatus.model.Event;
import github.shairontoledo.cloudstatus.model.Service;
import github.shairontoledo.cloudstatus.persistence.DataObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class EventsController extends BaseController{

    @Autowired
    DataObjectRepository<Event> eventRepository;

    @Autowired
    DataObjectRepository<Service> serviceRepository;

    @RequestMapping(method = GET, value = "/events")
    public List<Event> getAll() {
        return eventRepository.findBy("event:*");
    }

    @RequestMapping(method = GET, value = "/events/{id}")
    public Event get(@PathVariable String id) throws HttpRequestException {
        return getEventById(id);
    }

    @RequestMapping(method = POST, value = "/events")
    public Event create(@RequestBody Event event, BindingResult result) throws HttpRequestException {
        event.generateId();
        validate(event, result);

        eventRepository.save(event);
        return event;
    }

    @RequestMapping(method = PUT, value = "/events/{id}")
    public Event update(@PathVariable String id, @RequestBody Event event, BindingResult result) throws Exception {
        Event existingEvent = getEventById(id);

        existingEvent.enhanceFrom(event, "id");
        validate(existingEvent, result);
        eventRepository.save(existingEvent);
        return existingEvent;
    }

    @RequestMapping(method = DELETE, value = "/events/{id}")
    public Event delete(@PathVariable String id) throws HttpRequestException {
        Event existingEvent = getEventById(id);
        return eventRepository.delete(existingEvent.getId());
    }

    public Event getEventById(String id) throws HttpRequestException {
        Event event = eventRepository.fetch(id);
        if (event == null){
            throw new HttpNotFoundException("Event id not found");
        }
        return event;
    }

    @Override
    public void validate(Object target, BindingResult result) throws HttpRequestException {
        Event event = (Event) target;
        super.validate(event, result);
        if (serviceRepository.fetch(event.getServiceId()) == null){
            throw new HttpNotFoundException("Service id not found");
        }
    }
}
