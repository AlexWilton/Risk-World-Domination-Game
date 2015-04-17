var game_state, my_player_id, selectedTerriories = [], attackOrigin, attackDestination;

//TODO use pre-game in state to add player by player deployment of 1 army to each country and move to real game play after all deployment finished.

function setup(){
    $.ajax( "/?operation=get_player_id").done(function(id){
        my_player_id = parseInt(id);
        getStateFromServer(function(){
            Risk.init();
            console.log(game_state);
            updateDisplay();

            //keep requesting state until it is your turn
            waitForMyTurn();
        });
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
    if(typeof game_state.players[my_player_id].isHost != 'undefined' && game_state.players[my_player_id].isHost == true){ //if this is the host
        playerInfo += "<br/><h5><strong>You are the host!</strong></h5>";
    }else
        playerInfo += "<br/><h5><strong>You are connected to the host!</strong></h5>"

    playerInfo += "<h3>Players</h3>";
    game_state.players.forEach(function(player){
        playerInfo += "<h5><small>Player " + player.ID + ":</small> <strong style='color: " + colors[player.ID] + "'>" + player.name + "</strong>";
        if(player.ID == game_state.currentPlayer.ID) playerInfo += " <small>(Myself)</small>";
        playerInfo += "</h5>";
    });
    $("#playerInfo").html(playerInfo);
}

function updateTurnPanel(){
    if(game_state.currentPlayer.ID == my_player_id){ //my turn
        if(game_state.pre_game_play === true){
            game_state.turn_stage = "STAGE_SETUP";
        }

        var panelHtml = "<strong>Your Turn</strong> (" + game_state.turn_stage + ")";
        switch(game_state.turn_stage){
            case "STAGE_SETUP":
                panelHtml += "<br/><h5>You still have <strong>" + game_state.currentPlayer.unassignedArmies +
                "</strong> left to deploy.</h5>It is your turn to <strong>Select</strong> a country!<div id='status'></div>";
                break;
            case "STAGE_TRADING":
                    panelHtml += generateTradeInPanel();

                break;
            case "STAGE_DEPLOYING":
                panelHtml += "<br/>You have " + game_state.currentPlayer.unassignedArmies + " armies to deploy.";
                panelHtml += "<br/><br/><small><strong>Select</strong> countries you wish to deploy to and specify how many armies to send to each.</small>";
                panelHtml += generateDeployPanel();
                break;
            case "STAGE_BATTLES":
                panelHtml += generateAttackPanel();

                break;
            default:
                break;
        }
        $("#turnPanel").html(panelHtml);
    }else{
        $("#turnPanel").html("<h4>Waiting for other players...</h4>");
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
    panelHtml += '</div><br/>';
    if(isTradePossibleForMe())
        panelHtml += '<button type="button" onclick="make_trade_in_request()" class="btn btn-info">Make Trade</button> ';
    panelHtml += '<button type="button" onclick="no_trade_request()" class="btn btn-info">No Trade</button></form>' +
    '<div id="tradeOutcome"></div>';
    return panelHtml;
}

function generateAttackPanel(){
    if(attackOrigin == null){
        return "<br/><br/><p><strong>Select</strong> your terriory to attack from.</p>";
    }

    if(attackOrigin.troop_count == 1){
        attackOrigin = null;
        return '<div class="alert alert-danger alert-dismissible" role="alert">' +
            '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
            '<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>' +
            '<span class="sr-only">Error:</span>' +
            'You must attack from a country with at least 2 armies.' +
            '</div>';
    }

    var panelHtml = '<br/><br/><p>Attack from Country: <strong style="color: ' + colors[attackOrigin.player_owner_id] + '">' + attackOrigin.name +
        '</strong></p><form id="attackForm" class="form-horizontal">' +
        '<input type="hidden" name="operation" value="perform_action"/>' +
        '<input type="hidden" name="action" value="attack"/>' +
        '<input type="hidden" name="attacking_country_id" value="'+ attackOrigin.country_id +'"/>' +
        '<input type="hidden" name="defending_country_id" value="'+ attackDestination.country_id +'"/>';

    if(attackDestination == null){
        panelHtml += "<br/><br/><p><strong>Select</strong> a terriory to attack.</p>";
        return panelHtml;
    }

    panelHtml += '<p>Country to Attack: <strong style="color: ' + colors[attackDestination.player_owner_id] + '">' + attackDestination.name + '</strong></p>';


    panelHtml += '<div class="form-group">' +
    '<label class="col-sm-5">Attack with:</label> ' +
    '<div class="col-sm-4"> ' +
    '<select name="num_of_armies" class="form-control">';
    for(var i = 1; i< attackOrigin.troop_count; i++) {
        panelHtml += '<option>' + i +'</option>';
    }
    panelHtml += "</select></div></div>";

    panelHtml += '<div id="attackOutcome"></div><button type="button" onclick="attempt_attack()" class="btn btn-danger">Attack!</button>' +
    '  <br/><br/><br/><button type="button" onclick="attempt_attack()" class="btn btn-info">End Attack Phrase</button><div id="attackOutcome"></div></form>';
    return panelHtml;
}


function attempt_attack(){
    $.get('/?' + $('#attackForm').serialize(), function(response){
        console.log(response);
        if(response.indexOf("true") == 0){
            $("#turnPanel").html("");
            $("#attackOutcome").html("<h4>Waiting for Server...</h4>");
            waitForServer();
        }else{
            $("#attackOutcome").html('<div class="alert alert-danger alert-dismissible" role="alert">' +
            '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
            '<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>' +
            '<span class="sr-only">Error:</span>' +
            response +
            '</div>');
        }
    });
}

function generateDeployPanel(){
    var panelHtml = '<form id="deployArmies" class="form-horizontal">' +
    '<input type="hidden" name="operation" value="perform_action"/>' +
    '<input type="hidden" name="action" value="deploy_armies"/>';
    for(t in selectedTerriories){
        if(selectedTerriories[t].player_owner_id == my_player_id) {
            var name = selectedTerriories[t].name;
            panelHtml += '<div class="form-group">' +
            '<label class="col-sm-8 control-label">'+name+'</label> ' +
            '<div class="col-sm-4"> ' +
            '<select name="' + selectedTerriories[t].country_id + '" class="form-control">';
            for(var i = 0; i<= game_state.currentPlayer.unassignedArmies; i++) {
                panelHtml += '<option>' + i +'</option>';
            }
            panelHtml += "</select></div></div>";
        }
    }

    panelHtml += '<div id="deployOutcome"></div><button type="button" onclick="attempt_deployment()" class="btn btn-info">Deploy Armies</button></form>';
    return panelHtml;
}
function attempt_deployment(){
    $.get('/?' + $('#deployArmies').serialize(), function(response){
        console.log(response);
        if(response.indexOf("true") == 0){
            $("#turnPanel").html("");
            $("#deployOutcome").html("<h4>Waiting for Server...</h4>");
            waitForServer();
        }else{
            $("#deployOutcome").html('<div class="alert alert-danger alert-dismissible" role="alert">' +
            '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
            '<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>' +
            '<span class="sr-only">Error:</span>' +
            response +
            '</div>');
        }
    });
}

function make_trade_in_request(){
    $.get('/?' + $('#tradeIn').serialize(), function(response){
        console.log(response);
        if(response.indexOf("true") == 0){
            $("#turnPanel").html("");
            $("#tradeOutcome").html("<h4>Waiting for Server...</h4>");
            waitForServer();
        }else{
            $("#tradeOutcome").html('<div class="alert alert-danger" role="alert">' +
            '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
            '<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>' +
            '<span class="sr-only">Error:</span>' +
                'Trade Not Allowed.' +
            '</div>');
        }
    });
}

function no_trade_request(){
    $.get('/?operation=perform_action&action=trade_in&no_trade=yes', function(response){
        console.log(response);
        if(response.indexOf("true") == 0){
            $("#turnPanel").html("");
            $("#tradeOutcome").html("<h4>Waiting for Server...</h4>");
            waitForServer();
        }else{
            $("#tradeOutcome").html('<div class="alert alert-danger" role="alert">' +
            '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
            '<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>' +
            '<span class="sr-only">Error:</span>' +
            'No Trade Not Allowed. You must select a valid trade' +
            '</div>');
        }
    });
}

function waitForServer(){
    $.get('/?operation=is_server_waiting_for_action', function(response){
        if(response.indexOf("true") == 0){
            getStateFromServer(updateDisplay);
        }else{
            setTimeout(waitForServer, 1000);
        }
    });
}

function waitForMyTurn(){
    getStateFromServer(function(){
        if(game_state.currentPlayer.ID == my_player_id){
            updateDisplay();
        }else{
            setTimeout(waitForMyTurn, 300);
        }
    })
}

function claimCountryDuringSetup(country_id){
    $.get('/?operation=perform_action&action=setup_claim_country&country_id=' + country_id, function(response){

        //getStateFromServer(Risk.updateMap());

        if(response.indexOf("true") == 0){
            $("#turnPanel").html("<h4>Waiting for other players...</h4>");
            waitForMyTurn();
        }else{
            $("#status").html('<div class="alert alert-danger" role="alert">' +
            '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
            '<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>' +
            '<span class="sr-only">Error:</span>' +
            'Selection not valid. Please try again.' +
            '</div>');
        }
    });
}