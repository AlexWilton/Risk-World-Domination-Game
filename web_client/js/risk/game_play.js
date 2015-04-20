var game_state, my_player_id, selectedTerriories = [], attackOrigin, attackDestination, fortificationOrigin, fortificationDestination;

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
    checkForEndGame();
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
            case "STAGE_FORTIFY":
                panelHtml += generateFortifyPanel();
                break;
            default:
                break;
        }
        $("#turnPanel").html(panelHtml);
        setupAttackOnclicks();
        setupFortifyOnClicks();
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


//*******ATTACKING FUNCTIONS***********
function generateAttackPanel(){
    if(game_state.attack_capture_needed) return generateAttackCapturePanel();

    var panelHtml = "";
    //see if a country to attack from has been selected:
    if(attackOrigin == null){
        panelHtml += "<br/><br/><p><strong>Select</strong> your territory to attack from.</p>";
    }else if(attackOrigin.troop_count == 1){
        attackOrigin = null;
        panelHtml += '<div class="alert alert-danger alert-dismissible" role="alert">' +
            '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
            '<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>' +
            '<span class="sr-only">Error:</span>' +
            'You must attack from a country with at least 2 armies.' +
            '</div>';
    }

    //display attacking country info
    if(attackOrigin != null) {
        panelHtml += '<br/><br/><p>Attack from Country: <strong style="color: ' + colors[attackOrigin.player_owner_id] + '">' + attackOrigin.name +
        '</strong><small><a id="removeAttackingCountryBtn" href="#"> (Remove)</a></small></p><form id="attackForm" class="form-horizontal">' +
        '<input type="hidden" name="operation" value="perform_action"/>' +
        '<input type="hidden" name="action" value="attack"/>' +
        '<input type="hidden" name="attacking_country_id" value="' + attackOrigin.country_id + '"/>';


        if(attackDestination == null){
            panelHtml += "<br/><p><strong>Select</strong> a terriory to attack.</p>";
        }else{
            panelHtml += '<input type="hidden" name="defending_country_id" value="'+ attackDestination.country_id +'"/>';

            panelHtml += '<p>Country to Attack: <strong style="color: ' + colors[attackDestination.player_owner_id] + '">' + attackDestination.name + '</strong>' +
            '<small><a id="removeDefendingCountryBtn" href="#"> (Remove)</a></small></p>';

            panelHtml += '<div class="form-group">' +
            '<label class="col-sm-5">Attack with:</label> ' +
            '<div class="col-sm-4"> ' +
            '<select name="num_of_armies" class="form-control">';
            for(var i = 1; i< attackOrigin.troop_count && i <= 3; i++) {
                panelHtml += '<option>' + i +'</option>';
            }
            panelHtml += "</select></div></div>" +
            '<button type="button" onclick="attempt_attack()" class="btn btn-danger">Attack!</button>';
        }
    }

    panelHtml += '<br/><br/><div id="attackOutcome"></div><br/><button type="button" onclick="end_attack_phrase()" class="btn btn-info">End Attack Phrase</button></form>';
    return panelHtml;
}

function end_attack_phrase(){
    $.get('/?operation=perform_action&action=attack&end_attack=yes', function(response){
            $("#turnPanel").html("");
            $("#attackOutcome").html("<h4>Waiting for Server...</h4>");
            waitForServer();
    });
}

