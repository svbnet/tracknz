package co.svbnet.tracknz.tracking;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Joe on 8/02/2016.
 */
public class TrackingServiceManager {

    private Set<Class<? extends TrackingService>> services = new HashSet<>();

    public void registerService(Class<? extends TrackingService> classOfT) {
        services.add(classOfT);
    }

    public void unregisterService(Class<? extends TrackingService> classOfT) {
        services.remove(classOfT);
    }

    public TrackingService newFromSource(String source) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        for (Class<? extends TrackingService> service : services) {
            if (source.equals(service.getDeclaredField("SOURCE").get(null))) {
                return service.newInstance();
            }
        }
        return null;
    }

    public List<String> getNames() throws NoSuchFieldException, IllegalAccessException {
        List<String> names = new ArrayList<>();
        for (Class<? extends TrackingService> service : services) {
            names.add((String) service.getDeclaredField("NAME").get(null));
        }
        return names;
    }

    public List<TrackedPackage> retrievePackages(List<TrackedPackage> packagesWithSource) {
        
    }

}
