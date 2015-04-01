package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

public class RiskCard implements JSONAware{
	private RiskCardType cardType;
	private int cardID;

    public RiskCard(RiskCardType type, int cardID){
        this.cardType = type;
        this.cardID = cardID;
    }

    public RiskCardType getType(){return cardType;}
    public int getCardID(){return cardID;}

    @Override
    public String toJSONString() {
        JSONObject card = new JSONObject();
        card.put("type", cardType.toString());
        card.put("card_id", cardID);
        return card.toJSONString();
    }
}
