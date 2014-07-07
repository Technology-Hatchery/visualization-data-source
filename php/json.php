<?php
/**
 * Created by IntelliJ IDEA.
 * User: Alfred
 * Date: 7/2/2014
 * Time: 10:31 PM
 ..........*/
//$output = file_get_contents("../json/mobinsight/Survey.json");
$output = file_get_contents("../json/mobinsight/Analytics.json");
$json = json_decode($output);
echo var_dump($json);

?>