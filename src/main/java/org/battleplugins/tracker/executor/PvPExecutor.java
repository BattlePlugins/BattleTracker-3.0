package org.battleplugins.tracker.executor;

import mc.alk.battlecore.executor.CustomCommandExecutor;
import mc.alk.mc.command.MCCommandSender;

/**
 * Main executor for the PvP tracker
 *
 * @author Redned
 */
public class PvPExecutor extends CustomCommandExecutor {

    @MCCommand(cmds = "top")
    public void topCommand(MCCommandSender sender) {
        sender.sendMessage("Top player command recieved");
    }
}
