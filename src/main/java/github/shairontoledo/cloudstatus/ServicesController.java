package github.shairontoledo.cloudstatus;

import github.shairontoledo.cloudstatus.model.Event;
import github.shairontoledo.cloudstatus.model.Service;
import github.shairontoledo.cloudstatus.persistence.DataObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class ServicesController extends BaseController {
    @Autowired
    DataObjectRepository<Service> serviceRepository;

    @Autowired
    DataObjectRepository<Event> eventRepository;

    @RequestMapping(method = GET, value = "/services")
    public List<Service> getAll() {
        return serviceRepository.findBy("service:*");
    }

    @RequestMapping(method = GET, value = "/services/{id}")
    public Service get(@PathVariable String id) throws Exception{ return getServiceById(id); }

    @RequestMapping(method = PUT, value = "/services/{id}")
    public Service updateService(@PathVariable String id, @RequestBody Service service, BindingResult result) throws Exception{
        Service existingService = getServiceById(id);
        validate(service, result);
        existingService.setName(service.getName());
        serviceRepository.save(existingService);

        return service;
    }

    @RequestMapping(method = POST, value = "/services")
    public Service create(@RequestBody Service service, BindingResult result) throws Exception{
        validate(service, result);
        serviceRepository.save(service);
        return service;
    }

    @RequestMapping(method = DELETE, value = "/services/{id}")
    public void deleteService(@PathVariable String id) throws Exception{
        Service service = getServiceById(id);
        serviceRepository.delete(service.getId());
    }

    @RequestMapping(method = POST, value = "/services/{id}/events")
    public Event create(@PathVariable String id, @RequestBody Event event, BindingResult result) throws Exception {
        Service service = getServiceById(id);
        event.setServiceId(service.getId());
        validate(event, result);
        eventRepository.save(event);
        serviceRepository.addToCollection(service,event);
        return event;
    }

    @RequestMapping(method = GET, value = "/services/{id}/events")
    public List<Event> listEvents(@PathVariable String id) throws Exception{
        Service service = getServiceById(id);
        return serviceRepository.fetchFomCollection(service, Event.class);
    }

    @RequestMapping(method = GET, value = "/services/{serviceId}/events/{id}")
    public Event getEvent(@PathVariable String serviceId, @PathVariable String id) throws Exception {
        Service service = getServiceById(serviceId);
        Event event = getEventById(id);
        return event;
    }

    @RequestMapping(method = PUT, value = "/services/{serviceId}/events/{id}")
    public Event updateEvent(@PathVariable String serviceId, @PathVariable String id, @RequestBody Event event, BindingResult result) throws Exception {
        Service service = getServiceById(serviceId);
        Event existingEvent = getEventById(id);
        existingEvent.enhanceFrom(event, "id", "serviceId");
        existingEvent.setServiceId(service.getId());
        validate(existingEvent, result);
        eventRepository.save(existingEvent);
        serviceRepository.addToCollection(service,existingEvent);
        return existingEvent;
    }

    @RequestMapping(method = DELETE, value = "/services/{serviceId}/events/{id}")
    public Event deleteEvent(@PathVariable String serviceId, @PathVariable String id) throws Exception{
        Service service = getServiceById(serviceId);
        Event event = getEventById(id);
        eventRepository.delete(event.getId());
        serviceRepository.removeFomCollection(service, event);
        return event;
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

}
