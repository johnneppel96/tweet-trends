<?php

//All dates used within the project files will follow this specific date format (yyyy-mm-dd) and mm and dd DO NOT have preceding zeroes
 $format = "Y-n-j";
	 
//Function returns true if the date passed in (of type string) resembles
//a valid date in the format dictated by the $format global variable.
function isDateValid($date) {
	global $format;
    $d = DateTime::createFromFormat($format, $date);
    // The Y ( 4 digits year ) returns TRUE for any integer with any number of digits so changing the comparison from == to === fixes the issue.
    return (bool) $d && $d->format($format) === $date;
}

//Function takes in a date (of type string) in follows the format dictated by the $format global
//variable checks the date-string to see if it represents a valid date, modifies that date-string 
//if it represents a invalid date (e.g 2018-11-31) and returns that modified variable.
function fixDate($date) {
	global $format;
	$modifiedDate = $date;
	while (isDateValid($modifiedDate)==false) {
		//This will correct the date by subtracting a day from it until it resembles
		//an actual date. For example, April 31st will be corrected to April 30th.
		//Invalid dates such as February 30th and February 31th will result
		//in corrected dates of March 1st and 2nd respectively.
		$modifiedDate = date($format, strtotime('-1 day', strtotime($modifiedDate)));
	}
	
	return $modifiedDate;
}


//Function takes in two dates (of type string) and returns true if the
// number of days between the 'from' and the 'to' dates is more than 90. This function is
//necessary because a user selecting to view large amounts data outside of this timeframe can
//cause server-side memory issues and bad website performance as well.
function isDateRangeGreaterThan90Days($from_date, $to_date) {
	global $format;
	//Following creates DateTime objects, which can be used to execute
	//various operations such as getting the number of days in between the two.
	$from_date_time = DateTime::createFromFormat($format, $from_date);
	$to_date_time = DateTime::createFromFormat($format, $to_date);
	
	if($from_date_time > $to_date_time) {
		return false;
	}
	
	//Returns DateInterval object or False on failure
	$date_interval = date_diff($from_date_time, $to_date_time);
	
	//The value of this will be the string: "X days".
	$day_difference = $date_interval->format('%a Days');
	$extracted_days= '';
	
	//This will extract the numerical value from the
	//$day_difference string
	for($i=0; $i< strlen($day_difference); $i++) {
		if ($day_difference[$i]==' ') {
			break;
		}
		$extracted_days = $extracted_days . $day_difference[$i];
}
	$extracted_days = (int)$extracted_days;
	
	if($extracted_days > 90) {
		return true;
	}
	else {
		return false;
	}
	
}


//Function takes in a date and returns a date forwarded to 90 days later.
function alterDateTo90DaysLater($date) {
	global $format;
	$created_date = date($format, strtotime('+90 days', strtotime($date)));
	
	return $created_date;
}
?>