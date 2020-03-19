package org.battleplugins.tracker.tracking.stat;

import lombok.Builder;
import lombok.Getter;

/**
 * A statistic type.
 *
 * @author Redned
 */
@Builder
@Getter
public class StatType {

    private String internalName;

    /**
     * The name of the stat type
     *
     * @return the name of the stat type
     */
    private String name;

    /**
     * If the stat type should be tracked/
     * stored inside of the database
     *
     * @return if the stat type should be tracked
     */
    @Builder.Default
    private boolean tracked = true;

    StatType(String internalName, String name, boolean tracked) {
        this.internalName = internalName;
        this.name = name;
        this.tracked = tracked;

        StatTypes.statTypes.add(this);
    }

    /**
     * Returns the internal name of the stat type, this is
     * what's used when storing data in a database
     *
     * @return the internal name of the stat type
     */
    public String getInternalName() {
        if (internalName == null || internalName.isEmpty())
            return name.replace(" ", "_").toLowerCase();

        return internalName;
    }
}
