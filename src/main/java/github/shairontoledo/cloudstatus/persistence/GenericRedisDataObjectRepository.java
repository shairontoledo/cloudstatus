package github.shairontoledo.cloudstatus.persistence;


import github.shairontoledo.cloudstatus.model.BaseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked")
public class GenericRedisDataObjectRepository<T extends BaseModel> implements DataObjectRepository<T>{
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void save(T item) {
        redisTemplate.opsForValue().getAndSet(item.getId(), item);
        index(item);
    }

    @Override
    public List<T> findBy(String keyPattern) {
        Set keys = redisTemplate.keys(keyPattern);
        return sortedKeys(redisTemplate.opsForValue().multiGet(keys));
    }
    private List<T> sortedKeys(List<T> list){
        list.sort((a,b) -> Double.compare(a.timeline(), b.timeline()) * -1 );
        return list;
    }
    @Override
    public T fetch(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    @Override
    public <C extends BaseModel> void addToCollection(T parent, C ...children) {
        for (C child : children) {
            redisTemplate.opsForValue().getAndSet(child.getId(), child);
            index((T) child);
            redisTemplate.opsForSet().add(collectionKey(parent, child.getClass()), child.getId());
        }
    }

    @Override
    public <C extends BaseModel> List<C> fetchFomCollection(T parent, Class clazz){
        Set keys = redisTemplate.opsForSet().members(collectionKey(parent, clazz));
        return (List<C>) sortedKeys(redisTemplate.opsForValue().multiGet(keys));
    }

    @Override
    public <C extends BaseModel> void removeFomCollection(T parent, C child){
        redisTemplate.opsForSet().remove(collectionKey(parent,child.getClass()), child.getId());
    }

    @Override
    public void clear() {
        //FIXME need delete only repository scope, being used only from tests for now.
        Set keys = redisTemplate.keys("*");
        redisTemplate.delete(keys);
    }

    @Override
    public <C extends BaseModel> List<C> fromTimeline(long from, long to, Class clazz){
        Set keys = redisTemplate.opsForZSet().rangeByScore(indexKey(clazz),from, to);

        return redisTemplate.opsForValue().multiGet(keys);
    }

    @Override
    public T delete(String key) {
        T obj = fetch(key);
        if (obj != null){

            redisTemplate.opsForZSet().remove(indexKey(obj.getClass()), obj.getId());
            for (Object o: redisTemplate.keys(collectionKey(obj, "*")) ){
                redisTemplate.delete(o);
            }
            redisTemplate.delete(key);
        }
        return obj;
    }

    private String collectionKey(T parent, Class clazz) {
        return collectionKey(parent, clazz.getClass().getSimpleName().toLowerCase());
    }

    private String collectionKey(T parent, String suffix) {
        return String.format("collection_%s_%s", parent.getId(), suffix);
    }

    private void index(T obj){
        double timeline = obj.timeline();
        redisTemplate.opsForZSet().add(indexKey(obj.getClass()), obj.getId(), timeline);
    }

    private String indexKey(Class clazz){
        return String.format("index_%s", clazz.getSimpleName().toLowerCase());
    }

}
