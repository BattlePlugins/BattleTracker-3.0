package org.battleplugins.tracker.sign;

import lombok.AllArgsConstructor;

import mc.alk.battlecore.util.Log;
import mc.alk.mc.MCWorld;
import mc.alk.mc.block.MCSign;

@AllArgsConstructor
public class SignUpdateTask implements Runnable {

    private SignManager signManager;

    @Override
    public void run() {
        for (LeaderboardSign sign : signManager.getSigns().values()) {
            MCWorld world = sign.getLocation().getWorld();
            if (!world.isType(world.getBlockAt(sign.getLocation()), MCSign.class)) {
                Log.debug("Block at " + sign.getLocation() + " is not a sign!");
                continue;
            }
            MCSign mcsign = world.toType(world.getBlockAt(sign.getLocation()), MCSign.class);
            signManager.refreshSignContent(mcsign);
        }
    }
}
