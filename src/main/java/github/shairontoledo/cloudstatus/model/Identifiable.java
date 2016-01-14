package github.shairontoledo.cloudstatus.model;

public interface Identifiable extends TimelineIndexable {
    String getId();

    void setId(String id);

    String generateId();
}
