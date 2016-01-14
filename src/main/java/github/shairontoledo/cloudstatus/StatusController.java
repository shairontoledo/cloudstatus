package github.shairontoledo.cloudstatus;

import github.shairontoledo.cloudstatus.model.Event;
import github.shairontoledo.cloudstatus.model.Service;
import github.shairontoledo.cloudstatus.model.Status;
import github.shairontoledo.cloudstatus.persistence.DataObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;


//@RequestMapping(value = "/statuses")
@RestController
public class StatusController extends BaseController{

    @Autowired
    DataObjectRepository<Status> statusRepository;
    @Autowired
    DataObjectRepository<Event> eventRepository;
    @Autowired
    DataObjectRepository<Service> serviceRepository;


    @RequestMapping(method = GET, value = "/statuses")
    public List<Status> getAll() {
        return statusRepository.findBy("status:*");
    }

    @RequestMapping(method = GET, value = "/statuses/{id}")
    public Status get(@PathVariable String id) throws HttpNotFoundException {
        return getStatusById(id);
    }


    @RequestMapping(method = GET, value = "/services/{serviceId}/events/{eventId}/statuses/{id}")
    public Status getStatus(@PathVariable String serviceId, @PathVariable String eventId, @PathVariable String id) throws HttpNotFoundException {
        getServiceById(serviceId);
        getEventById(eventId);

        return getStatusById(id);
    }

    @RequestMapping(method = GET, value = "/services/{serviceId}/events/{eventId}/statuses")
    public List<Status> getAllEventStatuses(@PathVariable String serviceId, @PathVariable String eventId) throws HttpNotFoundException {
        getServiceById(serviceId);
        Event event = getEventById(eventId);
        return eventRepository.fetchFomCollection(event, Status.class);
    }

    @RequestMapping(method = POST, value = "/services/{serviceId}/events/{eventId}/statuses")
    public Status createStatus(@PathVariable String serviceId, @PathVariable String eventId, @RequestBody Status status, BindingResult result) throws HttpRequestException {
        getServiceById(serviceId);
        Event event = getEventById(eventId);
        status.setServiceId(serviceId);
        status.setEventId(eventId);
        if (status.getWhen() == null){
            status.setWhen(new Date());
        }
        validate(status, result);
        statusRepository.save(status);
        eventRepository.addToCollection(event, status);
        return status;
    }

    @RequestMapping(method = DELETE, value = "/services/{serviceId}/events/{eventId}/statuses/{id}")
    public Status deleteStatus(@PathVariable String serviceId, @PathVariable String eventId, @PathVariable String id) throws HttpNotFoundException {
        getServiceById(serviceId);
        Event event = getEventById(eventId);
        Status status = getStatusById(id);
        statusRepository.delete(status.getId());
        eventRepository.removeFomCollection(event, status);
        return status;
    }

    public Service getServiceById(String id) throws HttpNotFoundException {
        Service service = serviceRepository.fetch(id);
        if (service == null){
            throw new HttpNotFoundException("Service id not found");
        }
        return service;
    }

    public Event getEventById(String id) throws HttpNotFoundException {
        Event event = eventRepository.fetch(id);
        if (event == null){
            throw new HttpNotFoundException("Event id not found");
        }
        return event;
    }

    private Status getStatusById(@PathVariable String id) throws HttpNotFoundException {
        Status status = statusRepository.fetch(id);
        if (status == null){
            throw new HttpNotFoundException("Status id not found");
        }
        return status;
    }

}
