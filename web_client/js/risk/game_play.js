var game_state;

$( window ).load(setup);


function setup(){
    //get state
    $.ajax( "example.php" )
        .done(function() {
            alert( "success" );
        })
        .fail(function() {
            alert( "error" );
        })
        .always(function() {
            alert( "complete" );
        });

// Pe
}