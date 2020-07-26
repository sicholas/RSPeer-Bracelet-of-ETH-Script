package com.sicholas;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptCategory;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;

@ScriptMeta(name = "Sich's Ethereum Script", desc = "Charges + HA Ethereum Bracelets ", developer = "Sich", category = ScriptCategory.MONEY_MAKING)
public class Main extends Script implements RenderListener {

    private static final DecimalFormat formatter = new DecimalFormat("#,###.##");
    private final String UNCHARGED_BRACELET_NAME = "Bracelet of ethereum (uncharged)";
    private final String REVENANT_ETHER_NAME = "Revenant ether";
    private final int BRACELET_OF_ETHERENUM_UNCHARGED_ID = 21817;
    private boolean restock = false;
    private long startTime;
    private int alchs = 0;
    private String status;
    private int startMagicXP, startMagicLvl = 0;
    private int gpEz = 0;

    private State getCurrentState() {
        if (!restock) {
            return State.ALCH;
        }
        return State.RESUPPLY;
    }

    @Override
    public void onStart() {
        startTime = System.currentTimeMillis();
        startMagicXP = Skills.getExperience(Skill.MAGIC);
        status = "Loading up!";
        startMagicLvl = Skills.getLevelAt(startMagicXP);
    }

    @Override
    public int loop() {
        if (Dialog.isOpen()) {
            Dialog.getContinue();
            return Random.high(150, 300);
        }

        switch (getCurrentState()) {
            case RESUPPLY:
                status = "Resupplying";
                if (Bank.isOpen() && Bank.close()) {
                    Time.sleepUntil(Bank::isClosed, 5000);
                    return Random.nextInt(150, 300);
                }

                final Item coins = Inventory.getFirst("Coins");
                if (coins == null) {
                    Log.fine("Stopping... Out of coins...");
                    return -1;
                }

                if (!GrandExchange.isOpen() && GrandExchange.open()) {
                    Time.sleepUntil(GrandExchange::isOpen, 10000);
                    return Random.nextInt(300, 600);
                }

                final RSGrandExchangeOffer offer = GrandExchange.getFirst(o -> o.getItemId() == BRACELET_OF_ETHERENUM_UNCHARGED_ID);
                if (offer != null) {
                    if (offer.getProgress() == RSGrandExchangeOffer.Progress.FINISHED) {
                        GrandExchange.collectAll(true);
                        restock = false;
                        return Random.nextInt(300, 600);
                    }
                    return Random.nextInt(1200, 1800);
                }
                if (!GrandExchange.getView().equals(GrandExchange.View.BUY_OFFER) &&
                        GrandExchange.createOffer(RSGrandExchangeOffer.Type.BUY)) {
                    Time.sleepUntil(() -> GrandExchange.getView().equals(GrandExchange.View.BUY_OFFER), 3000);
                    return Random.nextInt(300, 500);
                }

                final Item item = GrandExchangeSetup.getItem();
                if (item == null || item.getId() != BRACELET_OF_ETHERENUM_UNCHARGED_ID) {
                    GrandExchangeSetup.setItem(BRACELET_OF_ETHERENUM_UNCHARGED_ID);
                    return Random.nextInt(600, 1200);
                }


                try {
                    final int price = ExPriceCheck.getOSBuddyPrice(BRACELET_OF_ETHERENUM_UNCHARGED_ID) + 400;
                    if (GrandExchangeSetup.getPricePerItem() != price) {
                        GrandExchangeSetup.setPrice(price);
                        return Random.nextInt(400, 800);
                    }
                    if (GrandExchangeSetup.getQuantity() <= 0) {

                        GrandExchangeSetup.setQuantity((int) Math.floor((double) coins.getStackSize() / price));
                        return Random.nextInt(400, 800);
                    }

                    GrandExchangeSetup.confirm();
                    return Random.nextInt(2000, 4000);
                } catch (IOException e) {
                    Log.fine("Cannot fetch item price");
                }

                return Random.nextInt(150, 300);
            case ALCH:
                status = "Commencing alching";
                Item bracelet = Inventory.getFirst("Bracelet of ethereum");
                if (bracelet != null) {
                    if (!Tabs.isOpen(Tab.MAGIC)) {
                        Tabs.open(Tab.MAGIC);
                        Time.sleepUntil(() -> Tabs.isOpen(Tab.MAGIC), 1000);
                    }

                    if (!Magic.isSpellSelected() && Magic.cast(Spell.Modern.HIGH_LEVEL_ALCHEMY)) {
                        Time.sleepUntil(Magic::isSpellSelected, 1800);
                        return Random.nextInt(600, 1200);
                    }

                    if (bracelet.interact("Cast")) {
                        int count = Inventory.getCount();
                        Time.sleepUntil(() -> count != Inventory.getCount(), 1000);
                        Time.sleep(400);
                        gpEz += 750;
                        alchs++;
                    }
                    return Random.nextInt(300, 600);
                }

                Item unchargedBracelet = Inventory.getFirst(UNCHARGED_BRACELET_NAME);
                Item ether = Inventory.getFirst(REVENANT_ETHER_NAME);
                if (unchargedBracelet != null && ether != null) {
                    if (Bank.isOpen() && Bank.close()) {
                        Time.sleepUntil(Bank::isClosed, 1800);
                        return Random.nextInt(100, 150);
                    }

                    if (!Inventory.isItemSelected() && ether.interact("Use")) {
                        Time.sleepUntil(Inventory::isItemSelected, 500);
                    } else if (bracelet.interact("Use")) {
                        Time.sleepUntil(() -> Inventory.contains("Bracelet of ethereum"), 1000);
                    }
                    return Random.nextInt(300, 600);
                }

                if (!Bank.isOpen() && Bank.open()) {
                    Time.sleepUntil(Bank::isOpen, 5000);
                    return Random.nextInt(150, 300);
                }

                if (Inventory.contains(UNCHARGED_BRACELET_NAME)) {
                    if (Bank.withdraw(REVENANT_ETHER_NAME, 1)) {
                        Time.sleepUntil(() -> Inventory.contains(REVENANT_ETHER_NAME), 2000);
                    }
                    return Random.high(100, 200);
                }

                if (Bank.contains(UNCHARGED_BRACELET_NAME) && Bank.contains(REVENANT_ETHER_NAME)) {
                    if (!Inventory.contains(UNCHARGED_BRACELET_NAME) &&
                            Bank.withdraw(UNCHARGED_BRACELET_NAME, 1)) {
                        Time.sleepUntil(() -> Inventory.contains(UNCHARGED_BRACELET_NAME), 2000);
                        return Random.nextInt(100, 150);
                    }
                    if (!Inventory.contains(REVENANT_ETHER_NAME)) {
                        if (Bank.withdraw(REVENANT_ETHER_NAME, 1)) {
                            Time.sleepUntil(() -> Inventory.contains(REVENANT_ETHER_NAME), 2000);
                        }
                    }
                } else {
                    restock = true;
                }

                return Random.nextInt(100, 150);
        }
        return Random.high(100, 150);
    }

