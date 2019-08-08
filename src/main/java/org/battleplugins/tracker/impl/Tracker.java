package org.battleplugins.tracker.impl;

import mc.alk.mc.MCOfflinePlayer;
import org.battleplugins.tracker.TrackerInterface;
import org.battleplugins.tracker.stat.StatType;
import org.battleplugins.tracker.stat.calculator.RatingCalculator;
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

    protected RatingCalculator calculator;
    protected Map<String, Record> records;

    public Tracker(String name, RatingCalculator calculator, Map<String, Record> records) {
        this.name = name;
        this.calculator = calculator;
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
    public boolean hasRecord(MCOfflinePlayer player) {
        return records.containsKey(player.getName());
    }

    @Override
    public Record getRecord(MCOfflinePlayer player) {
        return records.get(player.getName());
    }

    @Override
    public Map<String, Record> getRecords() {
        return records;
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
    public void setValue(StatType statType, float value, MCOfflinePlayer player) {
        setValue(statType.getInternalName(), value, player);
    }

    @Override
    public void setValue(String statType, float value, MCOfflinePlayer player) {
        Record record = records.get(player.getName());
        record.setValue(statType, value);
    }

    @Override
    public void updateRating(MCOfflinePlayer killer, MCOfflinePlayer killed, boolean tie) {
        Record killerRecord = getRecord(killer);
        Record killedRecord = getRecord(killed);
        calculator.updateRating(killerRecord, killedRecord, tie);

        float killerRating = killerRecord.getRating();
        float killedRating = killedRecord.getRating();

        float killerMaxRating = killerRecord.getStat(StatType.MAX_RATING);
        float killedMaxRating = killedRecord.getStat(StatType.MAX_RATING);

        setValue(StatType.RATING, killerRecord.getRating(), killer);
        setValue(StatType.RATING, killedRecord.getRating(), killed);

        if (killerRating > killerMaxRating)
            setValue(StatType.MAX_RATING, killerRecord.getRating(), killer);

        if (killedRating > killedMaxRating)
            setValue(StatType.MAX_RATING, killedRecord.getRating(), killed);

        if (tie) {
            setValue(StatType.TIES, killerRecord.getStat(StatType.TIES) + 1, killer);
            setValue(StatType.TIES, killedRecord.getStat(StatType.TIES) + 1, killed);
        }
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
