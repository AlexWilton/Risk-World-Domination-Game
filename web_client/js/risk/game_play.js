var game_state, my_player_id;

//$( window ).load(setup);

function setup(){
    $.ajax( "/?operation=get_player_id").done(function(id){
        my_player_id = parseInt(id);
        getStateFromServer(function(){
            Risk.init();
            console.log(game_state);
            updateDisplay();

            //keep requesting state until it is your turn
            setTimeout(function(){
                if(game_state.currentPlayer.ID != my_player_id)
                    getStateFromServer();
            }, 500);
        });
    });
};

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
    updateMapDisplay();
}

function updatePlayerDisplay(){
    var playerInfo = "";
    game_state.players.forEach(function(player){
        playerInfo += "<h5><small>Player " + player.ID + ":</small> <strong style='color: " + colors[player.ID] + "'>" + player.name + "</strong>";
        if(player.ID == game_state.currentPlayer.ID) playerInfo += " <small>(Current Player)</small>";
        playerInfo += "</h5>";
    });
    $("#playerInfo").html(playerInfo);
}

function updateMapDisplay(){
    game_state.players.forEach(function(player){

    });
    Risk.setTerritoriesColour();
}

function updateTurnPanel(){
    if(game_state.currentPlayer.ID == my_player_id){ //my turn
        var panelHtml = "Turn Stage: " + game_state.turn_stage;
        switch(game_state.turn_stage){
            case "STAGE_TRADING":
                if(isTradePossibleForMe())
                    panelHtml += generateTradeInPanel();
                else{
                    game_state.turn_stage = "STAGE_DEPLOYING";
                    updateTurnPanel();
                    return;
                }

                break;
            default:

                break;
        }
        $("#turnPanel").html(panelHtml);
    }else{
        $("#turnPanel").html("");
    }
}

function isTradePossibleForMe(){
    var cards = game_state.currentPlayer.cards;
    if(cards.length < 3)
        return false;

    //TODO check for possible combinations.
    return true;
}

function generateTradeInPanel(){
    var panelHtml = "<h4>Select cards to trade in:</h4>";
    panelHtml += '<form id="tradeIn"><div class="btn-group" data-toggle="buttons">' +
    '<input type="hidden" name="operation" value="perform_action"/>' +
    '<input type="hidden" name="action" value="trade_in"/>';
    game_state.currentPlayer.cards.forEach(function(card){
        panelHtml += '<label class="btn btn-default"><input type="checkbox" name="card" value="'+ card.card_id +'" autocomplete="off"> ' + card.type +'</label><br/><br/>';
    });
    panelHtml += '</div>' +
    '<br/><button type="button" onclick="make_trade_in_request()" class="btn btn-info">Make Trade</button></form>' +
    '<div id="tradeOutcome"></div>';
    return panelHtml;
}

function waitForServer(){
    $.get('/?operation=is_server_waiting_for_action', function(response){
        if(response == "true"){
            getStateFromServer();
            updateDisplay();
        }else{
            setTimeout(waitForServer, 500);
        }
    });
}


function make_trade_in_request(){
    $.get('/?' + $('#tradeIn').serialize(), function(outcome){
        console.log(outcome);
        if(outcome){
            $("#turnPanel").html("");
            $("#tradeOutcome").html("<h4>Waiting for Server...</h4>");
            waitForServer();
        }else{
            $("#tradeOutcome").html('<div class="alert alert-danger" role="alert">' +
            '<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>' +
                '<span class="sr-only">Error:</span>' +
                    'Trade Not Allowed.' +
            '</div>');
        }
        //http://localhost:52484/play.html?operation=is_server_waiting_for_action
    });
}