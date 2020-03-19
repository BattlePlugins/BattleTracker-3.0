package org.battleplugins.tracker.util;

import org.battleplugins.api.Platform;
import org.battleplugins.api.PlatformTypes;
import org.battleplugins.tracker.BattleTracker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Utility class used for downloading dependencies
 * off the internet.
 *
 * @author Redned
 */
public class DependencyUtil {

    private static File libFolder;

    private static URLClassLoader classLoader = ((URLClassLoader) ClassLoader.getSystemClassLoader());
    private static Method method;

    private static String MYSQL_NAME = "MySQL";
    private static String SQLITE_NAME = "SQLite";

    private static final String MYSQL_DOWNLOAD = "https://repo.md-5.net/content/repositories/central/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar";
    private static final String SQLITE_DOWNLOAD = "https://repo.md-5.net/content/repositories/central/org/xerial/sqlite-jdbc/3.28.0/sqlite-jdbc-3.28.0.jar";

    static {
        try {
            method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            BattleTracker.getInstance().getLogger().severe("Failed to initialize URLClassLoader, dependencies will not be loaded!");
            ex.printStackTrace();
        }
    }

    public static CompletableFuture<DownloadResult> downloadDepedencies() {
        CompletableFuture<DownloadResult> future = new CompletableFuture<>();
        CompletableFuture<DownloadResult> sqlFuture = new CompletableFuture<>();
        CompletableFuture<DownloadResult> sqliteFuture = new CompletableFuture<>();

        // Sponge has its own libraries for this, no need to install anything
        if (Platform.getPlatformType() == PlatformTypes.SPONGE) {
            future.complete(DownloadResult.SUCCESS);
            return future;
        }

        File sqlFile = new File(libFolder, MYSQL_NAME + ".jar");
        File sqliteFile = new File(libFolder, SQLITE_NAME + ".jar");

        if (sqlFile.exists() || isClassInPath("com.mysql.jdbc.Driver")) {
            BattleTracker.getInstance().getLogger().info("MySQL was found!");
            if (!isClassInPath("com.mysql.jdbc.Driver"))
                loadDependency(sqlFile);

            sqlFuture.complete(DownloadResult.SUCCESS);
        } else {
            BattleTracker.getInstance().getLogger().info("MySQL was not found... downloading it now.");
            downloadFile(MYSQL_NAME + ".jar", MYSQL_DOWNLOAD, true, sqlFuture);
        }

        if (sqliteFile.exists() || isClassInPath("org.sqlite.JDBC")) {
            BattleTracker.getInstance().getLogger().info("SQLite was found!");
            if (!isClassInPath("org.sqlite.JDBC"))
                loadDependency(sqliteFile);

            sqliteFuture.complete(DownloadResult.SUCCESS);
        } else {
            BattleTracker.getInstance().getLogger().info("SQLite was not found... downloading it now.");
            downloadFile(SQLITE_NAME + ".jar", SQLITE_DOWNLOAD, true, sqliteFuture);
        }

        if (sqlFuture.isDone() && sqliteFuture.isDone()) {
            future.complete(DownloadResult.SUCCESS);
            return future;
        }

        sqlFuture.whenComplete((result, action) -> {
            switch (result) {
                case SUCCESS:
                    try {
                        if (sqliteFuture.isDone() && sqliteFuture.get() == DownloadResult.SUCCESS) {
                            future.complete(DownloadResult.SUCCESS);
                            return;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
                case FAILURE:
                case INVALID_URL:
                    BattleTracker.getInstance().getLogger().severe("MySQL could not be downloaded!");
                    future.complete(result);
                    break;
            }
        });

        sqliteFuture.whenComplete((result, action) -> {
            switch (result) {
                case SUCCESS:
                    try {
                        if (sqlFuture.isDone() && sqlFuture.get() == DownloadResult.SUCCESS) {
                            future.complete(DownloadResult.SUCCESS);
                            return;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
                case FAILURE:
                case INVALID_URL:
                    BattleTracker.getInstance().getLogger().severe("SQLite could not be downloaded!");
                    future.complete(result);
                    break;
            }
        });

        return future;
    }

    /**
     * Loads the jar file into the classpath
     *
     * @param jarFile the file to load in
     */
    public static void loadDependency(File jarFile) {
        try {
            method.invoke(classLoader, jarFile.toURI().toURL());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Downloads a file from the specified URL into the library folder.
     *
     * @param file the name of the file to save it as
     * @param link the url of the file
     * @param loadToClasspath if the jar should be loaded into the classpath
     * @param future the future to complete to upon download
     */
    private static void downloadFile(String file, String link, boolean loadToClasspath, CompletableFuture<DownloadResult> future) {
        if (!libFolder.exists())
            libFolder.mkdir();

        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {
            // Download the file
            URL url = new URL(link);
            in = new BufferedInputStream(url.openStream());
            fout = new FileOutputStream(libFolder.getAbsolutePath() + File.separator + file);

            byte[] data = new byte[1024];
            int count;
            BattleTracker.getInstance().getLogger().info("About to download a dependency at: " + link);
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }
            //Just a quick check to make sure we didn't leave any files from last time...
            for (final File xFile : libFolder.listFiles()) {
                if (xFile.getName().endsWith(".zip")) {
                    xFile.delete();
                }
            }
            // Check to see if it's a zip file, if it is, unzip it.
            File dFile = new File(libFolder.getAbsolutePath() + File.separator + file);
            if (dFile.getName().endsWith(".zip")) {
                // Unzip
                unzip(dFile.getCanonicalPath());
            }

            if (loadToClasspath)
                loadDependency(new File(libFolder, file));

            BattleTracker.getInstance().getLogger().info("Finished downloading jar.");
        } catch (FileNotFoundException ex) {
            future.complete(DownloadResult.INVALID_URL);
            BattleTracker.getInstance().getLogger().warning("The dependency downloader tried to download a jar, but was unsuccessful (invalid library folder?).");
        } catch (MalformedURLException ex) {
            future.complete(DownloadResult.INVALID_URL);
            BattleTracker.getInstance().getLogger().warning("The dependency downloader tried to download a jar, but was unsuccessful (invalid download link?).");
        } catch (IOException ex) {
            future.complete(DownloadResult.FAILURE);
            BattleTracker.getInstance().getLogger().warning("The dependency downloader tried to download a jar, but was unsuccessful.");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (fout != null) {
                    fout.close();
                }
            } catch (Exception ex) {
                /* do nothing */
            }
        }

        future.complete(DownloadResult.SUCCESS);
    }

    /**
     * Part of Zip-File-Extractor. Unzips a downloaded
     * file.
     *
     * @param file the location of the file to extract.
     */
    private static void unzip(String file) {
        try {
            File fSourceZip = new File(file);
            String zipPath = file.substring(0, file.length() - 4);
            ZipFile zipFile = new ZipFile(fSourceZip);
            Enumeration<? extends ZipEntry> e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                File destinationFilePath = new File(zipPath, entry.getName());
                destinationFilePath.getParentFile().mkdirs();
                if (entry.isDirectory()) {
                    continue;
                } else {
                    BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
                    int b;
                    byte buffer[] = new byte[1024];
                    FileOutputStream fos = new FileOutputStream(destinationFilePath);
                    BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
                    while ((b = bis.read(buffer, 0, 1024)) != -1) {
                        bos.write(buffer, 0, b);
                    }
                    bos.flush();
                    bos.close();
                    bis.close();
                    String name = destinationFilePath.getName();
                    if (name.endsWith(".jar") && doesFileExist(name)) {
                        destinationFilePath.renameTo(new File(BattleTracker.getInstance().getDataFolder().getParent(), libFolder + File.separator + name));
                    }
                }
            }
            zipFile.close();

            // Move any plugin data folders that were included to the right place, Bukkit won't do this for us.
            for (File dFile : new File(zipPath).listFiles()) {
                if (dFile.isDirectory()) {
                    if (doesFileExist(dFile.getName())) {
                        File oFile = new File(BattleTracker.getInstance().getDataFolder().getParent(), dFile.getName()); // Get current dir
                        File[] contents = oFile.listFiles(); // List of existing files in the current dir
                        for (final File cFile : dFile.listFiles()) { // Loop through all the files in the new dir
                            boolean found = false;
                            for (final File xFile : contents) { // Loop through contents to see if it exists
                                if (xFile.getName().equals(cFile.getName())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                // Move the new file into the current dir
                                cFile.renameTo(new File(oFile.getCanonicalFile() + File.separator + cFile.getName()));
                            } else {
                                // This file already exists, so we don't need it anymore.
                                cFile.delete();
                            }
                        }
                    }
                }
                dFile.delete();
            }
            new File(zipPath).delete();
            fSourceZip.delete();
        } catch (IOException ex) {
            BattleTracker.getInstance().getLogger().severe("The dependency downloader tried to unzip a dependency file, but was unsuccessful.");
            ex.printStackTrace();
        }
        new File(file).delete();
    }

    /**
     * Check if the name of a jar is one of the dependencies currently installed,
     * used for extracting the correct files out of a zip.
     *
     * @param name a name to check for inside the dependencies folder.
     * @return true if a file inside the dependencies folder is named this.
     */
    private static boolean doesFileExist(String name) {
        for (File file : libFolder.listFiles()) {
            if (file.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isClassInPath(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ex) {
            /* do nothing */
        }
        return false;
    }

    /**
     * Sets the lib/dependency folder
     *
     * @param libFolder the lib/dependency folder
     */
    public static void setLibFolder(File libFolder) {
        DependencyUtil.libFolder = libFolder;
    }

    public enum DownloadResult {

        SUCCESS,
        FAILURE,
        INVALID_URL;
    }
}
