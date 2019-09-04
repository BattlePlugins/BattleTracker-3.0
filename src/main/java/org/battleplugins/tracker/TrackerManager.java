package org.battleplugins.tracker;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class contains all the necessary tracker info and handles
 * saving/loading new tracker interfaces. It is also injected into
 * the platform's service provider API, so other plugins can easily
 * access this class.
 *
 * @author Redned
 */
public class TrackerManager {

    private boolean trackPvP;
    private boolean trackPvE;

    private Map<String, TrackerInterface> interfaces;

    public TrackerManager() {
        interfaces = Collections.synchronizedMap(new ConcurrentHashMap<String, TrackerInterface>());
        trackPvP = true;
        trackPvE = true;
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
        return interfaces.get(BattleTracker.PVP_INTERFACE);
    }

    /**
     * Returns the PvE tracker interface
     *
     * @return the PvE tracker interface
     */
    public TrackerInterface getPvEInterface() {
        return interfaces.get(BattleTracker.PVE_INTERFACE);
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

    /**
     * Adds a new tracker interface
     *
     * @param interfaceName the name of the tracker interface
     * @param trackerInterface the tracker interface
     */
    public void addInterface(String interfaceName, TrackerInterface trackerInterface) {
       interfaces.put(interfaceName, trackerInterface);
    }

    /**
     * Removes an existing tracker interface
     *
     * @param interfaceName the name of the tracker interface
     */
    public void removeInterface(String interfaceName) {
        interfaces.remove(interfaceName);
    }

    /**
     * Returns if PvP is tracked
     *
     * @return if PvP is tracked
     */
    public boolean isTrackingPvP() {
        return trackPvP;
    }

    /**
     * Sets if PvP should be tracked
     *
     * @param trackPvP if PvP should be tracked
     */
    public void setTrackPvP(boolean trackPvP) {
        this.trackPvP = trackPvP;
    }

    /**
     * Returns if PvE is tracked
     *
     * @return if PvE is tracked
     */
    public boolean isTrackingPvE() {
        return trackPvE;
    }

    /**
     * Sets if PvE should be tracked
     *
     * @param trackPvE if PvE should be tracked
     */
    public void setTrackPvE(boolean trackPvE) {
        this.trackPvE = trackPvE;
    }

    /**
     * Returns a map of the tracker interfaces.
     *
     * Key: the name of the interface
     * Value: the tracker object
     *
     * @return a map of the tracker interfaces.
     */
    public Map<String, TrackerInterface> getInterfaces() {
        return interfaces;
    }
}
