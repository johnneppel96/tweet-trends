<?php
//Function returns true if the date-string passed in resembles
//a valid date in yyyy-mm-dd format (mm and dd do NOT have preceding zeroes).
function isDateValid($date, $format = 'Y-n-j') { 
    $d = DateTime::createFromFormat($format, $date);
    // The Y ( 4 digits year ) returns TRUE for any integer with any number of digits so changing the comparison from == to === fixes the issue.
    return (bool) $d && $d->format($format) === $date;
}

//Function takes in a date-string in yyyy-mm-dd format (mm and dd do NOT have preceding zeroes),
//checks the date-string to see if it represents a valid date, modifies that date-string 
//if it represents a invalid date (e.g 2018-11-31) and returns that modified variable.
function fixDate($date) {
	$modifiedDate = $date;
	while (isDateValid($modifiedDate)==false) {
		//This will correct the date by subtracting a day from it until it resembles
		//an actual date. For example, April 31st will be corrected to April 30th.
		//Invalid dates such as February 30th and February 31th will result
		//in corrected dates of March 1st and 2nd respectively.
		$modifiedDate = date("Y-n-j", strtotime('-1 day', strtotime($modifiedDate)));
	}
	
	return $modifiedDate;
}
?>