function attempt_attack(){
    $.get('/?' + $('#attackForm').serialize(), function(response){
        if(response.indexOf("true") == 0){
            $("#turnPanel").html("");
            $("#attackOutcome").html("<h4>Waiting for Server...</h4>");
            var attackingCountryId = attackOrigin.country_id;
            var defendingCountryId = attackDestination.country_id;
            var originCountryArmyCountBeforeAttack = attackOrigin.troop_count;
            var destinationCountryArmyCountBeforeAttack = attackDestination.troop_count;
            waitForAttackToBeProcessed(attackingCountryId, defendingCountryId, originCountryArmyCountBeforeAttack, destinationCountryArmyCountBeforeAttack);
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

function make_attack_capture(){
    $.get('/?' + $('#attackCaptureForm').serialize(), function(response){
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

function waitForAttackToBeProcessed(attackingCountryId, defendingCountryId, originCountryArmyCountBeforeAttack, destinationCountryArmyCountBeforeAttack){
    getStateFromServer(function(){
        var attackHappened = false, countryCaptured = false, attackerArmiesLost = 0, defenderArmiesLost = 0;
        game_state.map.countries.forEach(function(c){
            if(attackingCountryId == c.country_id){
                if(c.troop_count != originCountryArmyCountBeforeAttack){
                    attackHappened = true;
                    attackerArmiesLost = originCountryArmyCountBeforeAttack - c.troop_count;
                }
            }

            if(defendingCountryId == c.country_id){
                if(c.troop_count != destinationCountryArmyCountBeforeAttack){
                    attackHappened = true;
                    defenderArmiesLost = destinationCountryArmyCountBeforeAttack - c.troop_count;
                    if(c.troop_count == 0)
                        countryCaptured = true;
                }
            }
        });
        if(attackHappened) {
            attackOrigin = null;
            attackDestination = null;
            updateDisplay();

            //Explain attack outcome to player
            var attackResultMsg = "";
            var alertType = "info";
            if(countryCaptured){
                alertType = "success";
                attackResultMsg = "Attack Successful! Territory Taken!";
                $("#turnPanel").html(generateAttackCapturePanel());
            }
            if(defenderArmiesLost == 0){
                alertType = "danger";
                attackResultMsg = "Attack Completely Failed!"
            }

            attackResultMsg += " <strong>Result:</strong><br/>Your armies lost: <strong>" + attackerArmiesLost + "</strong><br/>" +
            "Defender Armies lost: <strong>" + defenderArmiesLost + "</strong>";

            $("#attackOutcome").html('<br/><div class="alert alert-' + alertType + ' alert-dismissible" role="alert">' +
            '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
            attackResultMsg +
            '</div>');
        }
        else
            waitForAttackToBeProcessed(attackingCountryId, defendingCountryId, originCountryArmyCountBeforeAttack, destinationCountryArmyCountBeforeAttack);
    });
}

function generateAttackCapturePanel(){
    var panelHtml = '';

    panelHtml += '<form id="attackCaptureForm"><div class="btn-group" data-toggle="buttons">' +
    '<input type="hidden" name="operation" value="perform_action"/>' +
    '<input type="hidden" name="action" value="attack_capture"/>' +
    '<input type="hidden" name="attacking_country_id" value="' + game_state.attack_capture_origin.country_id + '"/>' +
    '<input type="hidden" name="defending_country_id" value="'+ game_state.attack_capture_destination.country_id +'"/>';

    panelHtml += '<p>You <strong>must</strong> move at least ' + game_state.attack_capture_min_armies_to_move_in + " armies from: ";
    panelHtml += '<p><strong>' + game_state.attack_capture_origin.name + '</strong></p><p>to:</p>';
    panelHtml += '<p><strong>' + game_state.attack_capture_destination.name + '</strong></p>';
    panelHtml += '<p>How many armies do you want to move in?</p><div class="form-group">' +
    '<div class="col-sm-5"> ' +
    '<select name="num_of_armies" class="form-control">';
    for(var i = game_state.attack_capture_min_armies_to_move_in; i<= game_state.attack_capture_origin.troop_count -1; i++) {
        panelHtml += '<option>' + i +'</option>';
    }
    panelHtml += "</select></div></div>";

    panelHtml += '<button type="button" onclick="make_attack_capture()" class="btn btn-info">Claim Country</button></form><br/><br/><br/><div id="attackOutcome"></div>';
    return panelHtml;
}

function setupAttackOnclicks(){
    $("#removeAttackingCountryBtn").click(function(){
        attackOrigin = null;
        attackDestination = null;
        updateTurnPanel();
    });
    $("#removeDefendingCountryBtn").click(function(){
        attackDestination = null;
        updateTurnPanel();
    });
}
//************************************


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
            $("#turnPanel").html("<h4>Waiting for Server...</h4>");
            selectedTerriories = [];
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

function generateFortifyPanel(){
    var panelHtml = "";
    //see if a country to deploy from has been selected:
    if(fortificationOrigin == null){
        panelHtml += "<br/><br/><p><strong>No</strong> fortification currently selected.</p>" +
        "<p>If you would like to make a fortification, please <strong>Select</strong> a terriory, " +
        "which you would like to deploy from.</p>" +
        "<p><small>If you do not wish to make a fortification, you can skip this stage.</small>" +
        '<br/><div id="fortifyStatus"></div><br/><button type="button" onclick="attemptFortification()" class="btn btn-info">Skip Fortification</button>';
    }

    //display defending country info
    if(fortificationOrigin != null) {
        panelHtml += '<br/><br/><p>Fortify from Country: <strong style="color: ' + colors[fortificationOrigin.player_owner_id] + '">' + fortificationOrigin.name +
        '</strong><small><a id="removeFortifyOriginBtn" href="#"> (Remove)</a></small></p><form id="fortifyForm" class="form-horizontal">' +
        '<input type="hidden" name="operation" value="perform_action"/>' +
        '<input type="hidden" name="action" value="fortify"/>' +
        '<input type="hidden" name="origin_country_id" value="' + fortificationOrigin.country_id + '"/>';


        if(fortificationDestination == null){
            panelHtml += "<br/><p><strong>Select</strong> a territory to fortify.</p>";
        }else{
            panelHtml += '<input type="hidden" name="destination_country_id" value="'+ fortificationDestination.country_id +'"/>';

            panelHtml += '<p>Country to Fortify: <strong style="color: ' + colors[fortificationDestination.player_owner_id] + '">' + fortificationDestination.name + '</strong>' +
            '<small><a id="removeFortifyDestinationBtn" href="#"> (Remove)</a></small></p>';

            panelHtml += '<div class="form-group">' +
            '<label class="col-sm-5">No. of Troops:</label> ' +
            '<div class="col-sm-4"> ' +
            '<select name="num_of_armies" class="form-control">';
            for(var i = 1; i< fortificationOrigin.troop_count; i++) {
                panelHtml += '<option>' + i +'</option>';
            }
            panelHtml += "</select></div></div>" +
            '<div id="fortifyStatus"></div><br/><button type="button" onclick="attemptFortification()" class="btn btn-primary">Fortify</button>';
        }
    }

    panelHtml += '<br/></form>';
    return panelHtml;
}

function attemptFortification(){

    var queryString = $('#fortifyForm').serialize();
    if(queryString == "") queryString = "operation=perform_action&action=fortify&skip_fortity=yes";
    $.get('/?' + queryString, function(response){
        if(response.indexOf("true") == 0){
            $("#turnPanel").html("<h4>Waiting for Server...</h4>" +
            '<br/><br/><div class="alert alert-success alert-dismissible" role="alert">' +
            '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
            'Fortification Successful' +
            '</div>');
            fortificationDestination = null;
            fortificationOrigin = null;
            updateDisplay();
            waitForMyTurn();
        }else{
            $("#fortifyStatus").html('<div class="alert alert-danger alert-dismissible" role="alert">' +
            '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
            '<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>' +
            '<span class="sr-only">Error:</span>' +
            response +
            '</div>');
        }
    });
}

function setupFortifyOnClicks(){
    $("#removeFortifyOriginBtn").click(function(){
        fortificationOrigin = null;
        fortificationDestination = null;
        updateTurnPanel();
    });
    $("#removeFortifyDestinationBtn").click(function(){
        fortificationDestination = null;
        updateTurnPanel();
    });
}

function waitForServer(){
    $.get('/?operation=is_server_waiting_for_action', function(response){
        if(response.indexOf("true") == 0){
            getStateFromServer(updateDisplay);
        }else{
            setTimeout(waitForServer, 400);
        }
    });
}

function waitForMyTurn(){
    getStateFromServer(function(){
        if(game_state.currentPlayer.ID == my_player_id){
            updateDisplay();
        }else{
            setTimeout(waitForMyTurn, 1000);
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

function checkForEndGame(){
    if(game_state.winner != null) {
        setTimeout(function(){
            //window.location.href = "/endGame.html";
            if(game_state.winner.ID == my_player_id){
                $.ajax("/win.html").done(function(html){
                    document.open();
                    document.write(html);
                    var newText = "<strong>Congratulations " + game_state.winner.name + "!</strong> (id: " + game_state.winner.ID + ")" +
                        "<p/>You have successfully taken over the world!";
                    $("#winText").html(newText);
                });
            }else{
                $.ajax("/lose.html").done(function(html){
                    document.open();
                    document.write(html);
                    var newText = "<strong>Unlucky! You have lost!</strong> " +
                        "<p/> Player " + game_state.winner.name + " (id: " + game_state.winner.ID + ") has beaten you!" +
                        "<p/><br/><small>Better luck next time.</small>";
                    document.getElementById("loseText").innerHTML = newText;
                });
            }
        }, 2000);
    }
}

function restart(){
    $.ajax("/?operation=restart_everything").done(function(response){
        window.location.href = "/";
    });
}