package org.battleplugins.tracker;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class contains all the necessary tracker info and handles
 * saving/loading new tracker interfaces. It is also injected into
 * Bukkit's service provider API, so other plugins can easily
 * access this class.
 *
 * @author Redned
 */
public class TrackerManager {

    private Map<String, TrackerInterface> interfaces;

    public TrackerManager() {
        interfaces = Collections.synchronizedMap(new ConcurrentHashMap<String, TrackerInterface>());
    }

    /**
     * Returns the tracker interface with the given name, null
     * if one doesn't exist under that name
     *
     * @param interfaceName the name of the tracker interface
     * @return the tracker interface with the given name
     */
    public TrackerInterface getInterface(String interfaceName) {
        return interfaces.get(interfaceName);
    }

    /**
     * Returns the PvP tracker interface
     *
     * @return the PvP tracker interface
     */
    public TrackerInterface getPvPInterface() {
        return interfaces.get(Tracker.PVP_INTERFACE);
    }

    /**
     * Returns the PvE tracker interface
     *
     * @return the PvE tracker interface
     */
    public TrackerInterface getPvEInterface() {
        return interfaces.get(Tracker.PVE_INTERFACE);
    }

    /**
     * Returns if an interface with the given name exists
     *
     * @param interfaceName the name of the interface to check
     * @return if an interface with the given name exists
     */
    public boolean hasInterface(String interfaceName) {
        return interfaces.containsKey(interfaceName);
    }
}
