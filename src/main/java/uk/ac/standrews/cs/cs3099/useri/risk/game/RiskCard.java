package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

/**
 * Risk Card Implementation
 */
public class RiskCard implements JSONAware{
	private RiskCardType cardType;
	private int cardID;

    /**
     * Contructor of RiskCard
     * @param type Enum for type of cards
     * @param cardID ID of the card
     */
    public RiskCard(RiskCardType type, int cardID){
        this.cardType = type;
        this.cardID = cardID;
    }

    /**
     * getter of the card type
     * @return Enum RiskCardType
     */
    public RiskCardType getType(){return cardType;}

    /**
     * getter of the card ID
     * @return integer variable with cardID
     */
    public int getCardID(){return cardID;}

    /**
     * Method to generate JSON marshalled text to represent the card
     * @return String to represent card in JSON format
     */
    @Override
    public String toJSONString() {
        JSONObject card = new JSONObject();
        card.put("type", cardType.toString());
        card.put("card_id", cardID);
        return card.toJSONString();
    }
}
