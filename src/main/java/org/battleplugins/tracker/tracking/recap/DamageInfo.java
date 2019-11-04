package org.battleplugins.tracker.tracking.recap;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Stores more information about a specific damage.
 *
 * @author Redned
 */
@Getter
@RequiredArgsConstructor
public class DamageInfo {

    /**
     * The cause of this damage
     *
     * @return the cause of this damage
     */
    @NonNull
    private final String cause;

    /**
     * The amount of damage dealt
     *
     * @return the amount of damage dealt
     */
    private final double damage;

    /**
     * When this damage info was logged
     *
     * @return when this damage info was logged
     */
    private long logTime = System.currentTimeMillis();
}
