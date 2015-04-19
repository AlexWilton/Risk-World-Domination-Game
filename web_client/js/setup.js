var nonPlayingHost = false, aiPlayers = [];

$( document ).ready(function(){
    setOnchangeForIsHostPlayingRadioBtn();
    $("[name=number_of_players]").change(showAddAiPlayerBtnIfMaxPlayersNotReach);
    provideAIoptionsForHostSetupPanel();

});

function gotoGamePlayWhenReady(){
    $.ajax( "/?operation=move_to_game_play")
        .done(function(response) {
            if(response.indexOf("true") == 0){
                setTimeout(function() {
                    if (nonPlayingHost)
                        window.location.href = "/server.html";
                    else
                        window.location.href = "/play.html";
                }, 1000);
            }else{
                setTimeout(gotoGamePlayWhenReady, 1000);
            }

        });
}

function connect(){
        var requestParams = $("#connectForm").find(":input").serialize();
        $.ajax( "/?operation=connect&" + requestParams )
            .done(function(response) {
                if(response.indexOf("true") == 0) {
                    gotoGamePlayWhenReady();
                }else{
                    alert(response);
                }
            })
            .fail(function(data) {
                alert( "AJAX Failed. Please refresh the page" );
            });
}



function keepGameLobbyInfoUpdated(num_of_players) {
    $.ajax("/?operation=get_list_of_players_connected_to_host")
        .done(function (response) {
            var names = JSON.parse(response);
            var html = '<div class="well"><h4><strong>Players Currently Connected:</strong></h4><ul>';
            for(i in names){
                html += '<p>' + names[i] + '</p>';
            }
            html += '</ul><h4><small>Game will start when ' + (names.length - num_of_players) +
            ' more players connect.</small></h4><ul></div>';
            $('#host').html(html);
            setTimeout(function(){keepGameLobbyInfoUpdated(num_of_players)}, 600);
        });
}


function host(){
    var requestParams = $("#hostForm").find(":input").serialize();
    $.ajax( "/?operation=host_game&" + requestParams )
        .done(function(response) {
            if(response.indexOf("true") == 0){
                if($("#hostForm").find("input:checked[name=is_host_playing]").val() == "false")
                    nonPlayingHost = true;

                //disable controls for user
                $(":input").prop("disabled", true);

                keepGameLobbyInfoUpdated($("[name=number_of_players]").val());
                gotoGamePlayWhenReady();
            }else{
                alert(response);
            }

        })
        .fail(function(data) {
            alert( "AJAX Failed. Please refresh the page" );
        });
}

function freezeControls(){
    $(":input").prop("disabled", true);
}

function setOnchangeForIsHostPlayingRadioBtn(){
    $("#hostForm").find("input[name=is_host_playing]").change( "click", function(){
        if($("#hostForm").find("input:checked[name=is_host_playing]").val() == "true"){
            $('#hostPlayerNameSection').show();
        }else{
            $('#hostPlayerNameSection').hide();
        }

        showAddAiPlayerBtnIfMaxPlayersNotReach();
    });
}
function provideAIoptionsForHostSetupPanel(){
    $.ajax( "/?operation=get_list_of_available_ai")
        .done(function(data) {
            var availableAiArray = JSON.parse(data);
            availableAiArray.sort();
            var optionHtml = "";
            for(aiNameIndex in availableAiArray){
                var name = availableAiArray[aiNameIndex];
                optionHtml += '<li><a href="#" onclick="aiSelected(\'' + name + '\')">' + name + '</a></li>';
            }
            $("#aiOptions").html(optionHtml);
        });
    
}

function aiSelected(aiType){
    var aiPlayer = {};
    aiPlayer.name = "";
    aiPlayer.type = aiType;
    var count = 0;
    for(pIndex in aiPlayers){
        if(aiPlayers[pIndex].type == aiType) count++;
    }
    aiPlayer.name = aiType + ((count == 0) ? "" : "_" + (count+1));
    aiPlayers.push(aiPlayer);
    displaySelectedAiPlayers();

    showAddAiPlayerBtnIfMaxPlayersNotReach();

   }

function showAddAiPlayerBtnIfMaxPlayersNotReach(){
    if(isMaxAllowedAiPlayersReached())
        $("#aiAdder").hide();
    else
        $("#aiAdder").show();

    var max_players = $("[name=number_of_players]").val();
    var is_host_playing = ($("#hostForm").find("input:checked[name=is_host_playing]").val() == "true");
    if(is_host_playing){
        if(aiPlayers.length > max_players -1){
            aiPlayers.pop();
            displaySelectedAiPlayers();
        }
        else
            return false;
    }else{
        if(aiPlayers.length > max_players){
            aiPlayers.pop();
            displaySelectedAiPlayers();
        }
        else
            return false;
    }

}

function isMaxAllowedAiPlayersReached(){
    var max_players = $("[name=number_of_players]").val();
    var is_host_playing = ($("#hostForm").find("input:checked[name=is_host_playing]").val() == "true");
    if(is_host_playing){
        if(aiPlayers.length >= max_players -1)
            return true;
        else
            return false;
    }else{
        if(aiPlayers.length >= max_players)
            return true;
        else
            return false;
    }
}

function removeAIPlayer(aiName){
    var playerIndex;
    for(playerIndex in aiPlayers){
        if(aiPlayers[pIndex] == aiName) break;
    }
    aiPlayers.splice(playerIndex, 1);
    displaySelectedAiPlayers();

    showAddAiPlayerBtnIfMaxPlayersNotReach();



}

function displaySelectedAiPlayers(){
    var html = "";
    for(playerIndex in aiPlayers){
        var player = aiPlayers[playerIndex];
        html += "<p>AI Player: " + player.name + '<small> <a href="#" onclick="removeAIPlayer(\'' + player.name + '\')">(Remove)</a></small></p>';
        html += ' <input type="hidden" class="form-control" name="ai_player" value="' + player.type + "," + player.name + '">';
    }
    $("#hostAIoptions").html(html);

}