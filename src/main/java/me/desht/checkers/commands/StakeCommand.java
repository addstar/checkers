package me.desht.checkers.commands;

import me.desht.checkers.CheckersException;
import me.desht.checkers.CheckersPlugin;
import me.desht.checkers.Messages;
import me.desht.checkers.game.CheckersGame;
import me.desht.checkers.game.CheckersGameManager;
import me.desht.checkers.util.CheckersUtils;
import me.desht.dhutils.MiscUtil;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class StakeCommand extends AbstractCheckersCommand {
	public StakeCommand() {
		super("checkers stake", 1, 1);
		setPermissionNode("checkers.commands.stake");
		setUsage("/<command> stake <amount>");
	}

	@Override
	public boolean execute(Plugin plugin, CommandSender player, String[] args) {
		if (((CheckersPlugin) plugin).getEconomy() == null) {
			return true;
		}

		String stakeStr = args[0];
		try {
			CheckersGame game = CheckersGameManager.getManager().getCurrentGame(player.getName(), true);
			double amount = Double.parseDouble(stakeStr);
			game.setStake(player.getName(), amount);
			MiscUtil.statusMessage(player, Messages.getString("Game.stakeChanged", CheckersUtils.formatStakeStr(amount)));
		} catch (NumberFormatException e) {
			throw new CheckersException(Messages.getString("Misc.invalidNumeric", stakeStr));
		}
		return true;
	}

}
