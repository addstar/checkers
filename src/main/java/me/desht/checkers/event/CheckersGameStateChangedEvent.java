package me.desht.checkers.event;

import me.desht.checkers.game.CheckersGame;

import org.bukkit.event.HandlerList;

public class CheckersGameStateChangedEvent extends CheckersGameEvent {

	private static final HandlerList handlers = new HandlerList();

	public CheckersGameStateChangedEvent(CheckersGame game) {
		super(game);
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
