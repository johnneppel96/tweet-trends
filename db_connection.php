
<?php
//This file is kept within a private directory on the server.
$username = 'root';
$password = '!J0hnnymax';
$db_name = 'tweet_trends_data';         //URL below
global $db_connection;
$db_connection = new mysqli('localhost',$username, $password, $db_name) or die('something broke');
?>