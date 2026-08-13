package com.inscryptor.engine;

import com.inscryptor.board.Board;
import com.inscryptor.board.Slot;
import com.inscryptor.exception.ConflictingModifierException;
import com.inscryptor.exception.InvalidSacrificeException;
import com.inscryptor.model.Card;
import com.inscryptor.model.GameEvent;
import com.inscryptor.model.GameEventType;
import com.inscryptor.modifier.ChallengeModifier;
import com.inscryptor.player.Player;

import java.util.ArrayList;
import java.util.List;

public class GameEngine {

    private final Board board;
    private final Player player;
    private final Player opponent;
    private final List<ChallengeModifier> activeModifiers = new ArrayList<>();

    public GameEngine(Board board, Player player, Player opponent) {
        this.board = board;
        this.player = player;
        this.opponent = opponent;
    }

    /**
     * Registers a modifier, rejecting it if it conflicts with one already active.
     * The engine never needs an if/else per modifier type - it just asks each
     * pair whether they're compatible.
     */
    public void addModifier(ChallengeModifier modifier) {
        for (ChallengeModifier existing : activeModifiers) {
            if (!existing.isCompatibleWith(modifier) || !modifier.isCompatibleWith(existing)) {
                throw new ConflictingModifierException(
                    modifier.getName() + " conflicts with " + existing.getName());
            }
        }
        activeModifiers.add(modifier);
    }

    public List<ChallengeModifier> getActiveModifiers() {
        return List.copyOf(activeModifiers);
    }

    public void applyModifiers() {
        GameContext context = currentContext();
        for (ChallengeModifier modifier : activeModifiers) {
            modifier.apply(context);
        }
    }

    /**
     * Starts a turn for one side: resets exhaustion on that side's cards (they
     * can act again) and broadcasts TURN_START so sigils can react.
     */
    public void startTurn(boolean isPlayerTurn) {
        GameContext context = currentContext();
        var slots = isPlayerTurn ? board.getPlayerSlots() : board.getOpponentSlots();
        for (var slot : slots) {
            Card occupant = slot.getOccupant();
            if (occupant != null) {
                occupant.resetExhaustion();
                fireEventIfOccupied(occupant, GameEventType.TURN_START, context);
            }
        }
    }

    /**
     * Resolves an attack: applies base damage via onAttack(), then broadcasts
     * an ATTACK event so sigils like Sharp Quills can react, then broadcasts
     * DEATH events for anything that died as a result.
     */
    public void resolveAttack(Card attacker, Card defender) {
        if (attacker == null || defender == null) {
            throw new IllegalArgumentException("Attacker and defender must not be null");
        }
        if (!defender.canBeTargeted()) {
            throw new IllegalStateException(defender.getName() + " cannot be targeted right now");
        }

        GameContext context = currentContext();

        attacker.onAttack(defender);

        GameEvent attackEvent = new GameEvent(GameEventType.ATTACK, attacker, defender);
        defender.applySigils(attackEvent, context);
        attacker.applySigils(attackEvent, context);

        announceDeathIfNeeded(defender, context);
        announceDeathIfNeeded(attacker, context);
    }

    private void announceDeathIfNeeded(Card card, GameContext context) {
        if (!card.isAlive()) {
            card.applySigils(new GameEvent(GameEventType.DEATH, card, null), context);
        }
    }

    /**
     * Rings the bell for one side: every card that side has on the board
     * attacks, one lane at a time, left to right (lane 0 first). This is not
     * something a player triggers per-card - it happens once, automatically,
     * when a turn ends. A card that was just placed this turn still attacks;
     * there's no summoning sickness in this ruleset, matching the reference
     * game's own lever-pull mechanic.
     */
    public void ringBell(boolean attackerIsPlayerSide) {
        int laneCount = board.getPlayerSlots().length;
        for (int lane = 0; lane < laneCount; lane++) {
            var attackerSlots = attackerIsPlayerSide ? board.getPlayerSlots() : board.getOpponentSlots();
            Card attacker = attackerSlots[lane].getOccupant();
            if (attacker != null && attacker.isAlive() && !attacker.isExhausted()) {
                resolveLaneAttack(attackerIsPlayerSide, lane);
            }
        }
    }

