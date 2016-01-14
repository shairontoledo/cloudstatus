package github.shairontoledo.cloudstatus.model;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.UUID;

public class BaseModel implements Identifiable {
    private String id;

    @Override
    public String getId() {
        if (id == null) setId(generateIdWithPattern());
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String generateId(){
        id = null;
        return getId();
    }

    private String generateIdWithPattern(){
        String clazzName = this.getClass().getSimpleName().toLowerCase();
        return String.format("%s:%s", clazzName, UUID.randomUUID().toString());
    }

    @Override
    public double timeline() {
        return System.currentTimeMillis();
    }

    public <C> void enhanceFrom(C origin, String ...ignoreField) throws IllegalAccessException {
        for (Field field: this.getClass().getDeclaredFields()){
            field.setAccessible(true);
            Object originValue = field.get(origin);

            if ( originValue != null && ( ignoreField == null || !Arrays.asList(ignoreField).contains(field.getName())) ){
                field.set(this, originValue);
            }

        }
    }
}
