import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author elig
 */

public class MapDS<K, V> extends HashMap<K, V> {
    public MapDS() {super();}
    //    public MapDS(double size) {super((int)size);}
    public MapDS(List<K> keys, List<V> values) {
        super();
        assert keys.size() == values.size();
        for(int i = 0; i < keys.size(); i++) {
            put(keys.get(i), values.get(i));
        }
    }
    public V get(Object key){return (V) super.get(key);}
    public V getOrDefault(Object key, Object defaultValue) { return (V) super.getOrDefault(key, (V)defaultValue); }
    public V getOrPutGet(Object key, Object defaultValue) {
        V value = get(key);
        if(value == null) {
            put((K) key, (V) defaultValue);
            value = get(key);
        }
        return value;
    }
    public Map<K, V> toMap(){
        return this;
    }
}
