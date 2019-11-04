package org.battleplugins.tracker.tracking.stat.calculator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.battleplugins.tracker.tracking.stat.record.Record;

/**
 * Class for calculating for elo.
 *
 * @author alkarin_v, Redned
 */
@Getter
@Setter
@AllArgsConstructor
public class EloCalculator implements RatingCalculator {

    private float defaultRating;

    /**
     * The spread of the rating
     *
     * @param spread the spread
     * @return the spread
     */
    private float spread;

    @Override
    public String getName() {
        return "elo";
    }

    @Override
    public void updateRating(Record killer, Record killed, boolean tie) {
        float result = tie ? 0.5f : 1.0f;
        float eloChange = getEloChange(killer, killed, result);
        if (killer.isTracking())
            killer.setRating(killer.getRating() + eloChange);

        if (killed.isTracking())
            killed.setRating(killed.getRating() - eloChange);
    }

    @Override
    public void updateRating(Record killer, Record[] killed, boolean tie) {
        float result = tie ? 0.5f : 1.0f;
        double eloWinner = 0;
        double dampening = killed.length == 1 ? 1 : killed.length / 2.0D;
        for (Record record : killed) {
            double eloChange = getEloChange(killer, record, result) / dampening;
            eloWinner += eloChange;
            if (record.isTracking())
                record.setRating(record.getRating() - (float) eloChange);
        }
        if (killer.isTracking())
            killer.setRating(killer.getRating() + (float) eloWinner);
    }

    private float getEloChange(Record killer, Record killed, float result) {
        float di = killed.getRating() - killer.getRating();

        float expected = (float) (1f / (1 + Math.pow(10, di/spread)));
        float eloChange = getK(killer.getRating()) * (result - expected);
        return eloChange;
    }

    /**
     * Returns the 'k' value for elo calculations
     * https://en.wikipedia.org/wiki/Elo_rating_system#Most_accurate_K-factor
     *
     * @param elo the elo to take into consideration
     * @return the 'k' value for elo calculations
     */
    private int getK(float elo) {
        if (elo < 1600) {
            return 50;
        } else if (elo < 1800) {
            return 35;
        } else if (elo < 2000) {
            return 20;
        } else if (elo < 2500) {
            return 10;
        }
        return 6;
    }
}
