$( document ).ready(function(){

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