    @Override
    public void notify(RenderEvent renderEvent) {
        int nextLvlXp = Skills.getExperienceToNextLevel(Skill.MAGIC);
        int gainedXp = Skills.getExperience(Skill.MAGIC) - startMagicXP;
        double ttl = (nextLvlXp / (getPerHour(gainedXp) / 60.0 / 60.0 / 1000.0));
        if (nextLvlXp == 0) {
            ttl = 0;
        }
        Graphics g = renderEvent.getSource();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0, 0, 0, 128));
        g2.fillRect(20, 235, 200, 95);
        g2.setColor(Color.WHITE);
        g2.drawRect(20, 235, 200, 95);

        int x = 25;
        int y = 250;
        FontMetrics metrics = g2.getFontMetrics();

//        double thieveRate = ((double) steal / (double) (steal + fail)) * 100;
//        String lvlsGained = (Skills.getLevelAt(Skills.getExperience(Skill.MAGIC)) - startMagicLvl) > 0 ? " (+" + (Skills.getLevelAt(Skills.getExperience(Skill.MAGIC)) - startMagicLvl) + ")" : "";
        final int currentMagicLevel = Skills.getCurrentLevel(Skill.MAGIC);
        final int gained = currentMagicLevel - startMagicLvl;
        String lvlsGained = gained > 0 ? " (+" + gained + ")" : "";

        g2.setColor(Color.WHITE);
        g2.drawString("Magic lvl: ", x, y);
        g2.setColor(new Color(238, 130, 238));
        int width = metrics.stringWidth("Magic lvl: ");
        g2.drawString(currentMagicLevel + lvlsGained, x + width, y);
        g2.setColor(Color.WHITE);
        width = metrics.stringWidth("Magic lvl: " + currentMagicLevel + lvlsGained);
        g2.drawString(" (TTL: " + formatTime(Double.valueOf(ttl).longValue()) + ")", x + width, y);

        g2.drawString("XP Gained: ", x, y += 15);
        width = metrics.stringWidth("XP Gained: ");
        g2.setColor(Color.YELLOW);
        g2.drawString(formatter.format(gainedXp), x + width, y);
        width = metrics.stringWidth("XP Gained: " + formatter.format(gainedXp));
        g2.setColor(Color.WHITE);
        g2.drawString(" (" + formatter.format(getPerHour(gainedXp)) + "/hr)", x + width, y);

        g2.drawString("GP Made: ", x, y += 15);
        width = metrics.stringWidth("GP Made: ");
        g2.setColor(Color.ORANGE);
        g2.drawString(formatter.format(gpEz), x + width, y);
        width = metrics.stringWidth("GP Made: " + formatter.format(gpEz));
        g2.drawString(" (" + formatter.format(getPerHour(gpEz)) + "/hr)", x + width, y);
        g2.setColor(Color.WHITE);
        g2.drawString("Alchs P/H: ", x, y += 15);
        width = metrics.stringWidth("Alchs P/H: ");
        g2.setColor(Color.ORANGE);
        g2.drawString(formatter.format(alchs), x + width, y);
        width = metrics.stringWidth("Alchs P/H: " + formatter.format(alchs));
        g2.drawString(" (" + formatter.format(getPerHour(alchs)) + "/hr)", x + width, y);
