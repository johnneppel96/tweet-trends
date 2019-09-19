<?php
		/*This PHP file is for setting the variables that are used in 
		in the queries executed to the database and the displayed form
		fields.*/

		//When the form data has been submitted by the user
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
			
			//This checks whether the user requested to view data from a date-range greater than 90
			//days. Due to the large scale of data which can cause memory and performance issues with the
			//application, the following will alter the "TO" date to 90 days later than the "FROM" date that
			//the user selected instead of the original selected dates.
			if(isDateRangeGreaterThan90Days($combined_from_date, $combined_to_date)==true) {
				 $combined_to_date = alterDateTo90DaysLater($combined_from_date);
				 $to_day = date('j', strtotime($combined_to_date));
				 $to_year = date('Y', strtotime($combined_to_date));
				?>
				<script> alert("Due to the large quantity of data, your selected date-range has" +
							   " been altered to represent a spread of 90 days: " +
							    "<?php echo date('F j, Y', strtotime($combined_from_date)) . ' - ' .
									date('F j, Y', strtotime($combined_to_date)) ?>."
							  );</script>
							  
				<?php 
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
			   $combined_to_date = date($format);
			
              //converts to the string/date representation: y-m-d
               $combined_from_date= date($format, strtotime("-7 days"));
			
				//Pulls the appropriate String date parameters from last weeks date.
			    //These will be displayed as the default (top) value in the date drop-down
		        //menus to show the user what dates are being represented by the intial
			    //load of the webpage.
				$from_month_string = date('F', strtotime("-7 days"));
				$from_day = date('j', strtotime("-7 days"));
				$from_year = date('Y', strtotime("-7 days"));
			
				$to_month_string = date('F');
				$to_day = date('j'); //contains no proceeding zeroes (no 03,04,etc)
				$to_year = date('Y');
				
				//This will be the default Tweet metadata displayed at the initial
			    // loading of the web page.
				$Tweet_hashtag = '#Metoo';
            }
?>