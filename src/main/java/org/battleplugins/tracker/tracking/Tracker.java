package org.battleplugins.tracker.tracking;

import mc.alk.battlecore.configuration.Configuration;
import mc.alk.mc.MCOfflinePlayer;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.sql.SQLInstance;
import org.battleplugins.tracker.tracking.message.DeathMessageManager;
import org.battleplugins.tracker.tracking.recap.RecapManager;
import org.battleplugins.tracker.tracking.stat.StatType;
import org.battleplugins.tracker.tracking.stat.calculator.RatingCalculator;
import org.battleplugins.tracker.tracking.stat.record.PlayerRecord;
import org.battleplugins.tracker.tracking.stat.record.Record;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Main implementation of a tracker instance. Any plugin
 * wanting to track data should extend this class or
 * use it as the implementation.
 *
 * @author Redned
 */
public class Tracker implements TrackerInterface {

    protected String name;

    protected DeathMessageManager messageManager;
    protected RecapManager recapManager;
    protected RatingCalculator calculator;

    protected Map<UUID, Record> records;

    protected SQLInstance sql;

    public Tracker(BattleTracker plugin, String name, Configuration config, RatingCalculator calculator, Map<UUID, Record> records) {
        this.name = name;
        this.recapManager = new RecapManager(plugin);
        this.messageManager = new DeathMessageManager(plugin, this, config);
        this.calculator = calculator;
        this.records = records;

        this.sql = new SQLInstance(this);
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
        return records.containsKey(player.getUniqueId());
    }

    @Override
    public Record getRecord(MCOfflinePlayer player) {
        return records.get(player.getUniqueId());
    }

    @Override
    public Map<UUID, Record> getRecords() {
        return records;
    }

    @Override
    public void incrementValue(StatType statType, MCOfflinePlayer player) {
        incrementValue(statType.getInternalName(), player);
    }

    @Override
    public void incrementValue(String statType, MCOfflinePlayer player) {
        Record record = records.get(player.getUniqueId());
        record.setValue(statType, record.getStat(statType) + 1);
    }

    @Override
    public void decrementValue(StatType statType, MCOfflinePlayer player) {
        decrementValue(statType.getInternalName(), player);
    }

    @Override
    public void decrementValue(String statType, MCOfflinePlayer player) {
        Record record = records.get(player.getUniqueId());
        record.setValue(statType, record.getStat(statType) - 1);
    }

    @Override
    public void setValue(StatType statType, float value, MCOfflinePlayer player) {
        setValue(statType.getInternalName(), value, player);
    }

    @Override
    public void setValue(String statType, float value, MCOfflinePlayer player) {
        Record record = records.get(player.getUniqueId());
        record.setValue(statType, value);
    }

    @Override
    public void updateRating(MCOfflinePlayer killer, MCOfflinePlayer killed, boolean tie) {
        Record killerRecord = getRecord(killer);
        Record killedRecord = getRecord(killed);
        calculator.updateRating(killerRecord, killedRecord, tie);

        float killerRating = killerRecord.getRating();
        float killerMaxRating = killerRecord.getStat(StatType.MAX_RATING);

        setValue(StatType.RATING, killerRecord.getRating(), killer);
        setValue(StatType.RATING, killedRecord.getRating(), killed);

        if (killerRating > killerMaxRating)
            setValue(StatType.MAX_RATING, killerRating, killer);

        if (tie) {
            incrementValue(StatType.TIES, killer);
            incrementValue(StatType.TIES, killed);
        }

        setValue(StatType.KD_RATIO, killerRecord.getStat(StatType.KILLS) / killerRecord.getStat(StatType.DEATHS), killer);
        setValue(StatType.KD_RATIO, killedRecord.getStat(StatType.KILLS) / killedRecord.getStat(StatType.DEATHS), killed);

        float killerKdr = killerRecord.getStat(StatType.KD_RATIO);
        float killerMaxKdr = killerRecord.getStat(StatType.MAX_KD_RATIO);

        if (killerKdr > killerMaxKdr)
            setValue(StatType.MAX_KD_RATIO, killerKdr, killer);

        setValue(StatType.STREAK, 0, killed);
        incrementValue(StatType.STREAK, killer);

        float killerStreak = killerRecord.getStat(StatType.STREAK);
        float killerMaxStreak = killerRecord.getStat(StatType.MAX_STREAK);

        if (killerStreak > killerMaxStreak)
            setValue(StatType.MAX_STREAK, killerStreak, killer);
    }

    @Override
    public void enableTracking(MCOfflinePlayer player) {
        Record record = records.get(player.getUniqueId());
        record.setTracking(true);
    }

    @Override
    public void disableTracking(MCOfflinePlayer player) {
        Record record = records.get(player.getUniqueId());
        record.setTracking(false);
    }

    @Override
    public void enableMessages(MCOfflinePlayer player) {
        records.get(player.getUniqueId()).setSendMessages(true);
    }

    @Override
    public void disableMessages(MCOfflinePlayer player) {
        records.get(player.getUniqueId()).setSendMessages(false);
    }

    @Override
    public void createNewRecord(MCOfflinePlayer player) {
        Map<String, Float> columns = new HashMap<>();
        for (String column : sql.getOverallColumns()) {
            columns.put(column, 0f);
        }

        Record record = new PlayerRecord(this, player.getUniqueId().toString(), player.getName(), columns);
        record.setRating(calculator.getDefaultRating());
        createNewRecord(player, record);
    }

    @Override
    public void createNewRecord(MCOfflinePlayer player, Record record) {
        record.setRating(calculator.getDefaultRating());
        records.put(player.getUniqueId(), record);

        save(player);
    }

    @Override
    public void removeRecord(MCOfflinePlayer player) {
        records.remove(player.getUniqueId());

        save(player);
    }

    @Override
    public DeathMessageManager getDeathMessageManager() {
        return messageManager;
    }

    @Override
    public RecapManager getRecapManager() {
        return recapManager;
    }

    @Override
    public RatingCalculator getRatingCalculator() {
        return calculator;
    }

    @Override
    public void flush() {

    }

    @Override
    public void save(MCOfflinePlayer player) {
        sql.save(records.get(player.getUniqueId()));
    }

    @Override
    public void saveAll() {
        sql.saveAll(records.values().toArray(new Record[records.size()]));
    }
}
