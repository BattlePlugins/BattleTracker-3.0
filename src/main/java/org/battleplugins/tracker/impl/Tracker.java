package org.battleplugins.tracker.impl;

import org.battleplugins.tracker.TrackerInterface;
import org.battleplugins.tracker.stat.StatType;
import org.battleplugins.tracker.stat.record.Record;
import org.bukkit.OfflinePlayer;

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
    public void incrementValue(StatType statType, OfflinePlayer player) {
        incrementValue(statType.getInternalName(), player);
    }

    @Override
    public void incrementValue(String statType, OfflinePlayer player) {
        Record record = records.get(player.getName());
        record.setValue(statType, record.getStat(statType) + 1);
    }

    @Override
    public void decrementValue(StatType statType, OfflinePlayer player) {
        decrementValue(statType.getInternalName(), player);
    }

    @Override
    public void decrementValue(String statType, OfflinePlayer player) {
        Record record = records.get(player.getName());
        record.setValue(statType, record.getStat(statType) - 1);
    }

    @Override
    public void setValue(StatType statType, int value, OfflinePlayer player) {
        setValue(statType.getInternalName(), value, player);
    }

    @Override
    public void setValue(String statType, int value, OfflinePlayer player) {
        Record record = records.get(player.getName());
        record.setValue(statType, value);
    }

    @Override
    public void updateRating(OfflinePlayer killer, OfflinePlayer loser) {

    }

    @Override
    public void enableTracking(OfflinePlayer player) {
        Record record = records.get(player.getName());
        record.setTracking(true);
    }

    @Override
    public void disableTracking(OfflinePlayer player) {
        Record record = records.get(player.getName());
        record.setTracking(false);
    }

    @Override
    public void enableMessages(OfflinePlayer player) {

    }

    @Override
    public void disableMessages(OfflinePlayer player) {

    }

    @Override
    public void createNewRecord(OfflinePlayer player, Record record) {
        records.put(player.getName(), record);
    }

    @Override
    public void removeRecord(OfflinePlayer player) {
        records.remove(player.getName());
    }

    @Override
    public void flush() {

    }

    @Override
    public void save(OfflinePlayer player) {

    }

    @Override
    public void saveAll() {

    }
}
