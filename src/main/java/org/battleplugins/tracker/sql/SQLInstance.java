package org.battleplugins.tracker.sql;

import mc.alk.battlecore.serializers.SQLSerializer;
import mc.alk.battlecore.util.Log;
import mc.alk.mc.scheduler.Scheduler;
import org.battleplugins.tracker.tracking.TrackerInterface;
import org.battleplugins.tracker.tracking.stat.StatType;
import org.battleplugins.tracker.tracking.stat.record.PlayerRecord;
import org.battleplugins.tracker.tracking.stat.record.Record;
import org.battleplugins.tracker.tracking.stat.tally.VersusTally;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main SQL config instance for Trackers.
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

    private TrackerInterface tracker;

    private List<String> overallColumns;
    private List<String> versusColumns;

    public SQLInstance(TrackerInterface tracker) {
        this(tracker, Stream.of(StatType.values()).filter(StatType::isTracking).map(StatType::getInternalName).collect(Collectors.toList()),
                Arrays.asList(StatType.KILLS.getInternalName(), StatType.DEATHS.getInternalName(), StatType.TIES.getInternalName()));
    }

    public SQLInstance(TrackerInterface tracker, List<String> overallColumns, List<String> versusColumns) {
        this.overallColumns = overallColumns;
        this.versusColumns = versusColumns;

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

        setupOverallTable();
        setupVersusTable();

        try {
            // TODO: Don't put all records in cache and add a way to flush
            RSCon overallRsCon = executeQuery("SELECT * FROM " + overallTable);
            createRecords(overallRsCon).whenComplete((records, exception) -> Scheduler.scheduleAsynchrounousTask(() ->
                    records.forEach(record -> tracker.getRecords().put(UUID.fromString(record.getID()), record))));

            // TODO: Don't put all records in cache and add a way to flush
            RSCon versusRsCon = executeQuery("SELECT * FROM " + versusTable);
            createVersusTallies(versusRsCon).whenComplete((tallies, exception ) -> Scheduler.scheduleAsynchrounousTask(() ->
                    tallies.forEach(tally -> {
                        tracker.getVersusTallies().add(tally);
                    })));
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

    public CompletableFuture<List<VersusTally>> createVersusTallies(RSCon rsCon) {
        CompletableFuture<List<VersusTally>> future = new CompletableFuture<>();
        List<VersusTally> tallies = new ArrayList<>();
        if (rsCon == null) {
            future.complete(tallies);
            return future;
        }

        try {
            ResultSet resultSet = rsCon.rs;
            while (resultSet.next()) {
                tallies.add(createVersusTally(rsCon).get());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(rsCon);
        }

        future.complete(tallies);
        return future;
    }

    public CompletableFuture<VersusTally> createVersusTally(RSCon rsCon) throws SQLException {
        CompletableFuture<VersusTally> future = new CompletableFuture<>();

        ResultSet resultSet = rsCon.rs;
        Map<String, Float> columns = new HashMap<>();
        for (String column : versusColumns) {
            if (column.equalsIgnoreCase("infinity")) { // sometimes kdr gets saved as 'infinity'
                columns.put(column, Float.POSITIVE_INFINITY);
                continue;
            }

            columns.put(column, Float.parseFloat(resultSet.getString(column)));
        }

        future.complete(new VersusTally(tracker,
                resultSet.getString("id1"),
                resultSet.getString("id2"),
                resultSet.getString("name1"),
                resultSet.getString("name2"),
                columns));

        System.out.println("Created vs tally with " + resultSet.getString("name1") + " with columns " + columns);
        return future;
    }

    public CompletableFuture<Record> getVersusTally(UUID id1, UUID id2) {
        CompletableFuture<Record> future = new CompletableFuture<>();
        // We need to check if id1 is in place of id2 and vice versa
        RSCon rsCon = executeQuery("SELECT * FROM " + versusTable + " WHERE (id1 = ? AND id2 = ?) OR (id1 = ? AND id2 = ?)", id1.toString(), id2.toString(), id2.toString(), id1.toString());
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

    public void save(UUID uuid) {
        saveTotals(new UUID[]{uuid});
    }

    public void saveAll() {
        saveTotals(tracker.getRecords().keySet().toArray(new UUID[0]));
    }

    public void saveTotals(UUID[] uuids) {
        if (uuids == null || uuids.length == 0)
            return;

        List<List<Object>> overallBatch = new ArrayList<>();
        List<List<Object>> versusBatch = new ArrayList<>();
        for (UUID uuid : uuids) {
            Record record = tracker.getRecords().get(uuid);
            if (record.getRating() < 0 || record.getRating() > 200000)
                Log.err("ELO out of range: " + record.getRating() + " with record " + record);

            // +2 in array for name and id
            String[] overallObjectArray = new String[overallColumns.size() + 2];
            overallObjectArray[0] = record.getName();
            overallObjectArray[1] = record.getID();
            for (int i = 0; i < overallColumns.size(); i++) {
                String overallColumn = overallColumns.get(i);
                overallObjectArray[i + 2] = record.getStats().get(overallColumn).toString();
            }

            overallBatch.add(Arrays.asList(overallObjectArray));
            executeBatch(true, constructInsertOverallStatement(), overallBatch);

            for (VersusTally versusTally : tracker.getVersusTallies()) {
                if (!versusTally.getId1().equals(uuid.toString()) && !versusTally.getId2().equals(uuid.toString()))
                    continue;

                // +4 in array for double name and id
                String[] versusObjectArray = new String[versusColumns.size() + 4];
                versusObjectArray[0] = versusTally.getId1();
                versusObjectArray[1] = versusTally.getName1();
                versusObjectArray[2] = versusTally.getId2();
                versusObjectArray[3] = versusTally.getName2();

                for (int i = 0; i < versusColumns.size(); i++) {
                    String versusColumn = versusColumns.get(i);
                    versusObjectArray[i + 4] = Optional.ofNullable(versusTally.getStats().get(versusColumn)).orElse(0f).toString();
                }

                versusBatch.add(Arrays.asList(versusObjectArray));
                executeBatch(true, constructInsertVersusStatement(), versusBatch);
            }
        }
    }

    public List<String> getOverallColumns() {
        return overallColumns;
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
                        builder.append(overallColumns.get(i)).append(" = VALUES(").append(overallColumns.get(i)).append("), ");
                    else
                        builder.append(overallColumns.get(i)).append(" = VALUES(").append(overallColumns.get(i)).append(")");
                }
                break;
            case SQLITE:
                builder.append("INSERT OR REPLACE INTO ").append(overallTable).append(" VALUES (");
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

    private String constructInsertVersusStatement() {
        StringBuilder builder = new StringBuilder();
        switch (getType()) {
            case MYSQL:
                String insertOverall = "INSERT INTO " + versusTable + " VALUES (?, ?, ?, ?, ";
                builder.append(insertOverall);
                for (int i = 0; i < versusColumns.size(); i++) {
                    if ((i + 1) < versusColumns.size())
                        builder.append("?, ");
                    else
                        builder.append("?)");
                }

                builder.append(" ON DUPLICATE KEY UPDATE ");
                builder.append("id1 = VALUES(id1), ");
                builder.append("name1 = VALUES(name1), ");
                builder.append("id2 = VALUES(id2), ");
                builder.append("name2 = VALUES(name2), ");
                for (int i = 0; i < versusColumns.size(); i++) {
                    if ((i + 1) < versusColumns.size())
                        builder.append(versusColumns.get(i)).append(" = VALUES(").append(versusColumns.get(i)).append("), ");
                    else
                        builder.append(versusColumns.get(i)).append(" = VALUES(").append(versusColumns.get(i)).append(")");
                }
                break;
            case SQLITE:
                builder.append("INSERT OR REPLACE INTO ").append(versusTable).append(" VALUES (");
                builder.append("?, ");
                builder.append("?, ");
                builder.append("?, ");
                builder.append("?, ");
                for (int i = 0; i < versusColumns.size(); i++) {
                    if ((i + 1) < versusColumns.size())
                        builder.append("?, ");
                    else
                        builder.append("?)");
                }
                break;
        }

        return builder.toString();
    }

    private void setupOverallTable() {
        String createOverall = "CREATE TABLE IF NOT EXISTS " + overallTable + " ("
                + "name VARCHAR(" + MAX_LENGTH + "), id VARCHAR(" + MAX_LENGTH + "), ";

        StringBuilder createStringBuilder = new StringBuilder();
        createStringBuilder.append(createOverall);
        for (int i = 0; i < overallColumns.size(); i++) {
            String column = overallColumns.get(i);
            createStringBuilder.append(column).append(" VARCHAR(").append(MAX_LENGTH).append("), ");
        }

        createStringBuilder.append(" PRIMARY KEY (id))");

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

        try {
            createTable(overallTable, createStringBuilder.toString());
        } catch (Exception ex) {
            Log.err("Failed to create tables!");
            ex.printStackTrace();
        }
    }

    private void setupVersusTable() {
        String createVersus = "CREATE TABLE IF NOT EXISTS " + versusTable + "(" +
                "id1 VARCHAR (" + MAX_LENGTH + ") NOT NULL," +
                "name1 VARCHAR (" + MAX_LENGTH + ") NOT NULL," +
                "id2 VARCHAR (" + MAX_LENGTH + ") NOT NULL, " +
                "name2 VARCHAR (" + MAX_LENGTH + ") NOT NULL,";

        StringBuilder createStringBuilder = new StringBuilder();
        createStringBuilder.append(createVersus);
        for (int i = 0; i < versusColumns.size(); i++) {
            String column = versusColumns.get(i);
            createStringBuilder.append(column).append(" VARCHAR(").append(MAX_LENGTH).append("), ");
        }

        createStringBuilder.append(" PRIMARY KEY (id1))");

        String insertOverall = "INSERT INTO " + versusTable + " VALUES (";
        StringBuilder insertStringBuilder = new StringBuilder();
        insertStringBuilder.append(insertOverall);
        for (int i = 0; i < versusColumns.size(); i++) {
            String column = versusColumns.get(i);

            insertStringBuilder.append(column);
            if ((i + 1) < versusColumns.size())
                insertStringBuilder.append("?, ");
            else
                insertStringBuilder.append("?)");
        }

        try {
            createTable(versusTable, createStringBuilder.toString());
        } catch (Exception ex) {
            Log.err("Failed to create tables!");
            ex.printStackTrace();
        }
    }
}
