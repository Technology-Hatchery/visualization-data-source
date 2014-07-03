<?php
/**
 * Created by IntelliJ IDEA.
 * User: Alfred
 * Date: 7/2/2014
 * Time: 6:05 PM
 */
//Connect to Cloud SQL
$conn = mysql_connect("173.194.253.254", "root", "Cc17931793");
if (!$conn) {
    die('Connect Error (' . mysql_error());
}

//Select Database
$db_selected = mysql_select_db('Alfred', $conn);
if (!$db_selected) {
    die('Can\'t use db : ' . mysql_error());
}

$sql = "SELECT Age, Weight FROM AgeWeight;";

// Show results as CSV
$result = mysql_query($sql);    //perform the query
echo "Age,Weight\n";
while ($row = mysql_fetch_assoc($result)) {
    echo $row['Age'].",".$row['Weight']."\n";
}
echo "<br /><br />";

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

?>