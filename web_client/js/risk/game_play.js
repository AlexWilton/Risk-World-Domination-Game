var game_state;

$( window ).load(setup);


function setup(){
    //get state
    $.ajax( "/?operation=get_state" )
        .done(function(data) {
            console.log(JSON.parse(data));
        })
        .fail(function() {
            alert( "error" );
        });
}