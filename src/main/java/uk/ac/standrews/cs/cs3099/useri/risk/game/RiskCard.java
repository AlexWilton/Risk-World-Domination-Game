package uk.ac.standrews.cs.cs3099.useri.risk.game;

public class RiskCard {
	private RiskCardType cardType;
	private int cardID;

    public RiskCard(RiskCardType type, int cardID){
        this.cardType = type;
        this.cardID = cardID;
    }

    public RiskCardType getType(){return cardType;}
    public int getCardID(){return cardID;}
}
