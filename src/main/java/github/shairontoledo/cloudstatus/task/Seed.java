package github.shairontoledo.cloudstatus.task;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import github.shairontoledo.cloudstatus.model.Event;
import github.shairontoledo.cloudstatus.model.Service;
import github.shairontoledo.cloudstatus.model.Status;
import github.shairontoledo.cloudstatus.persistence.DataObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.Date;

public class Seed {

    private static final Logger logger = LoggerFactory.getLogger(Seed.class);
    @Autowired
    DataObjectRepository<Event> eventRepository;

    @Autowired
    DataObjectRepository<Service> serviceRepository;

    @Autowired
    DataObjectRepository<Status> statusRepository;

    public  void seed(String filename) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new File(filename));
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            JsonNode servicesPath = root.path("services");
            for(JsonNode serviceNode: servicesPath){

                Service service = mapper.convertValue(serviceNode, Service.class);
                serviceRepository.save(service);
                for(JsonNode eventNode: serviceNode.path("events")){
                    Event event = mapper.convertValue(eventNode, Event.class);
                    event.setServiceId(service.getId());
                    eventRepository.save(event);
                    serviceRepository.addToCollection(service,event);
                    for (JsonNode statusNode: eventNode.path("statuses")){
                        Status status = mapper.convertValue(statusNode, Status.class);

                        status.setServiceId(service.getId());
                        status.setEventId(event.getId());
                        if (status.getWhen() == null){
                            status.setWhen(new Date());
                        }
                        statusRepository.save(status);
                        eventRepository.addToCollection(event, status);
                    }
                }
            }
            logger.info(String.format("*** Seed file %s loaded successfully", filename));
            System.exit(0);
        } catch (Exception e) {
            logger.error(String.format("*** Something wrong loading file '%s', error message '%s'", filename, e.getMessage()));
            System.exit(1);
        }

    }
}
