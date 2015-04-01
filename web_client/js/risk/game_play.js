var game_state;

$( window ).load(setup);

function setup(){
    //get state
    $.ajax( "/?operation=get_state" )
        .done(function(data) {
            game_state = JSON.parse(data);
            console.log(game_state);
            updateDisplay();
        })
}

function updateDisplay(){
    var playerInfo = "";
    game_state.players.forEach(function(player){
        if(player.ID == game_state.currentPlayer.ID) playerInfo += "<div class=\"currentPlayer\">";
        playerInfo += "<h5><small>Player " + player.ID + ":</small> <strong>" + player.name + "</strong>";
        if(player.ID == game_state.currentPlayer.ID) playerInfo += " (Current Player)</div>";
        playerInfo += "</h5>";

    });
    $("#playerInfo").html(playerInfo);
}

