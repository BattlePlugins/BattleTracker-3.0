package org.battleplugins.tracker.sql;

import mc.alk.battlecore.serializers.SQLSerializer;
import mc.alk.battlecore.util.Log;
import org.battleplugins.tracker.TrackerInterface;
import org.battleplugins.tracker.stat.StatType;
import org.battleplugins.tracker.stat.record.PlayerRecord;
import org.battleplugins.tracker.stat.record.Record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main SQL storage instance for Trackers
 *
 * @author alkarinv, Redned
 */
public class SQLInstance extends SQLSerializer {

    public static String TABLE_PREFIX;

    public static String DATABASE;
    public static String URL;
    public static String PORT;
    public static String USERNAME;
    public static String PASSWORD;

    public static String TYPE;

    private static final int MAX_LENGTH = 100;

    private String overallTable;
    private String tallyTable;
    private String versusTable;

    private String createOverall;
    private String insertOverall;

    private TrackerInterface tracker;

    private List<String> overallColumns;

    public SQLInstance(TrackerInterface tracker) {
        this(tracker, Stream.of(StatType.values()).filter(StatType::isTracking).map(StatType::getInternalName).collect(Collectors.toList()));
    }

    public SQLInstance(TrackerInterface tracker, List<String> columns) {
        this.overallColumns = columns;

        setTables(tracker);
    }

    public void setTables(TrackerInterface tracker) {
       this.tracker = tracker;

       this.overallTable = TABLE_PREFIX + tracker.getName().toLowerCase() + "_overall";
       this.tallyTable = TABLE_PREFIX + tracker.getName().toLowerCase() + "_tally";
       this.versusTable = TABLE_PREFIX + tracker.getName().toLowerCase() + "_versus";

       init();
    }

    @Override
    protected boolean init() {
        setDB(DATABASE);
        setType(SQLType.valueOf(TYPE.toUpperCase()));
        setURL(URL);
        setPort(PORT);
        setUsername(USERNAME);
        setPassword(PASSWORD);

        super.init();

        String createOverall = "CREATE TABLE IF NOT EXISTS " + overallTable + " ("
                + "name VARCHAR(" + MAX_LENGTH + "), id VARCHAR(" + MAX_LENGTH + "), ";

        StringBuilder createStringBuilder = new StringBuilder();
        createStringBuilder.append(createOverall);
        for (int i = 0; i < overallColumns.size(); i++) {
            String column = overallColumns.get(i);
            createStringBuilder.append(column + " VARCHAR(" + MAX_LENGTH + "), ");
        }

        createStringBuilder.append(" PRIMARY KEY (id))");
        this.createOverall = createStringBuilder.toString();

        String insertOverall = "INSERT INTO " + overallTable + " VALUES (";
        StringBuilder insertStringBuilder = new StringBuilder();
        insertStringBuilder.append(insertOverall);
        for (int i = 0; i < overallColumns.size(); i++) {
            String column = overallColumns.get(i);

            insertStringBuilder.append(column);
            if ((i + 1) < overallColumns.size())
                insertStringBuilder.append("?, ");
            else
                insertStringBuilder.append("?)");
        }

        this.insertOverall = insertStringBuilder.toString();

        try {
            createTable(overallTable, createStringBuilder.toString());
        } catch (Exception ex) {
            Log.err("Failed to create tables!");
            ex.printStackTrace();
        }

        try {
            RSCon rsCon = executeQuery("SELECT * FROM " + overallTable);
            createRecords(rsCon).whenComplete((records, exception) -> {
                for (Record record : records) {
                    tracker.getRecords().put(UUID.fromString(record.getID()), record);
                }
            });
        } catch (Exception ex) {
            Log.err("Failed to generate info from tables!");
            ex.printStackTrace();
        }
        return true;
    }

