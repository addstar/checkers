package me.desht.checkers.player;

import me.desht.checkers.CheckersException;
import me.desht.checkers.CheckersPlugin;
import me.desht.checkers.Messages;
import me.desht.checkers.TimeControl.ControlType;
import me.desht.checkers.TwoPlayerClock;
import me.desht.checkers.game.CheckersGame;
import me.desht.checkers.model.*;
import me.desht.checkers.responses.DrawResponse;
import me.desht.checkers.responses.SwapResponse;
import me.desht.checkers.responses.UndoResponse;
import me.desht.checkers.responses.YesNoResponse;
import me.desht.checkers.util.CheckersUtils;
import me.desht.checkers.view.BoardView;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.cuboid.Cuboid.CuboidDirection;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class HumanCheckersPlayer extends CheckersPlayer {

	private Player player;
	private int tcWarned = 0;

	public HumanCheckersPlayer(String name, CheckersGame game, PlayerColour colour) {
		super(name, game, colour);
	}

	private Player getBukkitPlayer() {
		if (player == null) {
			player = Bukkit.getPlayerExact(getName());
		} else {
			if (!player.isOnline()) {
				player = null;
			}
		}

		return player;
	}

	@Override
	public String getDisplayName() {
		return ChatColor.GOLD + getName() + ChatColor.RESET;
	}

	@Override
	public void validateAffordability(String error) {
		if (error == null) {
			error = "Game.cantAffordToJoin";
		}
		double stake = getGame().getStake();
		Economy economy = CheckersPlugin.getInstance().getEconomy();
		if (economy != null && !economy.has(getName(), stake)) {
			throw new CheckersException(Messages.getString(error, CheckersUtils.formatStakeStr(stake)));
		}
	}

	@Override
	public void validateInvited(String error) {
		String invited = getGame().getInvited();
		if (!invited.equals(CheckersGame.OPEN_INVITATION) && !invited.equalsIgnoreCase(getName())) {
			throw new CheckersException(Messages.getString(error));
		}
	}

	@Override
	public void promptForFirstMove() {
		alert(Messages.getString("Game.started", getColour().getDisplayColour(), CheckersUtils.getWandDescription()));
	}

	@Override
	public void promptForNextMove() {
		Player player = getBukkitPlayer();
		if (player != null) {
			Move m = getGame().getPosition().getLastMove();
			if (m != null) {
				int size = getGame().getPosition().getRules().getBoardSize();
				int from = m.getFrom().toCheckersNotation(size);
				int to = m.getTo().toCheckersNotation(size);
				alert(Messages.getString("Game.playerPlayedMove",
						getColour().getOtherColour().getDisplayColour(), from, to, getColour().getDisplayColour()));
				maybeAutoSelect();
			}
		}
	}

	private void maybeAutoSelect() {
		boolean doAutoSelect = true;
		RowCol autoSelectSquare = null;
		Position position = getGame().getPosition();
		if (position.getLegalMoves().length > 0 && position.getLegalMoves()[0].isJump()) {
			// a jump is required; see if only one piece can move, and if so, auto-select it
			for (Move m : position.getLegalMoves()) {
				if (autoSelectSquare != null && !autoSelectSquare.equals(m.getFrom())) {
					doAutoSelect = false;
					break;
				} else {
					autoSelectSquare = m.getFrom();
				}
			}
		}
		if (doAutoSelect) {
			getGame().selectSquare(autoSelectSquare);
		}
	}

	@Override
	public void promptForContinuedMove() {
		alert(Messages.getString("Game.mustContinueJumping"));
	}

	@Override
	public void alert(String message) {
		Player player = getBukkitPlayer();
		if (player != null) {
			MiscUtil.alertMessage(player, Messages.getString("Game.alertPrefix", getGame().getName()) + message);
		}
	}

	@Override
	public void statusMessage(String message) {
		Player player = getBukkitPlayer();
		if (player != null) {
			MiscUtil.statusMessage(player, message);
		}
	}

//	@Override
//	public void replayMoves() {
//		// nothing to do here
//	}

	@Override
	public void cleanup() {
		player = null;
	}

	@Override
	public boolean isHuman() {
		return true;
	}

	@Override
	public void withdrawFunds(double amount) {
		Economy economy = CheckersPlugin.getInstance().getEconomy();
		economy.withdrawPlayer(getName(), amount);
		alert(Messages.getString("Game.stakePaid", CheckersUtils.formatStakeStr(amount)));
	}

	@Override
	public void depositFunds(double amount) {
		Economy economy = CheckersPlugin.getInstance().getEconomy();
		economy.depositPlayer(getName(), amount);
	}

	@Override
	public void cancelOffers() {
		Player player = getBukkitPlayer();
		if (player != null) {
			// making a move after a draw/swap/undo offer has been made effectively declines the offer
			YesNoResponse.handleYesNoResponse(player, false);
		}
	}

	@Override
	public double getPayoutMultiplier() {
		return 2.0;
	}

	@Override
	public void drawOffered() {
		String offerer = getGame().getPlayer(getColour().getOtherColour()).getName();
		CheckersPlugin.getInstance().getResponseHandler().expect(getName(), new DrawResponse(getGame(), offerer));
		alert(Messages.getString("Offers.drawOfferedOther", offerer));
		alert(Messages.getString("Offers.typeYesOrNo"));
	}

	@Override
	public void swapOffered() {
		String offerer = getGame().getPlayer(getColour().getOtherColour()).getName();
		CheckersPlugin.getInstance().getResponseHandler().expect(getName(), new SwapResponse(getGame(), offerer));
		alert(Messages.getString("Offers.swapOfferedOther", offerer));
		alert(Messages.getString("Offers.typeYesOrNo"));
	}

	@Override
	public void undoOffered() {
		String offerer = getGame().getPlayer(getColour().getOtherColour()).getName();
		CheckersPlugin.getInstance().getResponseHandler().expect(getName(), new UndoResponse(getGame(), offerer));
		alert(Messages.getString("Offers.undoOfferedOther", offerer));
		alert(Messages.getString("Offers.typeYesOrNo"));
	}

	@Override
	public void undoLastMove() {
		// nothing to do here
	}

	@Override
	public void checkPendingAction() {
		// nothing to do here

	}

	@Override
	public void playEffect(String effect) {
		Player player = getBukkitPlayer();
		if (player != null) {
			CheckersPlugin.getInstance().getFX().playEffect(player.getLocation(), effect);
		}
	}

	@Override
	public void teleport(Location loc) {
		Player player = getBukkitPlayer();
		if (player != null) {
			CheckersPlugin.getInstance().getPlayerTracker().teleportPlayer(player, loc);
		}
	}

	@Override
	public void teleport(BoardView bv) {
		Player player = getBukkitPlayer();
		if (player != null) {
			// only teleport the player if they're not on (or very near) the board already
			if (!bv.getBoard().getFullBoard().outset(CuboidDirection.Both, 5).contains(player.getLocation())) {
				Location loc = bv.getTeleportInDestination();
				CheckersPlugin.getInstance().getPlayerTracker().teleportPlayer(player, loc);
			}
		}
	}

	@Override
	public void timeControlCheck() {
		TwoPlayerClock clock = getGame().getClock();
		if (needToWarn(clock)) {
			alert(Messages.getString("Game.timeControlWarning", clock.getRemainingTime(getColour()) / 1000));
			tcWarned++;
		}
	}

	private boolean needToWarn(TwoPlayerClock clock) {
		if (clock.getTimeControl().getControlType() == ControlType.NONE) {
			return false;
		}
		long remaining = clock.getRemainingTime(getColour());
		long t = CheckersPlugin.getInstance().getConfig().getInt("time_control.warn_seconds") * 1000;
		long tot = clock.getTimeControl().getTotalTime();
		long warning = Math.min(t, tot) >>> tcWarned;

		int tickInt = (CheckersPlugin.getInstance().getConfig().getInt("tick_interval") * 1000) + 50;	// fudge for inaccuracy of tick timer
		return remaining <= warning && remaining > warning - tickInt;
	}

	@Override
	public boolean isAvailable() {
		return getBukkitPlayer() != null;
	}
}
