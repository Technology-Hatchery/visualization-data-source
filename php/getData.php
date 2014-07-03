<?php

include 'functions.php';

// Parameters
$source = 2;
$debug = 0;

if ($source == 0) {
    // Local File
    // ------
    //$output = file_get_contents("../json/sampleData1.json");
    $output = file_get_contents("../csv/sampleData.csv");
    echo $output;
} elseif ($source == 1) {
    // MSSQL
    // ------
    $server = '54.85.42.208,1433'; // Server in the this format: <server>,<port> when using a non default port number
    $user = 'alfred.wechselberger';
    $password = 'Cc17931793';
    $database = 'alfred';
    $table = 'weights'; // Assumes that 'dbo' is in front of the table name
    $separator = ", ";

    $result = getMSSQL($server, $user, $password, $database, $table, $separator);
    $types = array("number", "number");
    $output = csvToDataFrame($types, $result);
    echo $output;
} elseif ($source == 2) {
    // MySQL
    // -----
    // Create connection
    $server = '173.194.253.254';
    $user = 'root';
    $password = 'Cc17931793';
    $database = 'Alfred';
    $table = 'AgeWeight';  // Assumes that 'dbo' is in front of the table name
    $separator = ", ";
    $result = getCloudSQL($server,$user,$password,$database,$table,$separator);
    $types = array("number", "number");
    $output = csvToDataFrame($types,$result);
    echo $output;
} else {
    echo "Invalid source!";
}


?>