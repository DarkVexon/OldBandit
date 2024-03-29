package theWario.util;

import basemod.abstracts.CustomCard;
import basemod.helpers.BaseModCardTags;
import basemod.helpers.CardTags;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.Strike_Blue;
import com.megacrit.cardcrawl.cards.colorless.Bite;
import com.megacrit.cardcrawl.cards.green.Strike_Green;
import com.megacrit.cardcrawl.cards.red.Strike_Red;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.events.city.Vampires;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.relics.BloodVial;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import theWario.cards.HastyBite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VampiresBanditEvent extends AbstractImageEvent {
    public static final String ID = "bandit:Vampires";
    private static final EventStrings eventStrings;
    public static final String NAME;
    public static final String[] DESCRIPTIONS;
    private static final String[] OPTIONS;
    private static final String ACCEPT_BODY;
    private static final String EXIT_BODY;
    private static final String GIVE_VIAL;
    private static final int HP_DRAIN_PERCENT = 30;
    private static final float HP_PERCENT = 0.7F;
    private int screenNum = 0;
    private boolean hasVial;
    private List<String> bites;

    public VampiresBanditEvent() {
        super(NAME, "test", "images/events/vampires.jpg");
        this.body = AbstractDungeon.player.getVampireText();
        this.bites = new ArrayList<>();
        this.hasVial = AbstractDungeon.player.hasRelic("Blood Vial");
        this.imageEventText.setDialogOption(OPTIONS[0] + HP_DRAIN_PERCENT + OPTIONS[1], new HastyBite());
        if (this.hasVial) {
            String vialName = (new BloodVial()).name;
            this.imageEventText.setDialogOption(OPTIONS[3] + vialName + OPTIONS[4], new HastyBite());
        }
        this.imageEventText.setDialogOption(OPTIONS[2]);
    }

    protected void buttonEffect(int buttonPressed) {
        switch (this.screenNum) {
            case 0:
                switch (buttonPressed) {
                    case 0:
                        CardCrawlGame.sound.play("EVENT_VAMP_BITE");
                        this.imageEventText.updateBodyText(ACCEPT_BODY);
                        int hpLoss = (int) ((float) AbstractDungeon.player.maxHealth * HP_PERCENT);
                        int diff = AbstractDungeon.player.maxHealth - hpLoss;
                        AbstractDungeon.player.maxHealth = hpLoss;
                        if (AbstractDungeon.player.maxHealth <= 0) {
                            AbstractDungeon.player.maxHealth = 1;
                        }

                        if (AbstractDungeon.player.currentHealth > AbstractDungeon.player.maxHealth) {
                            AbstractDungeon.player.currentHealth = AbstractDungeon.player.maxHealth;
                        }

                        this.replaceAttacks();
                        logMetricObtainCardsLoseMapHP(Vampires.ID, "Became a vampire", this.bites, diff);
                        this.screenNum = 1;
                        this.imageEventText.updateDialogOption(0, OPTIONS[5]);
                        this.imageEventText.clearRemainingOptions();
                        return;
                    case 1:
                        if (this.hasVial) {
                            CardCrawlGame.sound.play("EVENT_VAMP_BITE");
                            this.imageEventText.updateBodyText(GIVE_VIAL);
                            AbstractDungeon.player.loseRelic("Blood Vial");
                            this.replaceAttacks();
                            logMetricObtainCardsLoseRelic(Vampires.ID, "Became a vampire (Vial)", this.bites, new BloodVial());
                            this.screenNum = 1;
                            this.imageEventText.updateDialogOption(0, OPTIONS[5]);
                            this.imageEventText.clearRemainingOptions();
                        } else {
                            refuse();
                        }
                        return;
                    case 2:
                        refuse();
                        return;
                    default:
                        refuse();
                        return;
                }
            case 1:
                this.openMap();
                break;
            default:
                this.openMap();
        }

    }

    private void refuse() {
        logMetricIgnored(Vampires.ID);
        this.imageEventText.updateBodyText(EXIT_BODY);
        this.screenNum = 2;
        this.imageEventText.updateDialogOption(0, OPTIONS[5]);
        this.imageEventText.clearRemainingOptions();
    }

    private void replaceAttacks() {
        for (Iterator<AbstractCard> i = AbstractDungeon.player.masterDeck.group.iterator(); i.hasNext(); ) {
            AbstractCard e = i.next();
            if ((e instanceof Strike_Red) || (e instanceof Strike_Green) || (e instanceof Strike_Blue)) {
                i.remove();
            }
            if ((e instanceof CustomCard && ((CustomCard) e).isStrike()) || CardTags.hasTag(e, BaseModCardTags.BASIC_STRIKE)) {
                i.remove();
            }
        }
        for (int i = 0; i < 5; i++) {
            AbstractCard c = new HastyBite();
            AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(c, Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
            this.bites.add(c.cardID);
        }
    }

    static {
        eventStrings = CardCrawlGame.languagePack.getEventString(Vampires.ID);
        NAME = eventStrings.NAME;
        DESCRIPTIONS = eventStrings.DESCRIPTIONS;
        OPTIONS = eventStrings.OPTIONS;
        ACCEPT_BODY = DESCRIPTIONS[2];
        EXIT_BODY = DESCRIPTIONS[3];
        GIVE_VIAL = DESCRIPTIONS[4];
    }
}
