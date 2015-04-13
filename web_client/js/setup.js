var nonPlayingHost = false;

$( document ).ready(function(){

    $("#hostButton").click(function(){
        var requestParams = $("#hostForm").find(":input").serialize();
        $.ajax( "/?operation=host_game&" + requestParams )
            .done(function(response) {
                if(response.indexOf("true") == 0){
                    if(!$("#hostForm").find(":input[name=is_host_playing]").val()== "true")
                        nonPlayingHost = true;
                    gotoGamePlayWhenReady();
                }else{
                    alert(response);
                }

            })
            .fail(function(data) {
                alert( "AJAX Failed. Please refresh the page" );
            });

    });
    $("#connectButton").click(function(){
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
    });



});

function gotoGamePlayWhenReady(){
    $.ajax( "/?operation=move_to_game_play")
        .done(function(response) {
            if(response.indexOf("true") == 0){
                alert("Game Started!");
                if(nonPlayingHost)
                    window.location.href = "/server.html";
                else
                    window.location.href = "/play.html";
            }else{
                setTimeout(gotoGamePlayWhenReady, 1000);
            }

        });
}
