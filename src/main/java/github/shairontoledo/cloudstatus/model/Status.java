package github.shairontoledo.cloudstatus.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;

public class Status extends BaseModel{
    @NotNull
    @Pattern(regexp = "green|yellow|red")
    private String type;

    @NotNull
    private String message;

    @JsonProperty("event_id")
    @NotNull
    private String eventId;

    @JsonProperty("service_id")
    @NotNull
    private String serviceId;

    @JsonFormat(pattern ="yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date when;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public Date getWhen() {
        if (when == null){
            setWhen(new Date());
        }
        return when;
    }

    public void setWhen(Date when) {
        this.when = when;
    }

    @Override
    public double timeline() {
        return getWhen().getTime();
    }
}
