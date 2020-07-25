package com.sicholas;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;
import org.rspeer.script.ScriptCategory;
import org.rspeer.runetek.api.component.Dialog;
import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;

@ScriptMeta(name = "Sich's Ethereum Script",  desc = "Charges + HA Ethereum Bracelets ", developer = "Sich", category = ScriptCategory.MONEY_MAKING)
public class Main extends Script implements RenderListener {
    boolean restock = false;
    private long startTime;
    public int alchs = 0;
    private String status;
    public int startMagicXP, startMagicLvl = 0;
    public int gpEz = 0;
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
        DecimalFormat formatter = new DecimalFormat("#,###.##");
        FontMetrics metrics = g2.getFontMetrics();

//        double thieveRate = ((double) steal / (double) (steal + fail)) * 100;
        String lvlsGained = (Skills.getLevelAt(Skills.getExperience(Skill.MAGIC)) - startMagicLvl) > 0 ? " (+" + (Skills.getLevelAt(Skills.getExperience(Skill.MAGIC)) - startMagicLvl) + ")" : "";

        g2.setColor(Color.WHITE);
        g2.drawString("Magic lvl: ", x, y);
        g2.setColor(new Color(238, 130, 238));
        int width = metrics.stringWidth("Magic lvl: ");
        g2.drawString(Skills.getLevelAt(Skills.getExperience(Skill.MAGIC)) + lvlsGained, x + width, y);
        g2.setColor(Color.WHITE);
        width = metrics.stringWidth("Magic lvl: " + Skills.getLevelAt(Skills.getExperience(Skill.MAGIC)) + lvlsGained);
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
    private enum State {
        alch, resupply
    }

    private State getCurrentState() {
        if(!restock){
            return State.alch;
        }
        return State.resupply;
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
        if(Dialog.isOpen()){
            Dialog.getContinue();
        }
        switch(getCurrentState()) {

            case resupply:
                status = "Resupplying";
                Bank.close();
                Time.sleepUntil(()->Bank.isClosed(), 10000);
                GrandExchange.open();
//                getOSBuddyPrice
                GrandExchange.createOffer(RSGrandExchangeOffer.Type.BUY);

                GrandExchangeSetup.setItem(21817);

                try {
                    GrandExchangeSetup.setPrice(ExPriceCheck.getOSBuddyPrice(21817) + 400);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    GrandExchangeSetup.setQuantity((int)Math.floor(Inventory.getFirst("Coins").getStackSize() / (ExPriceCheck.getOSBuddyPrice(21817) + 400)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                GrandExchangeSetup.setQuantity(30);
                GrandExchangeSetup.confirm();
//                Time.sleep(3000);
//                GrandExchange.createOffer(RSGrandExchangeOffer.Type.BUY);
//                GrandExchangeSetup.setItem(21820);
//                try {
//                    GrandExchangeSetup.setPrice(ExPriceCheck.getOSBuddyPrice(21820) + 20);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                GrandExchangeSetup.setQuantity(30);
//                GrandExchangeSetup.confirm();
                Time.sleep(2000);
//                Time.sleepUntil(()-> GrandExchange.collectAll(), 10000);
                Time.sleep(1000);
                GrandExchange.collectAll(true);
                restock = false;
//                break;
            case alch:
                status = "Commencing alching";
                if(Inventory.contains("Bracelet of ethereum")){
                    Item[] braces = Inventory.getItems(item -> item.getName().equalsIgnoreCase("Bracelet of ethereum"));


                    Tabs.open(Tab.MAGIC);
                    Time.sleepUntil(() -> Tabs.isOpen(Tab.MAGIC), 2000);
                    Magic.cast(Spell.Modern.HIGH_LEVEL_ALCHEMY);
                    Inventory.getFirst("Bracelet of ethereum").interact("Cast");
                    Time.sleepUntil(() -> !Inventory.contains("Bracelet of ethereum"), 2000);
                    Time.sleep(400);
                    gpEz += 750;
                    alchs++;

                }
                else if(Inventory.contains("Bracelet of ethereum (uncharged)") && Inventory.contains("Revenant ether")){
                    Inventory.getFirst("Revenant ether").interact("Use");
                    Time.sleepUntil(() -> Inventory.isItemSelected(), 2000);
                    Inventory.getFirst("Bracelet of ethereum (uncharged)").interact("Use");
                    Time.sleepUntil(() -> Inventory.contains("Bracelet of ethereum"), 2000);
                }
                else {
                    Bank.open();
                    Time.sleepUntil(() -> Bank.isOpen(), 30000);
                    if ((Bank.contains("Bracelet of ethereum (uncharged)")) && Bank.contains("Revenant ether")) {
                        Bank.withdraw("Bracelet of ethereum (uncharged)", 1);
                        Time.sleepUntil(() -> Inventory.contains("Bracelet of ethereum (uncharged)"), 2000);
                        Bank.withdraw("Revenant ether", 1);
                        Time.sleepUntil(() -> Inventory.contains("Revenant ether"), 2000);
                        Bank.close();
                        Time.sleep(250);
//                    Time.sleep(500);

                    }
                    else{
                        restock = true;
                    }
                }

                break;
        }
        return 0;
    }

    @Override
    public void onStop() {

    }

    private String formatTime(final long ms) {
        long s = ms / 1000, m = s / 60, h = m / 60;
        s %= 60;
        m %= 60;
        h %= 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
}