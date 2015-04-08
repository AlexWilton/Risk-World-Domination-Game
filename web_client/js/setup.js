$( document ).ready(function(){

    $("#hostButton").click(function(){
        var requestParams = $("#hostForm").find(":input").serialize();
        $.ajax( "/?operation=host_game&" + requestParams )
            .done(function(data) {
                alert(data);
            })
            .fail(function(data) {
                alert( "AJAX Failed. Please refresh the page" );
            });

    });


    $("#connectButton").click(function(){
        var requestParams = $("#connectForm").find(":input").serialize();
        $.ajax( "/?operation=connect&" + requestParams )
            .done(function(data) {
                alert(data);
            })
            .fail(function(data) {
                alert( "AJAX Failed. Please refresh the page" );
            });
    });

});


