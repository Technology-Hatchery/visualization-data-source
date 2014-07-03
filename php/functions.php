<?php
/**
 * Created by IntelliJ IDEA.
 * User: Alfred
 * Date: 7/1/2014
 * Time: 11:59 AM
 */

// $server: In the format of <server>,<port> when using a non default port number
function getMSSQL($server,$user,$password,$database,$table,$separator,$debug=0)
{
    $result = "";
    $connectionInfo = array("UID" => $user, "PWD" => $password, "LoginTimeout" => 3, "Database" => $database);
    $conn = sqlsrv_connect($server, $connectionInfo);
    if ($conn) {
        //echo "Connection established.<br />";
    } else {
        return ("Connection could not be established.<br />");
        //die( print_r( sqlsrv_errors(), true));
    }

    // Generate query
    $sql = 'SELECT * FROM [' . $database . '].[dbo].[' . $table . ']';
    if ($debug != 0) {
        $result = $result . "<b>Executing the following SQL statement:</b><br />";
        $result = $result . $sql . "<br /><br />";
    }
    $stmt = sqlsrv_query($conn, $sql);
    if ($stmt === false) {
        die(print_r(sqlsrv_errors(), true));
    }
    //echo $stmt;

    /*$row_count = sqlsrv_num_rows( $stmt );
    if ($row_count === false)
        echo "Error in retrieveing row count.";
    else
        echo $row_count;

    if ($stmt) {
        $rows = sqlsrv_has_rows( $stmt );
        if ($rows === true)
            echo "There are rows. <br />";
        else
            echo "There are no rows. <br />";
    }*/

    // Print out metadata
    $output = "";
    foreach (sqlsrv_field_metadata($stmt) as $fieldMetadata) {
        $output = $output . $fieldMetadata["Name"] . $separator;
    }
    if ($debug != 0) {
        $result = $result . "<b>Here is the result:</b><br />";
    }
    $output = substr($output, 0, -1*strlen($separator));
    $result = $result.$output.($debug!=0?"<br />":"\n");

    // Print out fields individually
    $numFields = sqlsrv_num_fields( $stmt );
    while( sqlsrv_fetch( $stmt )) {
        // Iterate through the fields of each row.
        $output = "";
        for ($i = 0; $i < $numFields; $i++) {
            $output = $output.sqlsrv_get_field($stmt, $i    ).$separator;
        }
        $output = substr($output, 0, -1*strlen($separator));
        $result = $result.$output.($debug!=0?"<br />":"\n");
    }

    /*// Retrieve each row as an object.
    while( $obj = sqlsrv_fetch_object( $stmt )) {  // Because no class is specified, each row will be retrieved as a stdClass object.
        echo $obj->name.", ".$obj->location.", ".$obj->qty."<br />";
    }*/

    if( ($errors = sqlsrv_errors() ) != null) {
        foreach ($errors as $error) {
            $result = $result."SQLSTATE: " . $error['SQLSTATE'] . "<br />";
            $result = $result."code: " . $error['code'] . "<br />";
            $result = $result."message: " . $error['message'] . "<br />";
        }
    }
    return($result);
}

function csvToJson($csv) {
    $rows = explode("\n", trim($csv));
    $keys = array_shift($rows);     // Removes first element and returns the first element
    $keys = explode(",", trim($keys));
    $callback = function ($row) use ($keys) {
        return array_combine($keys, str_getcsv($row));
    };
    $csvArr = array_map($callback, $rows);
    $json = json_encode($csvArr);

    return $json;
}

//Makes an associate array (the keys return arrays)
function csvToKeyArray($csv) {
    function parse_row($row) {
        return array_map('trim', explode(',', $row));
    }

    $rows   = str_getcsv($csv, "\n");
    $keys   = parse_row(array_shift($rows));
    $keys = range(0,sizeOf($keys)-1,1);   //Uses numbers instead of the first row
    $result = array();

    foreach ($rows as $row) {
        $row = parse_row($row);
        //$row = array_pad($row, 3, NULL);
        $result[] = array_combine($keys, $row);
    }
    return($result);
}


// $types:  The types of the columns
// $rows: The values for the rows
// ---------------
// Assumes the first row are the headers
// Uses integers to index the rows and columns
// This outputs a JSON object suitable for importing into a Google Visualization chart
function csvToDataFrame($types, $csv) {

    $rows = explode("\n",trim($csv));
    $columns = str_getcsv(array_shift($rows));
    $numCols = sizeOf($types);
    $numRows = sizeOf($rows);
    $rows = csvToKeyArray($csv);

    $output = "";
    $output = $output.
        '{
            "cols": [
                    ';
    for ($x=0; $x<$numCols; $x++) {
        $output = $output.
                    '{"label":"'.trim($columns[$x]).'","type":"'.trim($types[$x]).'"}'.($x+1!=$numCols?',
                    ':'');
    }
    $output = $output.'
                    ],
            "rows": [
                    ';
    for ($x=0; $x<$numRows; $x++) {
        $output = $output.
                    '{"c":[';
        for ($y=0; $y<$numCols; $y++) {
            $output = $output.
                            '{"v":'.($types[$y]=='string'?'"':'').$rows[$x][$y].($types[$y]=='string'?'"':'').'}'.($y+1!=$numCols?',':'');
        }
        $output = $output.']}'.($x+1!=$numRows?',
                    ':'');
    }
    $output = $output.'
                    ]
        }';
    return($output);

/* -------------
Sample of the JSON format expected by Google Visualization.
Some fields were omitted to make it easier to code.

{
    "cols": [
        {"label":"Age","type":"number"},
        {"label":"Weight","type":"number"}
    ],
    "rows": [
        {"c":[{"v":8},{"v":12}]},
        {"c":[{"v":4},{"v":5.5}]},
        {"c":[{"v":11},{"v":14}]},
        {"c":[{"v":4},{"v":5}]},
        {"c":[{"v":3},{"v":3.5}]},
        {"c":[{"v":6.5},{"v":7}]}
    ]
}
 -------------
*/

}

// Connects to MySQL and CloudSQL
// $server: As an IP address
// $user:
// $password:
// $database:
// $table:
// $separator:
function getCloudSQL ($server,$user,$password,$database,$table,$separator,$debug=0) {

    //Connect to Cloud SQL
    $conn = new mysqli($server, $user, $password, $database);
    //$conn = mysql_connect($server, $user, $password);
    if ($conn->connect_errno) {
        echo "Failed to connect to MySQL: (" . $conn->connect_errno . ") " . $conn->connect_error;
    }

    $sql = "SELECT Age, Weight FROM ".$table.";";

    // Show results as CSV
    $result = $conn->query($sql); //perform the query
    $output = '';
    $output = $output."Age,Weight\n";
    $result->data_seek(0);
    while ($row = $result->fetch_assoc()) {
        $output = $output.$row['Age'].$separator.$row['Weight']."\n";
    }

    return($output);
    //echo "<br /><br />";

    /*
    //Show Results in Table
    echo "<h3>Results from the Google Cloud SQL</h3>";
    echo "<table class=simpletable border=1>";
    echo "<tr><th align=left>Age</th><th>Weight</th></tr>";
    $result = mysql_query($sql);        //perform the query
    while ($row = mysql_fetch_assoc($result)) {
        echo "<tr><td align=left>" . $row['Age'] . "</td>";
        echo "<td align=center class=addComas>" . $row['Weight'] . "</td></tr>";
    }
    echo "</table>";
    */
}


