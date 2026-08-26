package com.inscription.engine;

import com.inscription.board.Board;
import com.inscription.board.Slot;
import com.inscription.exception.ConflictingModifierException;
import com.inscription.exception.InvalidSacrificeException;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;
import com.inscription.model.GameEventType;
import com.inscription.model.SpecialCardType;
import com.inscription.modifier.ChallengeModifier;
import com.inscription.player.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameEngine {

    private final Board board;
    private final Player player;
    private final Player opponent;
    private final List<ChallengeModifier> activeModifiers = new ArrayList<>();
    private final HealthScale healthScale = new HealthScale();

    public GameEngine(Board board, Player player, Player opponent) {
        this.board = board;
        this.player = player;
        this.opponent = opponent;
    }

    public HealthScale getHealthScale() {
        return healthScale;
    }

    /**
     * Plays a card onto the board - the proper "this was actually played"
     * action, as opposed to Board.placeCard() which is just the raw
     * mechanical placement (used for pre-built setup, e.g. the opponent's
     * starting creature, where nothing should react to it being "played").
     * Fires PLACE so sigils like Hoarder can react.
     */
    public void placeCard(boolean isPlayerSide, int lane, Card card) {
        board.placeCard(isPlayerSide, lane, card);
        card.applySigils(new GameEvent(GameEventType.PLACE, card, null), currentContext());
        triggerGuardianResponse(isPlayerSide, lane);
    }

    /**
     * If the side opposing whoever just played has a Guardian card elsewhere
     * on its own board, and its own lane at this same index is empty, that
     * Guardian relocates there - "when an opposing card is played opposite
     * an empty space, this card moves to that space." Only one Guardian ever
     * responds per placement (the first one found).
     */
    private void triggerGuardianResponse(boolean placerIsPlayerSide, int lane) {
        boolean reactingSide = !placerIsPlayerSide;
        if (board.peekCard(reactingSide, lane) != null) {
            return;
        }
        var slots = reactingSide ? board.getPlayerSlots() : board.getOpponentSlots();
        for (int i = 0; i < slots.length; i++) {
            if (i == lane) {
                continue;
            }
            Card occupant = slots[i].getOccupant();
            if (occupant != null && occupant.hasSigil("Guardian")) {
                board.removeCard(reactingSide, i);
                board.placeCard(reactingSide, lane, occupant);
                return;
            }
        }
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
     * Resolves an attack for a specific, already-determined amount of
     * damage: applies it via onAttack() (letting a subclass still add its
     * own bonus on top, if one ever overrides it), then broadcasts an
     * ATTACK event so sigils like Sharp Quills can react, then broadcasts
     * DEATH events for anything that died as a result. The amount is
     * computed by the caller (see computeEffectiveAttack) rather than read
     * from attacker.getAttack() here, since positional effects like Stinky
     * need to adjust it first.
     */
    public void resolveAttack(Card attacker, Card defender, int attackAmount) {
        if (attacker == null || defender == null) {
            throw new IllegalArgumentException("Attacker and defender must not be null");
        }
        if (!defender.canBeTargeted()) {
            throw new IllegalStateException(defender.getName() + " cannot be targeted right now");
        }

        GameContext context = currentContext();

        attacker.onAttack(defender, attackAmount);

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
        fireTurnEnd(attackerIsPlayerSide);
    }

    /**
     * Broadcasts TURN_END to every card on one side - the trigger for
     * end-of-turn movement sigils like Sprinter and Hefty. Captures the
     * actual Card references before firing anything, rather than iterating
     * live board slots: a movement sigil relocates cards mid-loop, and
     * iterating by lane index would risk skipping a card that just got
     * shifted into an already-visited lane, or re-processing one that moved
     * into a not-yet-visited lane.
     */
    private void fireTurnEnd(boolean isPlayerSide) {
        var slots = isPlayerSide ? board.getPlayerSlots() : board.getOpponentSlots();
        List<Card> occupants = new ArrayList<>();
        for (var slot : slots) {
            if (slot.getOccupant() != null) {
                occupants.add(slot.getOccupant());
            }
        }
        GameContext context = currentContext();
        for (Card card : occupants) {
            card.applySigils(new GameEvent(GameEventType.TURN_END, card, null), context);
        }
    }

    /**
     * Attacks from a specific board lane. Which opposing lane(s) get hit
     * depends on the attacker's sigils: normally just the one directly
     * across, but Bifurcated Strike hits the two lanes adjacent to that one
     * instead, and Trifurcated Strike hits all three. Only the genuine
     * "directly across" lane falls back to face damage when its target is
     * empty - the extra lanes from a multi-strike sigil are wasted swings if
     * nothing's there, not free damage to the player.
     * <p>
     * Marks the attacker exhausted (so a single bell ring can't attack twice
     * with the same card) and fires a MOVE event afterward, so a Diver
     * attacker "surfaces" and becomes targetable once it acts.
     */
    private void resolveLaneAttack(boolean attackerIsPlayerSide, int lane) {
        var attackerSlots = attackerIsPlayerSide ? board.getPlayerSlots() : board.getOpponentSlots();
        Card attacker = attackerSlots[lane].getOccupant();
        int laneCount = board.getPlayerSlots().length;

        int effectiveAttack = computeEffectiveAttack(attacker, attackerIsPlayerSide, lane);

        Set<Card> relocatedBurrowers = new HashSet<>();
        for (int targetLane : determineTargetLanes(attacker, lane, laneCount)) {
            boolean isDirectlyAcross = targetLane == lane;
            strikeLane(attacker, effectiveAttack, attackerIsPlayerSide, targetLane, isDirectlyAcross, relocatedBurrowers);
        }

        attacker.move();
        attacker.applySigils(new GameEvent(GameEventType.MOVE, attacker, null), currentContext());
        creditBonesForFallenCards();
        List<int[]> combatDeathVacancies = captureDeathVacancies();
        board.clearDeadCards();
        triggerCorpseEaters(combatDeathVacancies);
    }

    /**
     * Records {side, lane} for every occupant that's about to be cleared by
     * clearDeadCards() - must be called before that happens, since afterward
     * there's no way to tell a lane that just died from one that was already
     * empty. Feeds triggerCorpseEaters().
     */
    private List<int[]> captureDeathVacancies() {
        List<int[]> vacancies = new ArrayList<>();
        var playerSlots = board.getPlayerSlots();
        for (int i = 0; i < playerSlots.length; i++) {
            Card occupant = playerSlots[i].getOccupant();
            if (occupant != null && !occupant.isAlive()) {
                vacancies.add(new int[]{1, i});
            }
        }
        var opponentSlots = board.getOpponentSlots();
        for (int i = 0; i < opponentSlots.length; i++) {
            Card occupant = opponentSlots[i].getOccupant();
            if (occupant != null && !occupant.isAlive()) {
                vacancies.add(new int[]{0, i});
            }
        }
        return vacancies;
    }

    /**
     * For each lane a friendly creature just died in (by combat), if its
     * owner has a Corpse Eater card in hand, automatically plays the first
     * one found onto that now-empty space - matching "if a card you own dies
     * by combat, this card is played from hand on its space." Goes through
     * placeCard() so PLACE still fires normally for whatever gets played.
     */
    private void triggerCorpseEaters(List<int[]> vacancies) {
        for (int[] vacancy : vacancies) {
            boolean isPlayerSide = vacancy[0] == 1;
            int lane = vacancy[1];
            if (board.peekCard(isPlayerSide, lane) != null) {
                continue;
            }
            Player owner = isPlayerSide ? player : opponent;
            Card corpseEaterCard = owner.getHand().stream()
                .filter(c -> c.hasSigil("Corpse Eater"))
                .findFirst()
                .orElse(null);
            if (corpseEaterCard != null) {
                owner.removeFromHand(corpseEaterCard);
                placeCard(isPlayerSide, lane, corpseEaterCard);
            }
        }
    }

    /**
     * The attacker's actual power for this whole attack action - its base
     * attack, minus 1 (floored at 0) if whoever sits directly opposite its
     * own home lane carries Stinky. Computed once per attack, not once per
     * lane struck, so a multi-strike attacker's power is reduced
     * consistently on every lane it hits, not just in a matchup against
     * Stinky itself.
     * <p>
     * The "base" attack itself isn't always the card's printed number: an
     * Ant-tribe card's base is the count of friendly Ant-tribe cards
     * currently on its own side of the board (itself included), so a swarm
     * of Ants grows stronger together. Stinky's reduction still applies on
     * top of that computed base, same as it would for any other card.
     */
    private int computeEffectiveAttack(Card attacker, boolean attackerIsPlayerSide, int homeLane) {
        int baseAttack = attacker.hasSigil("Ant") ? countFriendlyAnts(attackerIsPlayerSide) : attacker.getAttack();
        baseAttack += leaderBonus(attackerIsPlayerSide, homeLane);

        var opposingSlots = attackerIsPlayerSide ? board.getOpponentSlots() : board.getPlayerSlots();
        Card directOpponent = opposingSlots[homeLane].getOccupant();
        if (directOpponent != null && directOpponent.hasSigil("Stinky")) {
            return Math.max(0, baseAttack - 1);
        }
        return baseAttack;
    }

    /** +1 for each Leader-sigil card sitting directly adjacent (same side), not counting itself. */
    private int leaderBonus(boolean isPlayerSide, int lane) {
        var slots = isPlayerSide ? board.getPlayerSlots() : board.getOpponentSlots();
        int bonus = 0;
        if (lane - 1 >= 0) {
            Card left = slots[lane - 1].getOccupant();
            if (left != null && left.hasSigil("Leader")) {
                bonus += 1;
            }
        }
        if (lane + 1 < slots.length) {
            Card right = slots[lane + 1].getOccupant();
            if (right != null && right.hasSigil("Leader")) {
                bonus += 1;
            }
        }
        return bonus;
    }

    /** How many Ant-tribe cards are currently on one side of the board. */
    private int countFriendlyAnts(boolean isPlayerSide) {
        var slots = isPlayerSide ? board.getPlayerSlots() : board.getOpponentSlots();
        int count = 0;
        for (var slot : slots) {
            Card occupant = slot.getOccupant();
            if (occupant != null && occupant.hasSigil("Ant")) {
                count++;
            }
        }
        return count;
    }

    /**
     * The attack a card would currently deal if it attacked right now - the
     * same computation combat itself uses. Exposed publicly so a UI layer
     * (ConsoleGame today, a future JavaFX view later) can show live,
     * board-position-aware numbers - an Ant's swarm-scaled power, or a
     * reduction from facing Stinky - without Card itself needing to know
     * anything about the board.
     * <p>
     * If the card isn't currently placed (e.g. still in hand),
     * ownerSideIfNotPlaced says which side to preview it from. An Ant-tribe
     * card previews using the current friendly Ant count on that side - this
     * doesn't simulate that placing it would add one more Ant to the count,
     * including itself, so the true value after placing may be one higher.
     */
    public int getDisplayAttack(Card card, boolean ownerSideIfNotPlaced) {
        int[] location = findCardLocation(card);
        if (location != null) {
            boolean isPlayerSide = location[0] == 1;
            int lane = location[1];
            return computeEffectiveAttack(card, isPlayerSide, lane);
        }
        if (card.hasSigil("Ant")) {
            return countFriendlyAnts(ownerSideIfNotPlaced);
        }
        return card.getAttack();
    }

    /** Where a card currently sits on the board: {1, lane} for player side, {0, lane} for opponent, or null. */
    private int[] findCardLocation(Card card) {
        var playerSlots = board.getPlayerSlots();
        for (int i = 0; i < playerSlots.length; i++) {
            if (playerSlots[i].getOccupant() == card) {
                return new int[]{1, i};
            }
        }
        var opponentSlots = board.getOpponentSlots();
        for (int i = 0; i < opponentSlots.length; i++) {
            if (opponentSlots[i].getOccupant() == card) {
                return new int[]{0, i};
            }
        }
        return null;
    }

    /** Which opposing lane indices this attacker's sigils say it should hit. */
    private List<Integer> determineTargetLanes(Card attacker, int lane, int laneCount) {
        if (attacker.hasSigil("Trifurcated Strike")) {
            List<Integer> lanes = new ArrayList<>();
            if (lane - 1 >= 0) {
                lanes.add(lane - 1);
            }
            lanes.add(lane);
            if (lane + 1 < laneCount) {
                lanes.add(lane + 1);
            }
            return lanes;
        }
        if (attacker.hasSigil("Bifurcated Strike")) {
            List<Integer> lanes = new ArrayList<>();
            if (lane - 1 >= 0) {
                lanes.add(lane - 1);
            }
            if (lane + 1 < laneCount) {
                lanes.add(lane + 1);
            }
            return lanes;
        }
        return List.of(lane);
    }

    /**
     * Resolves the attacker's strike against one specific target lane, using
     * an already-computed attack amount (see computeEffectiveAttack).
     * isDirectlyAcross controls whether an empty/unblocked target falls back
     * to face damage - true only for the lane genuinely opposite the
     * attacker, false for every "extra" lane a multi-strike sigil adds.
     */
    private void strikeLane(Card attacker, int effectiveAttack, boolean attackerIsPlayerSide, int targetLane,
                             boolean isDirectlyAcross, Set<Card> relocatedBurrowers) {
        var defenderSlots = attackerIsPlayerSide ? board.getOpponentSlots() : board.getPlayerSlots();
        Card defender = defenderSlots[targetLane].getOccupant();

        if (defender == null) {
            // A Burrower elsewhere on the defending side relocates into an
            // attacked empty lane to block it, before this would otherwise
            // become face damage (or a wasted swing, for an extra lane).
            defender = relocateBurrowerIfPresent(attackerIsPlayerSide, targetLane, relocatedBurrowers);
        }

        if (defender != null && defender.hasSigil("Loose Tail")) {
            // Evades entirely - the strike lands on nothing, not even the
            // tail it leaves behind.
            handleLooseTail(attackerIsPlayerSide, targetLane, defender);
            return;
        }

        if (defender != null && defender.canBeTargeted() && !bypassesDefender(attacker, defender)) {
            resolveAttack(attacker, defender, effectiveAttack);
            return;
        }
        if (isDirectlyAcross) {
            // Lane is either empty (and no Burrower blocked it), its
            // defender can't be targeted right now (e.g. Waterborne), or an
            // Airborne attacker is skipping it - either way, nothing blocks
            // the attack and it shifts the shared health scale instead of
            // hitting a per-player life total.
            dealFaceDamage(attackerIsPlayerSide, effectiveAttack);
        }
        // Otherwise this was an "extra" lane from a multi-strike sigil with
        // no valid target - the swing is simply wasted, no face damage.
    }

    /**
     * Loose Tail's escape: the fleeing card's lane is refilled with a Tail
     * decoy, and the fleeing card itself moves one lane to the right if
     * that space is open. If there's nowhere to flee to (off the board or
     * blocked), it simply escapes the board entirely rather than being
     * force-placed somewhere the sigil didn't actually specify.
     */
    private void handleLooseTail(boolean attackerIsPlayerSide, int lane, Card fleeingCard) {
        boolean defenderIsPlayerSide = !attackerIsPlayerSide;
        board.removeCard(defenderIsPlayerSide, lane);
        board.placeCard(defenderIsPlayerSide, lane, SpecialCardType.TAIL.create());

        int fleeLane = lane + 1;
        int laneCount = board.getPlayerSlots().length;
        if (fleeLane < laneCount && board.peekCard(defenderIsPlayerSide, fleeLane) == null) {
            board.placeCard(defenderIsPlayerSide, fleeLane, fleeingCard);
        }
    }

    private void dealFaceDamage(boolean attackerIsPlayerSide, int amount) {
        if (attackerIsPlayerSide) {
            healthScale.damageOpponent(amount);
        } else {
            healthScale.damagePlayer(amount);
        }
    }

    /**
     * Scans the defending side's board for a card with Burrower elsewhere
     * (not already in the attacked lane, and not already relocated earlier
     * in this same multi-lane strike) and relocates it into the attacked
     * lane to block. Returns the relocated card, or null if there's no
     * Burrower available.
     */
    private Card relocateBurrowerIfPresent(boolean attackerIsPlayerSide, int targetLane, Set<Card> alreadyRelocated) {
        boolean defenderIsPlayerSide = !attackerIsPlayerSide;
        var slots = defenderIsPlayerSide ? board.getPlayerSlots() : board.getOpponentSlots();
        for (int i = 0; i < slots.length; i++) {
            if (i == targetLane) {
                continue;
            }
            Card occupant = slots[i].getOccupant();
            if (occupant != null && occupant.hasSigil("Burrower") && !alreadyRelocated.contains(occupant)) {
                board.removeCard(defenderIsPlayerSide, i);
                board.placeCard(defenderIsPlayerSide, targetLane, occupant);
                alreadyRelocated.add(occupant);
                return occupant;
            }
        }
        return null;
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
                owner.gainBones(occupant.hasSigil("Bone King") ? 4 : 1);
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
