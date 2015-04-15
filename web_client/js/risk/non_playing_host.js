var game_state, my_player_id, selectedTerriories = [];

//TODO use pre-game in state to add player by player deployment of 1 army to each country and move to real game play after all deployment finished.

function setup(){
        my_player_id = -1;
        getStateFromServer(function(){
            Risk.init();
            console.log(game_state);
            updateDisplay();

            //keep requesting state until it is your turn
            updateLots();
        });
}

function getStateFromServer(functionToCallAfter){
    $.ajax( "/?operation=get_state" )
        .done(function(data) {
            game_state = JSON.parse(data);
            functionToCallAfter();
        });
}

function updateDisplay(){
    updatePlayerDisplay();
    updateTurnPanel();
    Risk.updateMap();
    Risk.setTerritoriesColour();
}

function updatePlayerDisplay(){
    var playerInfo = "";

        playerInfo += "<br/><h5><strong>You are the host!</strong></h5>";

    playerInfo += "<h3>Players</h3>";
    game_state.players.forEach(function(player){
        playerInfo += "<h5><small>Player " + player.ID + ":</small> <strong style='color: " + colors[player.ID] + "'>" + player.name + "</strong>";
        playerInfo += "</h5>";
    });
    $("#playerInfo").html(playerInfo);
}

function updateTurnPanel(){
        $("#turnPanel").html("<h4>Watching Game...</h4>");
}




function updateLots(){
    getStateFromServer(function(){
            updateDisplay();
            setTimeout(updateLots, 300);
    });
}

function claimCountryDuringSetup(country_id){
    $.get('/?operation=perform_action&action=setup_claim_country&country_id=' + country_id, function(response){
    });
}