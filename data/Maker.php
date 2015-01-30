<?php

$result = new stdClass();
$result->data = "map";


//Countries in each Continent Data
$handle = fopen("data.csv", "r");
$result->continents = new stdClass();
$lastContID = "";
while (($line = fgets($handle)) !== false) {
    list($continID, $continName, $countryID, $countryName) = split(",",$line,6);
    if($continID == ""){
        $continID = $lastContID;
    }else{
        $lastContID = $continID;
        $result->continents->$continID = array();
    }
    if($continID != ""){
        array_push($result->continents->$continID, $countryID);
    }
}
fclose($handle);


//connections values (hard code mappings from a to b
$result->connections = array();
$a = array(0,0,0, 1,1,1,2,2,2, 3,3,4,4,4,5,6,6,7,8,9, 9, 10,10,11,11, 13,13,14,14,14,15,15,15,15,15,16,16,17,17,18,18,19,19,19, 20,20,20,21,21,22,22,23,23,23,24, 26,26,26,27,27,27,27,28,28,29,29,29,30,31,31,33,33,33,34,34,35,36,37, 38,38,39,39,40);
$b = array(1,3,29,3,2,4,4,5,13,4,6,5,6,7,7,7,8,8,9,10,10,11,12,12,20, 14,16,16,17,15,17,19,26,33,35,17,18,18,19,19,20,20,21,35, 21,22,23,23,35,23,24,25,24,35,25, 27,34,33,28,30,31,34,29,30,30,32,31,31,32,34,34,35,36,36,37,36,37,38, 39,40,40,41,41);
for($i = 0; $i < count($a); $i += 1){
    array_push($result->connections,array($a[$i], $b[$i]));
}




//continent values (hard coded)
$result->continent_values = new stdClass();
$values = array(5,2,5,3,7,2);

$i = 0;
foreach($values as $value){
    $str = $i . "";
    $result->continent_values->$str = $value;
    $i += 1;
}


//Country Name Data
$handle = fopen("data.csv", "r");
$result->country_names = new stdClass();
while (($line = fgets($handle)) !== false) {
    list($continID, $continName, $countryID, $countryName) = split(",",$line,6);
    $result->country_names->$countryID = $countryName;
}
fclose($handle);


//Continent Name Data
$result->continent_names = new stdClass();
$handle = fopen("data.csv", "r");
while (($line = fgets($handle)) !== false) {
    list($continID, $continName, $countryID, $countryName) = split(",",$line,6);
    if($continID != ""){
        $result->continent_names->$continID = $continName;
        echo $continID . " " . $continName . "\n";
    }
}
fclose($handle);


echo stripslashes(json_encode($result));