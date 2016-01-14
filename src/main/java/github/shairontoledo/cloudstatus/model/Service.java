package github.shairontoledo.cloudstatus.model;

import javax.validation.constraints.NotNull;

public class Service extends BaseModel {
    @NotNull
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
