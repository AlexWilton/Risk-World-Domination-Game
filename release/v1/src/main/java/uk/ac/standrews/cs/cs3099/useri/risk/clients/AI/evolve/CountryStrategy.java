package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve;

import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.game.TurnStage;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Continent;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.CountrySet;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.*;

import java.util.ArrayList;

/**
 * Strategy for one country
 */
public class CountryStrategy {

    Country country;
    Player player;
    State state;
    double importance;

    public CountryStrategy(Country c, Player p, State s, double i){
        state = s;
        player = p;
        country = c;
        importance = i;
    }

    /**
     * calcs importance of this strategy, more important if we almost own its continent
     * @return
     */
    public double getImportance () {
        Continent c = state.getCountryContinent(country.getCountryId());
        double proportionOwned = ((double)c.getCountriesOwnedBy(player.getID()).size())/((double)c.getCountries().size());
        double enemiesAround = 0;

        for (Country en : country.getEnemyNeighbours()){
            enemiesAround += en.getTroops();
        }

        if (country.getOwner() == null)
            return 0;
        if (country.getOwner().getID() == player.getID()){

            return proportionOwned * c.getReinforcementValue() * enemiesAround/((double)country.getTroops()) ;
        } else {
            double sameAround = 0;
            for (Country neighbour : country.getNeighboursOwnedBy(player.getID())){
                sameAround += neighbour.getTroops();
            }
            //do we want it?
            return proportionOwned * c.getReinforcementValue() - country.getTroops()/sameAround;
        }

    }

    /**
     * just put a troop here is the beneficial commands if we own it
     * @return
     */
    public SetupCommand getBeneficialSetupCommand () {
        if ((!state.hasUnassignedCountries() && country.getOwner().getID() == player.getID()) || country.getOwner() == null)
            return new SetupCommand(country.getCountryId(),player.getID());
        return null;
    }

    /**
     * fortify here, or fortify to neighbour if we dont own it so we can capture it
     * @return
     */
    public  FortifyCommand getBeneficialFortifyCommand() {

        //deploy all to this, or around neighbours
        if (country.getOwner().getID() == player.getID()){
            Country best = null;
            CountrySet surr = country.getNeighboursOwnedBy(player.getID());
            for (Country c : surr) {
                if (best != null){
                    if (best.getTroops() < c.getTroops()){
                        best = c;
                    }
                }else {
                    best = c;
                }
            }
            if (best != null)
                return (new FortifyCommand(best.getCountryId(),country.getCountryId(),best.getTroops()-1,player.getID()));

        }

            //put them into surrounding country
            ArrayList<DeployTuple> deployTuples = new ArrayList<>();
            CountrySet surr = country.getNeighboursOwnedBy(player.getID());
            while (surr.size() != 0) {
                Country best = null;
                for (Country c : surr) {
                    if (best != null){
                        if (best.getTroops() < c.getTroops()){
                            best = c;
                        }
                    }else {
                        best = c;
                    }
                }
                //best origin
                Country bestOrigin = null;
                for (Country c : best.getSamePlayerNeighbours()) {
                    if (bestOrigin != null){
                        if (best.getTroops() < c.getTroops()){
                            bestOrigin = c;
                        }
                    }else {
                        bestOrigin = c;
                    }
                }
                if ( bestOrigin != null){
                    return (new FortifyCommand(bestOrigin.getCountryId(),best.getCountryId(),bestOrigin.getTroops()-1,player.getID()));

                }
                else {
                    surr.remove(best);
                }

            }

                //put into closest country

                Country best = country.getClosestCountryOwnedBy(player.getID());
                //best origin
                Country bestOrigin = null;
                for (Country c : best.getSamePlayerNeighbours()) {
                    if (bestOrigin != null){
                        if (best.getTroops() < c.getTroops()){
                            bestOrigin = c;
                        }
                    }else {
                        bestOrigin = c;
                    }
                }
                if ( bestOrigin != null){
                    return (new FortifyCommand(bestOrigin.getCountryId(),best.getCountryId(),bestOrigin.getTroops()-1,player.getID()));
                }






        return null;
    }

    /**
     * attack this if we can, if its ours, dont attack
     * @return
     */
    public AttackCommand getBeneficialAttackCommand() {


        if (country.getOwner().getID() != player.getID()){
            CountrySet neighboursOwned = country.getNeighboursOwnedBy(player.getID());


            if (neighboursOwned.size() > 0) {
                Country best = null;
                int troopsCount = 0;
                for (Country c : neighboursOwned) {
                    if (best != null){
                        if (best.getTroops() < c.getTroops()){
                            best = c;
                        }
                    }else {
                        best = c;
                    }

                    troopsCount += c.getTroops();
                }
                if (troopsCount > country.getTroops() && best.getTroops() > 1){
                    return (new AttackCommand(best.getCountryId(),country.getCountryId(),best.getTroops() >= 4 ? 3 : (best.getTroops()-1),player.getID()));
                }

            } else {
                return null;
            }
        }
        return null;
    }


    /**
     * deployu where we have to, and then to this country is beneficial
     * @return
     */
    public DeployCommand getBeneficialDeployCommand() {

        //deploy all to this, or around neighbours
        if (country.getOwner().getID() == player.getID()) {
            ArrayList<DeployTuple> deployTuples = new ArrayList<>();
            int armiesLeft = player.getUnassignedArmies();

            CountrySet mustDeployTo = state.getSetOfCountriesWhereAtLeastOneNeedsToBeDeployedTo();

            if (mustDeployTo != null) {
                Country c = mustDeployTo.get(mustDeployTo.getIDList().get(0));
                DeployTuple temp = new DeployTuple(c.getCountryId(), 2);
                armiesLeft -= 2;
                deployTuples.add(temp);

            }

            DeployTuple depTup = new DeployTuple(country.getCountryId(),armiesLeft);

            deployTuples.add(depTup);
            return new DeployCommand(deployTuples,player.getID());
        } else{
            //put them equally into surrounding countries
            ArrayList<DeployTuple> deployTuples = new ArrayList<>();
            int armiesLeft = player.getUnassignedArmies();

            CountrySet mustDeployTo = state.getSetOfCountriesWhereAtLeastOneNeedsToBeDeployedTo();

            if (mustDeployTo != null) {
                for (Country c : mustDeployTo) {
                    DeployTuple temp = new DeployTuple(c.getCountryId(), 2);
                    armiesLeft -= 2;
                    deployTuples.add(temp);
                }
            }
            CountrySet surr = country.getNeighboursOwnedBy(player.getID());
            if (surr.size() != 0) {
                int armiesPC = armiesLeft/surr.size();
                int rest = armiesLeft-armiesPC*surr.size();
                for (Country c : surr) {
                    int dep = armiesPC;
                    if (rest >= 1){
                        dep ++;
                        rest--;
                    }
                    deployTuples.add(new DeployTuple(c.getCountryId(),dep));
                }

            } else {
                deployTuples.add(new DeployTuple(country.getClosestCountryOwnedBy(player.getID()).getCountryId(),armiesLeft));
            }

            return new DeployCommand(deployTuples,player.getID());

        }
    }
}
