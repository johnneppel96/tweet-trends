<?php
		/*This PHP file is for setting the variables that are used in 
		in the queries executed to the database and the displayed form
		fields.*/

		//This is for when the form data has been submitted by the user
        if(isset($_POST['submit'])) {
            $from_month = $_POST['from_month'];
            $from_day = $_POST['from_day'];
            $from_year = $_POST['from_year'];
            $combined_from_date = $from_year . '-' . $from_month .
                '-' . $from_day; //yyyy-mm-dd format, used for SQL query
			
            
            $to_month = $_POST['to_month'];
            $to_day = $_POST['to_day'];
            $to_year = $_POST['to_year'];
             $combined_to_date = $to_year . '-' . $to_month .
                '-' . $to_day;  //yyyy-mm-dd format, used for SQL query.
			
			//The following checks for invalid dates from the form (e.g 2018-2-31) in order
			//to prevent errors in queries later executed to the database. Invalid date variables
			//will be modified to represent a valid one.
			if(isDateValid($combined_from_date) ==false) {
				$combined_from_date = fixDate($combined_from_date);
				$from_day = date('j', strtotime($combined_from_date));
			}
			
			if(isDateValid($combined_to_date)==false) {
				$combined_to_date = fixDate($combined_to_date);
				$to_day = date('j', strtotime($combined_to_date));
			}
			
			//The corresponding month string (e.g May, July, etc) for
        	// the date that will be displayed as the default
        	// value for the form drop-down menu
			$from_month_string = date('F', strtotime($combined_from_date));
			$to_month_string = date('F', strtotime($combined_to_date));
			
            $Tweet_hashtag = $_POST['Tweet_hashtag'];
        }
		
		//This is for the initial uploading of the webpage, in which the last 7 days worth
		//of collected Tweet data will be displayed onto the maps.
        else {
			  //The 'to' date is today's current date
			   $combined_to_date = date("Y-n-j");
			
              //returns the number of seconds FROM Jan 1, 1970 TO the following dates.
			  //The value is stored for further operations regarding those dates
               $week_agos_date_amt = strtotime("-7 days");
              //converts to the string/date representation: y-m-d
               $combined_from_date= date("Y-n-j",$week_agos_date_amt);
			
				//Pulls the appropriate String date parameters from last weeks date.
			    //These will be displayed as the default (top) value in the date drop-down
		        //menus to show the user what dates are being represented by the intial
			    //load of the webpage.
				$from_month_string = date('F', $week_agos_date_amt);
				$from_day = date('j', $week_agos_date_amt);
				$from_year = date('Y', $week_agos_date_amt);
			
				$to_month_string = date('F');
				$to_day = date('j'); //contains no proceeding zeroes (no 03,04,etc)
				$to_year = date('Y');
				
				//This will be the default Tweet metadata displayed at the initial
			    // loading of the web page.
				$Tweet_hashtag = '#Metoo';
            }
?>