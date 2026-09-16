package com.inscription.run.event;

import com.inscription.model.Card;
import com.inscription.player.Player;
import com.inscription.run.NodeContent;
import com.inscription.run.RunContext;
import com.inscription.sigil.ExtraSigilMarker;
import com.inscription.sigil.Sigil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mysterious Stones: sacrifice a card from hand - only cards that actually
 * carry at least one real sigil are offered as a source, since transferring
 * nothing would be pointless, and each candidate's sigils are shown so the
 * choice is informed - to transfer its sigils onto another card in hand. A
 * target is only eligible if it hasn't already received 2 separate
 * transfers via this event; each transfer (regardless of how many
 * individual sigils it carries) counts as one toward that cap, tracked via
 * ExtraSigilMarker.
 */
public class MysteriousStonesEvent implements NodeContent {

    private static final int MAX_TRANSFERS_PER_CARD = 2;

    @Override
    public String describe() {
        return "Mysterious Stones: sacrifice a sigil-bearing card to transfer its sigils to another";
    }

    @Override
    public boolean resolve(RunContext context) {
        Player player = context.getPlayer();
        List<Card> drawnForThisEvent = HandUtil.drawEntireDeckIntoHand(player);
        List<Card> hand = player.getHand();

        List<Card> sources = hand.stream().filter(c -> !realSigils(c).isEmpty()).toList();
        if (sources.isEmpty()) {
            context.getUI().show("No card in hand carries any sigils to transfer - nothing happens.");
            HandUtil.returnUnusedDrawnCards(player, drawnForThisEvent);
            return true;
        }

        List<String> sourceTexts = sources.stream().map(this::describeWithSigils).toList();
        int sourceIndex = context.getUI().askChoice("Choose a card to sacrifice (its sigils will transfer):", sourceTexts);
        Card source = sources.get(sourceIndex);

        List<Card> targets = hand.stream()
            .filter(c -> c != source && transferCount(c) < MAX_TRANSFERS_PER_CARD)
            .toList();
        if (targets.isEmpty()) {
            context.getUI().show("No eligible card to receive the transfer (everything else has already reached "
                + MAX_TRANSFERS_PER_CARD + " transfers) - nothing happens.");
            HandUtil.returnUnusedDrawnCards(player, drawnForThisEvent);
            return true;
        }

        List<String> targetTexts = targets.stream().map(Card::toString).toList();
        int targetIndex = context.getUI().askChoice("Choose a card to receive its sigils:", targetTexts);
        Card target = targets.get(targetIndex);

        for (Sigil sigil : source.getSigils()) {
            target.addSigil(sigil);
        }
        target.addSigil(new ExtraSigilMarker());
        player.removeFromHand(source);
        context.getUI().show(target.getName() + " has gained " + source.getName() + "'s sigils.");
        HandUtil.returnUnusedDrawnCards(player, drawnForThisEvent);
        return true;
    }

    /** How many Mysterious Stones transfers this card has already received. */
    private int transferCount(Card card) {
        return (int) card.getSigils().stream().filter(s -> s.getName().equals("Extra Sigil")).count();
    }

    /** A card's meaningful sigils - hidden identity tags (tribes) don't count as "having a sigil" for this event, only real described abilities like Sharp Quills do. The internal ExtraSigilMarker is excluded too, for the same reason. */
    private List<Sigil> realSigils(Card card) {
        return card.getVisibleSigils().stream().filter(s -> !s.getName().equals("Extra Sigil")).toList();
    }

    private String describeWithSigils(Card card) {
        String sigilNames = realSigils(card).stream().map(Sigil::getName).collect(Collectors.joining(", "));
        return card + " [sigils: " + sigilNames + "]";
    }
}