    /**
     * Attacks from a specific board lane. If the opposing lane has a card,
     * this delegates to resolveAttack() for creature-vs-creature combat. If
     * the opposing lane is empty, the attack goes straight to the defending
     * player's life - same as the table battler on which this is modeled.
     * Marks the attacker exhausted (so a single bell ring can't attack twice
     * with the same card) and fires a MOVE event afterward, so a Diver
     * attacker "surfaces" and becomes targetable once it acts.
     */
    private void resolveLaneAttack(boolean attackerIsPlayerSide, int lane) {
        var attackerSlots = attackerIsPlayerSide ? board.getPlayerSlots() : board.getOpponentSlots();
        var defenderSlots = attackerIsPlayerSide ? board.getOpponentSlots() : board.getPlayerSlots();

        Card attacker = attackerSlots[lane].getOccupant();
        Card defender = defenderSlots[lane].getOccupant();
        Player defendingPlayer = attackerIsPlayerSide ? opponent : player;

        if (defender != null && defender.hasSigil("Repulsive")) {
            // The attack fizzles entirely - not even face damage.
        } else if (defender != null && defender.canBeTargeted() && !bypassesDefender(attacker, defender)) {
            resolveAttack(attacker, defender);
        } else {
            // Lane is either empty, its defender can't be targeted right now
            // (e.g. Waterborne), or an Airborne attacker is skipping it -
            // either way, nothing blocks the attack and it lands on the
            // defending player directly.
            defendingPlayer.takeDamage(attacker.getAttack());
        }

        attacker.move();
        attacker.applySigils(new GameEvent(GameEventType.MOVE, attacker, null), currentContext());
        creditBonesForFallenCards();
        board.clearDeadCards();
    }

    /** Airborne attackers skip the defender entirely, unless it has Mighty Leap. */
    private boolean bypassesDefender(Card attacker, Card defender) {
        return attacker.hasSigil("Airborne") && !defender.hasSigil("Mighty Leap");
    }

    /**
     * Grants the owner of any dead-but-not-yet-cleared card 1 bone - whether
     * it died as the attacker (e.g. Sharp Quills reflect) or the defender.
     * Must run before clearDeadCards(), which is what actually removes the
     * evidence from the board.
     */
    private void creditBonesForFallenCards() {
        creditBonesForFallenCards(board.getPlayerSlots(), player);
        creditBonesForFallenCards(board.getOpponentSlots(), opponent);
    }

    private void creditBonesForFallenCards(Slot[] slots, Player owner) {
        for (var slot : slots) {
            Card occupant = slot.getOccupant();
            if (occupant != null && !occupant.isAlive()) {
                owner.gainBones(1);
            }
        }
    }

    /**
     * Sacrifices whatever card is on a given lane of a side's own board for
     * blood and bone. This is the only sacrifice mechanic - fodder has to
     * already be on the board, not just sitting in hand, matching the
     * reference game's own rule. A card with Many Lives stays on the board
     * even after being sacrificed - only removed otherwise.
     */
    public void sacrificeFromBoard(boolean isPlayerSide, int lane) {
        Card card = board.peekCard(isPlayerSide, lane);
        if (card == null) {
            throw new InvalidSacrificeException("No card in lane " + lane + " to sacrifice");
        }
        if (!card.hasSigil("Many Lives")) {
            board.removeCard(isPlayerSide, lane);
        }
        Player owner = isPlayerSide ? player : opponent;
        owner.sacrificeCard(card);
    }

    private void fireEventIfOccupied(Card card, GameEventType type, GameContext context) {
        if (card != null) {
            card.applySigils(new GameEvent(type, card, null), context);
        }
    }

    private GameContext currentContext() {
        return new GameContext(board, player, opponent);
    }
}
