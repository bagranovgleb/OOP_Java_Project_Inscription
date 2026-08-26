package com.inscription.engine;

/**
 * The shared health scale both players fight over - a single value from 0
 * (player defeated) to 10 (opponent defeated), starting centered at 5. This
 * isn't a per-player stat like blood or bones; it belongs to the match
 * itself, which is why GameEngine owns it rather than Player.
 * <p>
 * Damage that would push the scale past an edge doesn't overflow the scale
 * itself - the excess is banked as golden teeth for whoever dealt it,
 * tallied silently and only meant to be revealed once the match ends.
 */
public final class HealthScale {

    public static final int MIN = 0;
    public static final int MAX = 10;
    public static final int CENTER = 5;

    private int value = CENTER;
    private int playerGoldenTeeth;
    private int opponentGoldenTeeth;

    public int getValue() {
        return value;
    }

    public boolean isPlayerVictorious() {
        return value >= MAX;
    }

    public boolean isOpponentVictorious() {
        return value <= MIN;
    }

    public int getPlayerGoldenTeeth() {
        return playerGoldenTeeth;
    }

    public int getOpponentGoldenTeeth() {
        return opponentGoldenTeeth;
    }

    /**
     * The player dealt unblocked damage - the scale shifts toward the
     * opponent's edge (MAX). Anything past MAX is banked as golden teeth
     * for the player instead of being wasted.
     */
    public void damageOpponent(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage cannot be negative");
        }
        int pushedTo = value + amount;
        if (pushedTo > MAX) {
            playerGoldenTeeth += pushedTo - MAX;
            pushedTo = MAX;
        }
        value = pushedTo;
    }

    /**
     * The opponent dealt unblocked damage - the scale shifts toward the
     * player's edge (MIN). Anything past MIN is banked as golden teeth for
     * the opponent instead of being wasted.
     */
    public void damagePlayer(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage cannot be negative");
        }
        int pushedTo = value - amount;
        if (pushedTo < MIN) {
            opponentGoldenTeeth += MIN - pushedTo;
            pushedTo = MIN;
        }
        value = pushedTo;
    }
}
