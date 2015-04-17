var nonPlayingHost = false;

$( document ).ready(function(){

    $("#hostButton").click(function(){
        var requestParams = $("#hostForm").find(":input").serialize();
        $.ajax( "/?operation=host_game&" + requestParams )
            .done(function(response) {
                if(response.indexOf("true") == 0){
                    if($("#hostForm").find("input:checked[name=is_host_playing]").val() == "false")
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

    $("#hostForm").find("input[name=is_host_playing]").change( "click", function(){
        if($("#hostForm").find("input:checked[name=is_host_playing]").val() == "true"){
            $('#hostPlayerNameSection').show()
        }else{
            $('#hostPlayerNameSection').hide()
        }
    })


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
                }, 1700);
            }else{
                setTimeout(gotoGamePlayWhenReady, 1000);
            }

        });
}