    public CompletableFuture<List<Record>> createRecords(RSCon rsCon) {
        CompletableFuture<List<Record>> future = new CompletableFuture<>();
        List<Record> records = new ArrayList<>();
        if (rsCon == null) {
            future.complete(records);
            return future;
        }

        try {
            ResultSet resultSet = rsCon.rs;
            while (resultSet.next()) {
                records.add(createRecord(rsCon).get());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(rsCon);
        }

        future.complete(records);
        return future;
    }

    public CompletableFuture<Record> createRecord(RSCon rsCon) throws SQLException {
        CompletableFuture<Record> future = new CompletableFuture<>();

        ResultSet resultSet = rsCon.rs;
        Map<String, Float> columns = new HashMap<>();
        for (String column : overallColumns) {
            columns.put(column, Float.parseFloat(resultSet.getString(column)));
        }

        future.complete(new PlayerRecord(tracker, resultSet.getString("id"), resultSet.getString("name"), columns));
        return future;
    }

    public CompletableFuture<Record> getRecord(UUID id) {
        CompletableFuture<Record> future = new CompletableFuture<>();
        RSCon rsCon = executeQuery("SELECT * FROM " + overallTable + " WHERE id = ?", id.toString());
        try {
            ResultSet resultSet = rsCon.rs;
            while (resultSet.next()){
                return createRecord(rsCon);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(rsCon);
        }

        return future;
    }

    public void save(Record record) {
        saveAll(record);
    }

    public void saveAll(Record... records) {
        saveTotals(records);

        // TODO: Implement individual record saving
        /**
        for (Record record : records) {
            try {

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        **/
    }

    public void saveTotals(Record... records) {
        if (records == null || records.length == 0)
            return;

        List<List<Object>> batch = new ArrayList<>();
        for (Record record : records) {
            if (record.getRating() < 0 || record.getRating() > 200000)
                Log.err("ELO out of range: " + record.getRating() + " with record " + record);

            // +2 in array for name and id
            Object[] objectArray = new Object[overallColumns.size() + 2];
            objectArray[0] = record.getName();
            objectArray[1] = record.getID();
            for (int i = 0; i < overallColumns.size(); i++) {
                String overallColumn = overallColumns.get(i);
                objectArray[i + 2] = record.getStats().get(overallColumn);
            }

            batch.add(Arrays.asList(objectArray));
            executeBatch(true, constructInsertOverallStatement(), batch);
        }
    }

    private String constructInsertOverallStatement() {
        StringBuilder builder = new StringBuilder();
        switch (getType()) {
            case MYSQL:
                String insertOverall = "INSERT INTO " + overallTable + " VALUES (?, ?, ";
                builder.append(insertOverall);
                for (int i = 0; i < overallColumns.size(); i++) {
                    if ((i + 1) < overallColumns.size())
                        builder.append("?, ");
                    else
                        builder.append("?)");
                }

                builder.append(" ON DUPLICATE KEY UPDATE ");
                builder.append("name = VALUES(name), ");
                builder.append("id = VALUES(id), ");
                for (int i = 0; i < overallColumns.size(); i++) {
                    if ((i + 1) < overallColumns.size())
                        builder.append(overallColumns.get(i) + " = VALUES(" + overallColumns.get(i) + "), ");
                    else
                        builder.append(overallColumns.get(i) + " = VALUES(" + overallColumns.get(i) + ")");
                }
                break;
            case SQLITE:
                builder.append("INSERT OR REPLACE INTO " + overallTable + " VALUES (");
                builder.append("?, ");
                builder.append("?, ");
                for (int i = 0; i < overallColumns.size(); i++) {
                    if ((i + 1) < overallColumns.size())
                        builder.append("?, ");
                    else
                        builder.append("?)");
                }
                break;
        }

        return builder.toString();
    }

    private String constructUpdateOverallStatement() {
        String insertOverall = "UPDATE " + overallTable + " SET name = ?, id = ?, ";
        StringBuilder builder = new StringBuilder();
        builder.append(insertOverall);
        for (int i = 0; i < overallColumns.size(); i++) {
            if ((i + 1) < overallColumns.size())
                builder.append(overallColumns.get(i) + " = ?, ");
            else
                builder.append(overallColumns.get(i) + " = ? WHERE id = ?");
        }

        return builder.toString();
    }

    public List<String> getOverallColumns() {
        return overallColumns;
    }
}
