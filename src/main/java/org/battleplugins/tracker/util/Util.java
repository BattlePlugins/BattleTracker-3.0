package org.battleplugins.tracker.util;

/**
 * Holds utility methods for BattleTracker.
 *
 * @author Redned
 */
public class Util {

    /**
     * Returns the formatted entity name from the given string
     *
     * @param name the formatted name
     * @return the formatted entity name
     */
    public static String getFormattedEntityName(String name, boolean capitalizeFirst) {
        switch (name) {
            case "EVOCATION_FANGS":
                name = "EVOKER_FANGS";
                break;
            case "EVOCATION_ILLAGER":
                name = "EVOKER";
                break;
            case "ILLUSION_ILLAGER":
                name = "ILLUSIONER";
                break;
            case "PIG_ZOMBIE":
                name = "ZOMBIE_PIGMAN";
                break;
            case "VINDICATION_ILLAGER":
                name = "VINDICATOR";
                break;
        }
        name = name.toLowerCase();
        name = name.replace(" ", "_");
        if (capitalizeFirst)
            name = capitalizeFirst(name);


        return name;
    }

    /**
     * Capitalizes the first letter of each word
     * in a string
     *
     * @param string the string to capitalizeFirst
     * @return a string with the first letter of every word capitalized
     */
    public static String capitalizeFirst(String string) {
        String[] words = string.split(" ");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
        }

        return String.join(" ", words);
    }
}
