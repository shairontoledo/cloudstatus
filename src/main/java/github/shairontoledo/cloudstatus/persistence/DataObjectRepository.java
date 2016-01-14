package github.shairontoledo.cloudstatus.persistence;


import github.shairontoledo.cloudstatus.model.BaseModel;

import java.util.List;


public interface DataObjectRepository<T extends BaseModel>{

    void save(T item);
    List<T> findBy(String keyPattern);
    T fetch(String key);
    T delete(String key);
    <C extends BaseModel> void addToCollection(T parent, C ...child);
    <C extends BaseModel> List<C> fetchFomCollection(T parent, Class clazz);
    <C extends BaseModel> List<C> fromTimeline(long from, long to, Class clazz);
    <C extends BaseModel> void removeFomCollection(T parent, C child);
    void clear();

 }
