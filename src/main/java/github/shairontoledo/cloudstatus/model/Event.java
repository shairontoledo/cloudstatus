package github.shairontoledo.cloudstatus.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.Date;

public class Event extends BaseModel {

    @JsonFormat(pattern ="yyyy-MM-dd'T'HH:mm:ss'Z'")
    @NotNull
    private Date when;

    @NotNull
    private String name;

    @NotNull
    private String severity;

    @JsonProperty("service_id")
    @NotNull
    private String serviceId;

    public Event() {
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getWhen() {
        return when;
    }

    public void setWhen(Date when) {
        this.when = when;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public double timeline() {
        return getWhen().getTime();
    }
}