var game_state, my_player_id, selectedTerriories = [];

//TODO use pre-game in state to add player by player deployment of 1 army to each country and move to real game play after all deployment finished.

function setup(){
        my_player_id = -1;
        getStateFromServer(function(){
            Risk.init();
            updateDisplay();

            //keep requesting state until it is your turn
            updateLots();
        });

    $("#turnPanel").html("<h4>Watching Game...</h4>");
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
    Risk.updateMap();
    Risk.setTerritoriesColour();
    checkForEndGame();
}

function updatePlayerDisplay(){
    var playerInfo = "";

        //playerInfo += "<br/><h5><strong>You are the host!</strong></h5>";

    playerInfo += "<h3>Players</h3>";
    game_state.players.forEach(function(player){
        playerInfo += "<h5><small>Player " + player.ID + ":</small> <strong style='color: " + colors[player.ID] + "'>" + player.name + "</strong>";
        playerInfo += "</h5>";
    });
    $("#playerInfo").html(playerInfo);
}


function updateLots(){
    getStateFromServer(function(){
            updateDisplay();
            setTimeout(updateLots, 500);
    });
}


function checkForEndGame(){
    if(game_state.winner != null) {
            setTimeout(function(){
                //window.location.href = "/endGame.html";
                $.ajax("/endGame.html").done(function(html){
                    document.open();
                    document.write(html);
                    var newText = "<strong>Congratulations " + game_state.winner.name + "!</strong> (id: " + game_state.winner.ID + ")" +
                        "<p/>They have successfully taken over the world!";
                        document.getElementById("gameOverText").innerHTML = newText;
                })

            }, 2500);
    }
}

function restart(){
    $.ajax("/?operation=restart_everything").done(function(response){
        window.location.href = "/";
    });
}