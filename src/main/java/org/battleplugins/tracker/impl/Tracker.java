package org.battleplugins.tracker.impl;

import mc.alk.mc.MCOfflinePlayer;
import org.battleplugins.tracker.TrackerInterface;
import org.battleplugins.tracker.stat.StatType;
import org.battleplugins.tracker.stat.record.Record;

import java.util.Map;

/**
 * Main implementation of a tracker instance. Any plugin
 * wanting to track data should extend this class or
 * use it as the implementation.
 *
 * @author Redned
 */
public class Tracker implements TrackerInterface {

    private String name;

    protected Map<String, Record> records;

    public Tracker(String name, Map<String, Record> records) {
        this.name = name;
        this.records = records;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getRecordCount() {
        return records.size();
    }

    @Override
    public void incrementValue(StatType statType, MCOfflinePlayer player) {
        incrementValue(statType.getInternalName(), player);
    }

    @Override
    public void incrementValue(String statType, MCOfflinePlayer player) {
        Record record = records.get(player.getName());
        record.setValue(statType, record.getStat(statType) + 1);
    }

    @Override
    public void decrementValue(StatType statType, MCOfflinePlayer player) {
        decrementValue(statType.getInternalName(), player);
    }

    @Override
    public void decrementValue(String statType, MCOfflinePlayer player) {
        Record record = records.get(player.getName());
        record.setValue(statType, record.getStat(statType) - 1);
    }

    @Override
    public void setValue(StatType statType, int value, MCOfflinePlayer player) {
        setValue(statType.getInternalName(), value, player);
    }

    @Override
    public void setValue(String statType, int value, MCOfflinePlayer player) {
        Record record = records.get(player.getName());
        record.setValue(statType, value);
    }

    @Override
    public void updateRating(MCOfflinePlayer killer, MCOfflinePlayer loser) {

    }

    @Override
    public void enableTracking(MCOfflinePlayer player) {
        Record record = records.get(player.getName());
        record.setTracking(true);
    }

    @Override
    public void disableTracking(MCOfflinePlayer player) {
        Record record = records.get(player.getName());
        record.setTracking(false);
    }

    @Override
    public void enableMessages(MCOfflinePlayer player) {

    }

    @Override
    public void disableMessages(MCOfflinePlayer player) {

    }

    @Override
    public void createNewRecord(MCOfflinePlayer player, Record record) {
        records.put(player.getName(), record);
    }

    @Override
    public void removeRecord(MCOfflinePlayer player) {
        records.remove(player.getName());
    }

    @Override
    public void flush() {

    }

    @Override
    public void save(MCOfflinePlayer player) {

    }

    @Override
    public void saveAll() {

    }
}
