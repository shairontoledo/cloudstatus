package github.shairontoledo.cloudstatus.model;

import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;


public class BaseModelTest {

    @Test
    public void testId(){
        BaseModel ident = new BaseModel();
        assertThat(ident.getId())
                .as("auto generante id")
                .isNotEmpty();
    }
    @Test
    public void testPrefix(){
        BaseModel ident = new BaseModel();

        assertThat(ident.getId())
                .as("Id has class name")
                .contains("basemodel");
    }

    @Test
    public void testPrefixInherited(){

        class Foo extends BaseModel {}
        Foo foo = new Foo();

        assertThat(foo.getId())
                .as("auto generante id in inherited class")
                .isNotEmpty();

        assertThat(foo.getId())
                .as("Id has class name in inherited class")
                .contains("foo");
    }

    @Test
    public void testGenerateId(){

        BaseModel ident = new BaseModel();
        String firstId = ident.getId();

        ident.generateId();

        assertThat(ident.getId())
                .as("Generate another id")
                .isNotEqualTo(firstId);
    }

    @Test
    public void testEnhanceFrom() throws Exception {
        Event event1 = new Event();
        event1.generateId();
        event1.setName("Cool event");
        event1.setSeverity("downtime");
        event1.setWhen(new Date());
        event1.setServiceId("service:1");

        Event event2 = new Event();
        event2.setName("Event from 2");
        event2.setSeverity(null);
        event2.setServiceId("service:2");

        event1.enhanceFrom(event2, new String[]{"serviceId"});
        assertThat(event1.getName()).isEqualTo("Event from 2");
        assertThat(event1.getSeverity()).isEqualTo("downtime");
        assertThat(event1.getServiceId()).isEqualTo("service:1");
    }
}