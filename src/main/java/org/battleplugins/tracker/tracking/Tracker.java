package org.battleplugins.tracker.tracking;

import lombok.AccessLevel;
import lombok.Getter;

import mc.alk.battlecore.configuration.Configuration;
import mc.alk.mc.MCOfflinePlayer;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.sql.SQLInstance;
import org.battleplugins.tracker.tracking.message.DeathMessageManager;
import org.battleplugins.tracker.tracking.recap.RecapManager;
import org.battleplugins.tracker.tracking.stat.StatTypes;
import org.battleplugins.tracker.tracking.stat.calculator.RatingCalculator;
import org.battleplugins.tracker.tracking.stat.record.PlayerRecord;
import org.battleplugins.tracker.tracking.stat.record.Record;
import org.battleplugins.tracker.tracking.stat.tally.VersusTally;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Main implementation of a tracker instance. Any plugin
 * wanting to track data should extend this class or
 * use it as the implementation.
 *
 * @author Redned
 */
@Getter
public class Tracker implements TrackerInterface {

    protected String name;

    protected DeathMessageManager deathMessageManager;
    protected RecapManager recapManager;
    protected RatingCalculator ratingCalculator;

    protected Map<UUID, Record> records;
    protected List<VersusTally> versusTallies;

    @Getter(AccessLevel.NONE)
    protected SQLInstance sql;

    public Tracker(BattleTracker plugin, String name, Configuration config, RatingCalculator calculator) {
        this(plugin, name, config, calculator, null);
    }

    public Tracker(BattleTracker plugin, String name, Configuration config, RatingCalculator calculator, SQLInstance sqlInstance) {
        this.name = name;
        this.recapManager = new RecapManager(plugin);
        this.deathMessageManager = new DeathMessageManager(plugin, this, config);
        this.ratingCalculator = calculator;
        this.records = new HashMap<>();
        this.versusTallies = new ArrayList<>();
        if (sqlInstance == null) {
            sqlInstance = new SQLInstance(this);
        }
        this.sql = sqlInstance;
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
    public boolean hasVersusTally(MCOfflinePlayer player) {
        for (VersusTally tally : versusTallies) {
            if (tally.getId1().equals(player.getUniqueId().toString()))
                return true;

            if (tally.getId2().equals(player.getUniqueId().toString()))
                return true;
        }

        return false;
    }

    @Override
    public VersusTally getVersusTally(MCOfflinePlayer player1, MCOfflinePlayer player2) {
        for (VersusTally tally : versusTallies) {
            if (tally.getId1().equals(player1.getUniqueId().toString()) &&
                    tally.getId2().equals(player2.getUniqueId().toString())) {

                return tally;
            }

            if (tally.getId2().equals(player1.getUniqueId().toString()) &&
                    tally.getId1().equals(player2.getUniqueId().toString())) {

                return tally;
            }
        }

        return null;
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
        ratingCalculator.updateRating(killerRecord, killedRecord, tie);

        float killerRating = killerRecord.getRating();
        float killerMaxRating = killerRecord.getStat(StatTypes.MAX_RATING);

        setValue(StatTypes.RATING, killerRecord.getRating(), killer);
        setValue(StatTypes.RATING, killedRecord.getRating(), killed);

        if (killerRating > killerMaxRating)
            setValue(StatTypes.MAX_RATING, killerRating, killer);

        if (tie) {
            incrementValue(StatTypes.TIES, killer);
            incrementValue(StatTypes.TIES, killed);
        }

        setValue(StatTypes.KD_RATIO, killerRecord.getStat(StatTypes.KILLS) / killerRecord.getStat(StatTypes.DEATHS), killer);
        setValue(StatTypes.KD_RATIO, killedRecord.getStat(StatTypes.KILLS) / killedRecord.getStat(StatTypes.DEATHS), killed);

        float killerKdr = killerRecord.getStat(StatTypes.KD_RATIO);
        float killerMaxKdr = killerRecord.getStat(StatTypes.MAX_KD_RATIO);

        if (killerKdr > killerMaxKdr)
            setValue(StatTypes.MAX_KD_RATIO, killerKdr, killer);

        setValue(StatTypes.STREAK, 0, killed);
        incrementValue(StatTypes.STREAK, killer);

        float killerStreak = killerRecord.getStat(StatTypes.STREAK);
        float killerMaxStreak = killerRecord.getStat(StatTypes.MAX_STREAK);

        if (killerStreak > killerMaxStreak)
            setValue(StatTypes.MAX_STREAK, killerStreak, killer);
    }

    @Override
    public void createNewRecord(MCOfflinePlayer player) {
        Map<String, Float> columns = new HashMap<>();
        for (String column : sql.getOverallColumns()) {
            columns.put(column, 0f);
        }

        Record record = new PlayerRecord(this, player.getUniqueId().toString(), player.getName(), columns);
        createNewRecord(player, record);
    }

    @Override
    public void createNewRecord(MCOfflinePlayer player, Record record) {
        record.setRating(ratingCalculator.getDefaultRating());
        records.put(player.getUniqueId(), record);

        save(player);
    }

    @Override
    public void removeRecord(MCOfflinePlayer player) {
        records.remove(player.getUniqueId());

        save(player);
    }

    @Override
    public void save(MCOfflinePlayer player) {
        sql.save(player.getUniqueId());
    }

    @Override
    public void saveAll() {
        sql.saveAll();
    }
}