//        g2.setColor(Color.WHITE);
//        g2.drawString("Stunner: ", x, y += 15);
//        width = metrics.stringWidth("Stunner: ");
//        g2.setColor(Color.YELLOW);
//        g2.drawString(formatter.format(fail) + "/" + formatter.format(steal + fail), x + width, y);
//        width = metrics.stringWidth("Stunner: " + formatter.format(fail) + "/" + formatter.format(steal + fail));
//        g2.setColor(Color.WHITE);
//        g2.drawString(" (" + formatter.format(thieveRate) + "% success)", x + width, y);

        g2.setColor(Color.WHITE);
        g2.drawString("Elapsed Time: ", x, y += 15);
        width = metrics.stringWidth("Elapsed Time: ");
        g2.setColor(Color.PINK);
        g2.drawString(formatTime(System.currentTimeMillis() - startTime), x + width, y);

        g2.setColor(Color.WHITE);
        g2.drawString("Status: ", x, y += 15);
        width = metrics.stringWidth("Status: ");
        g2.setColor(Color.YELLOW);
        g2.drawString(status, x + width, y);


        //Hide username
        if (Players.getLocal() != null) {
            Color tanColor = new Color(204, 187, 154);
            g2.setColor(tanColor);
            g2.fillRect(9, 459, 91, 15);
        }

    }

    private double getPerHour(double value) {
        if ((System.currentTimeMillis() - startTime) > 0) {
            return value * 3600000d / (System.currentTimeMillis() - startTime);
        } else {
            return 0;
        }
    }

    private String formatTime(final long ms) {
        long s = ms / 1000, m = s / 60, h = m / 60;
        s %= 60;
        m %= 60;
        h %= 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    private enum State {
        ALCH,
        RESUPPLY
    }
}