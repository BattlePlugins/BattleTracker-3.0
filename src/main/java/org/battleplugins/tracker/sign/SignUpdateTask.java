package org.battleplugins.tracker.sign;

import lombok.AllArgsConstructor;

import mc.alk.battlecore.util.Log;
import org.battleplugins.api.world.World;
import org.battleplugins.api.world.block.entity.BlockEntity;
import org.battleplugins.api.world.block.entity.Sign;

import java.util.Optional;

@AllArgsConstructor
public class SignUpdateTask implements Runnable {

    private SignManager signManager;

    @Override
    public void run() {
        for (LeaderboardSign sign : signManager.getSigns().values()) {
            World world = sign.getLocation().getWorld();
            Optional<BlockEntity> blockEntity = world.getBlockEntityAt(sign.getLocation());
            if (!blockEntity.isPresent() || !world.isType(blockEntity.get(), Sign.class)) {
                Log.debug("Block at " + sign.getLocation() + " is not a sign!");
                continue;
            }
            Sign mcsign = world.toType(blockEntity.get(), Sign.class);
            signManager.refreshSignContent(mcsign);
        }
    }
}
