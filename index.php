 <?php
    include "db_connection.php";
	include "date_validator.php";
    ?>
     <?php
		//This is for when the form data has been submitted by the user
        if(isset($_POST['submit'])) {
            //variable determines whether the getInput() javascript function
            //shall be executed.
            $display_form_data = true; 
			
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
               $display_form_data = false;
			   $current_date = date("Y-n-j");
			
              //returns the number of seconds FROM Jan 1, 1970 TO the following dates.
			  //The value is stored for further operations regarding those dates
               $week_agos_date_amt = strtotime("-7 days");
              //converts to the string/date representation: y-m-d
               $week_ago_date= date("Y-n-j",$week_agos_date_amt);
			
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

<!DOCTYPE html >
<html>
   <head>
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
    <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
    <title>Tweet Trends</title>
    <style>
      /* Always set the map height explicitly to define the size of the div
       * element that contains the map. */
      #map {
        height: 50%;

      }
      #markerMap{
      	height: 50%;
      }
      /* Optional: Makes the sample page fill the window. */
      html, body {
        height: 100%;
        margin: 0;
        padding: 0;

      }
      h1{
              text-shadow: 2px 2px 2px #FFFFFF;
      }

      form {
      	  color: white;
      	  padding: 10px;

  	  }	
  	  button{
  	  	padding: 10px;
  	  }


      #floating-panel {
        position: relative;
        top: 10px;
        left: 25%;
        z-index: 5;
        background-color: #fff;
        padding: 5px;
        border: 1px solid #999;
        text-align: center;
        font-family: 'Roboto','sans-serif';
        line-height: 30px;
        padding-left: 10px;
      }
      
      #floating-panel {
        background-color: #fff;
        border: 1px solid #999;
        left: 0%;
        padding: 5px;
        position: bottom;
        z-index: 5;


      }

	  .show {display: block;}
    </style>
  </head>

  <body style = "background-color:black;">
  
<h1 style="color:skyblue;text-align:center;font-size:50px;">
	#TweetTrends
</h1>
  <p style="color:skyblue;text-align:center;">
    This site is used to track where #MeToo tweets are occurring. It displays both a heatmap and a marker map, which you can edit in the forms below.
    To look at corresponding hashtags related to #MeToo, simply hover the cursor over a marker and it will be displayed.
  </p>
  <div style="display:flex;">    
	<form action = "index.php" method = "POST" style="margin: 0 auto;text-align:center;">
    	Select two dates to show Tweets in between the two.&nbsp; &nbsp; <b>From: &nbsp;</b> Month &nbsp;
	  <select name ="from_month" id="selectMonth">
    	<option value= "<?php echo date('n', strtotime($from_month_string)); ?>" selected hidden><?php echo $from_month_string; ?></option>
	    <option value="1">January</option>
	    <option value="2">February</option>
	    <option value="3">March</option>
	    <option value="4">April</option>
	    <option value="5">May</option>
	    <option value="6">June</option>
	    <option value="7">July</option>
	    <option value="8">August</option>
	    <option value="9">September</option>
	    <option value="10">October</option>
	    <option value="11">November</option>
	    <option value="12">December</option>
	  </select>
        &nbsp;
         Day
	  <select name = "from_day" id="selectDay">
    	<option value= "<?php echo $from_day; ?>" selected hidden> <?php echo $from_day; ?></option>
	    <option value="1">1</option>
	    <option value="2">2</option>
	    <option value="3">3</option>
	    <option value="4">4</option>
	    <option value="5">5</option>
	    <option value="6">6</option>
	    <option value="7">7</option>
	    <option value="8">8</option>
	    <option value="9">9</option>
	    <option value="10">10</option>
	    <option value="11">11</option>
	    <option value="12">12</option>
	    <option value="13">13</option>
	    <option value="14">14</option>
	    <option value="15">15</option>
	    <option value="16">16</option>
	    <option value="17">17</option>
	    <option value="18">18</option>
	    <option value="19">19</option>
	    <option value="20">20</option>
	    <option value="21">21</option>
	    <option value="22">22</option>
	    <option value="23">23</option>
	    <option value="24">24</option>
	    <option value="25">25</option>
	    <option value="26">26</option>
	    <option value="27">27</option>
	    <option value="28">28</option>
	    <option value="29">29</option>
	    <option value="30">30</option>
	    <option value="31">31</option>
	  </select>
	    &nbsp;
		Year
		<select name = "from_year" id = "selectYear">
		<option value= "<?php echo $from_year; ?>" selected hidden> <?php echo $from_year; ?></option>
			<option value="2019">2019</option>
	    	<option value="2018">2018</option>
	    	<option value="2017">2017</option>
	    </select>
	    &nbsp;
	    &nbsp;
        &nbsp;
        <b>To:</b>
	  &nbsp; Month
	  <select name = "to_month" id="selectSecondMonth">
     	<option value="<?php echo date('n', strtotime($to_month_string)); ?>" selected hidden><?php echo $to_month_string; ?></option>
	    <option value="1">January</option>
	    <option value="2">February</option>
	    <option value="3">March</option>
	    <option value="4">April</option>
	    <option value="5">May</option>
	    <option value="6">June</option>
	    <option value="7">July</option>
	    <option value="8">August</option>
	    <option value="9">September</option>
	    <option value="10">October</option>
	    <option value="11">November</option>
	    <option value="12">December</option>
	  </select>
    &nbsp;
	  Day
	  <select name = "to_day" id="selectSecondDay">
    	<option value= "<?php echo $to_day; ?>" selected hidden><?php echo $to_day; ?></option>
	    <option value="1">1</option>
	    <option value="2">2</option>
	    <option value="3">3</option>
	    <option value="4">4</option>
	    <option value="5">5</option>
	    <option value="6">6</option>
	    <option value="7">7</option>
	    <option value="8">8</option>
	    <option value="9">9</option>
	    <option value="10">10</option>
	    <option value="11">11</option>
	    <option value="12">12</option>
	    <option value="13">13</option>
	    <option value="14">14</option>
	    <option value="15">15</option>
	    <option value="16">16</option>
	    <option value="17">17</option>
	    <option value="18">18</option>
	    <option value="19">19</option>
	    <option value="20">20</option>
	    <option value="21">21</option>
	    <option value="22">22</option>
	    <option value="23">23</option>
	    <option value="24">24</option>
	    <option value="25">25</option>
	    <option value="26">26</option>
	    <option value="27">27</option>
	    <option value="28">28</option>
	    <option value="29">29</option>
	    <option value="30">30</option>
	    <option value="31">31</option>
	  </select>
	  &nbsp;
		Year
		<select name = "to_year" id = "selectSecondYear">
			<option value= "<?php echo $to_year; ?>" selected hidden><?php echo $to_year; ?></option>
			<option value="2019">2019</option>
	    	<option value="2018">2018</option>
	    	<option value="2017">2017</option>
	    </select>
	    &nbsp;
		Tweet
		<select name = "Tweet_hashtag" id = hashtag>
			<option value = "#Metoo"> #MeToo </option>
		</select>
          &nbsp;
	
		<input type="submit" name="submit" value = "Display">
      </form>
      </div>
  <div id= "markerMap"></div>

   <div id="map"></div> <!--This will contain the map with the heatmap raster -->

   <div id="floating-panel">
      <button onclick="toggleHeatmap()">Toggle Heatmap</button>
      <button onclick="changeGradient()">Change gradient</button>
      <button onclick="changeRadius()">Change radius</button>
      <button onclick="changeOpacity()">Change opacity</button>
    </div>

<p style="color:skyblue;text-align:center;">
  This website was created by the Monmouth University Software Engineering department, graduating class of 2019, made by: 
</p>

<p style="color:skyblue;text-align:center;padding:10px;">
  John Neppel
</p>
<p style="color:skyblue;text-align:center;padding:10px;">
  Thomas McHugh
</p>
       
<script>
var heatmapLayer;
var heatMapData = [];
var markerData = [];
var map; //basemap
var markerMap;
var image = "https://maps.google.com/mapfiles/ms/icons/blue-dot.png";
var infoWindow = new google.maps.InfoWindow;

function initMap() {
var customGradient = [
      'rgba(0, 255, 255, 0)',
      'rgba(0, 255, 255, 1)',
      'rgba(0, 191, 255, 1)',
      'rgba(0, 127, 255, 1)',
      'rgba(0, 63, 255, 1)',
      'rgba(0, 0, 255, 1)',
      'rgba(0, 0, 223, 1)',
      'rgba(0, 0, 191, 1)',
      'rgba(0, 0, 159, 1)',
      'rgba(0, 0, 127, 1)',
      'rgba(63, 0, 91, 1)',
      'rgba(127, 0, 63, 1)',
      'rgba(191, 0, 31, 1)',
      'rgba(255, 0, 0, 1)'
    ];
    //Creates a base map for the marker map
    markerMap = new google.maps.Map(document.getElementById('markerMap'), {
    center: new google.maps.LatLng(40, -90),
    zoom: 4,
    styles: [
            {elementType: 'geometry', stylers: [{color: '#939393'}]},
            {
              featureType: 'road',
              elementType: 'geometry',
              stylers: [{color: '#696969'}]
            },
            {
              featureType: 'road',
              elementType: 'geometry.stroke',
              stylers: [{color: '#696969'}]
            },
            {
              featureType: 'road.highway',
              elementType: 'geometry',
              stylers: [{color: '#696969'}]
            },
            {
              featureType: 'road.highway',
              elementType: 'geometry.stroke',
              stylers: [{color: '#696969'}]
            },
            {
              featureType: 'transit',
              elementType: 'geometry',
              stylers: [{color: '#2f3948'}]
            },
            {
              featureType: 'water',
              elementType: 'geometry',
              stylers: [{color: '#000000'}]
            },
          ]
    });
 //creates basemap in the map html container (which will have a heatmap raster overlay)
    map = new google.maps.Map(document.getElementById('map'), {
    center: new google.maps.LatLng(40, -90),
    zoom: 4,
    styles: [
            {elementType: 'geometry', stylers: [{color: '#939393'}]},
            {
              featureType: 'road',
              elementType: 'geometry',
              stylers: [{color: '#696969'}]
            },
            {
              featureType: 'road',
              elementType: 'geometry.stroke',
              stylers: [{color: '#696969'}]
            },
            {
              featureType: 'road.highway',
              elementType: 'geometry',
              stylers: [{color: '#696969'}]
            },
            {
              featureType: 'road.highway',
              elementType: 'geometry.stroke',
              stylers: [{color: '#696969'}]
            },
            {
              featureType: 'transit',
              elementType: 'geometry',
              stylers: [{color: '#2f3948'}]
            },
            {
              featureType: 'water',
              elementType: 'geometry',
              stylers: [{color: '#000000'}]
            },
          ]
    });
    
     //Defines the heatmap raster layer
    heatmapLayer = new google.maps.visualization.HeatmapLayer({  
          data: heatMapData,
          map: map,
          opacity: 1,
          radius: 2.5,
          dissipating: false,
          gradient: customGradient
        });
    
    <?php
    if($display_form_data == true) { ?>
        displayInputData();
    
    <?php }
    if($display_form_data == false) { ?>
        displayPastWeeksData();
    <?php } ?>
    
   } //end of initMap
      
    
function displayPastWeeksData() {
	removeMarkers(); //clears map in preparation for new marker points
	var newDataSet = [];
    var hashtags = '';
    var latitude = 0.0;
    var longitude = 0.0;
    var marker;

	<?php
		if($display_form_data == false) {
			?>
    <?php           
    $query = "SELECT latitude, longitude, Hashtags FROM tweet_data WHERE
     tweet_timestamp >= '$week_ago_date' AND tweet_timestamp <= '$current_date' 
      AND query_tag_id = (SELECT Query_Tag_ID FROM tweet_tags WHERE QueryTagText = '$Tweet_hashtag') ";
    $result = mysqli_query($db_connection, $query) or  die("Query execution failed " .mysqli_error($db_connection));
    while($row = mysqli_fetch_assoc($result)) {
                $latitude = $row['latitude'];
                $longitude = $row['longitude'];
                $Hashtags_list = $row['Hashtags']; 
        ?>
            

       hashtags = "<?php echo $Hashtags_list; ?>";
       latitude = <?php echo $latitude; ?>;
       longitude = <?php echo $longitude; ?>;
        
        //For the heat map; all points get the same default weight of 1
     	newDataSet.push(new google.maps.LatLng(latitude, longitude)); 

     	 marker = new google.maps.Marker({
          position: new google.maps.LatLng(latitude, longitude),
          visible: true,
          icon: image,
          map: markerMap,
          title: hashtags
        });
        
        //Adds marker to the marker map's data list
      	markerData.push(marker);
      <?php } //end of while ?>
	<?php } //end of if ?>
    
    heatmapLayer.setData(newDataSet);
} //end displayPastWeeksData
    
 
function toggleHeatmap() {
    heatmapLayer.setMap(heatmapLayer.getMap() ? null : map);
  }

function changeGradient() {
    var customGradient = [
      'rgba(0, 255, 255, 0)',
      'rgba(0, 255, 255, 1)',
      'rgba(0, 191, 255, 1)',
      'rgba(0, 127, 255, 1)',
      'rgba(0, 63, 255, 1)',
      'rgba(0, 0, 255, 1)',
      'rgba(0, 0, 223, 1)',
      'rgba(0, 0, 191, 1)',
      'rgba(0, 0, 159, 1)',
      'rgba(0, 0, 127, 1)',
      'rgba(63, 0, 91, 1)',
      'rgba(127, 0, 63, 1)',
      'rgba(191, 0, 31, 1)',
      'rgba(255, 0, 0, 1)'
    ]
    heatmapLayer.set('gradient', heatmapLayer.get('gradient') ? null : customGradient);
  }
     
function changeRadius() {
    heatmapLayer.set('radius', heatmapLayer.get('radius') ? null : 1);
  }
     
      
function changeOpacity() {
    heatmapLayer.set('opacity', heatmapLayer.get('opacity') ? null : 1);
  }     

function myFunction() {
  document.getElementById("myDropdown").classList.toggle("show");
}

function removeMarkers(){
    var empty = [];
    for (var i = 0; i < markerData.length; i++) {
          markerData[i].setMap(null);
    }
    heatmapLayer.setData(empty);
    heatMapData = [];
    markerData = [];
}

function displayInputData() {
	removeMarkers(); //clears map in preparation for new marker points
	var newDataSet = [];
    var hashtags ='';
    var latitude =0.0;
    var longitude = 0.0;
    var marker;
    <?php
		if($display_form_data == true) {
			?>
<?php 
    $query = "SELECT latitude, longitude, Hashtags FROM tweet_data WHERE
     tweet_timestamp >= '$combined_from_date' AND tweet_timestamp <= '$combined_to_date' 
      AND query_tag_id = (SELECT Query_Tag_ID FROM tweet_tags WHERE QueryTagText = '$Tweet_hashtag') ";
    $result = mysqli_query($db_connection, $query) or  die("Query execution failed " . mysqli_error($db_connection));
    while($row = mysqli_fetch_assoc($result)) {
                $latitude = $row['latitude'];
                $longitude = $row['longitude'];
                $Hashtags_list = $row['Hashtags'];
        ?>

       hashtags = "<?php echo $Hashtags_list; ?>";
       latitude = <?php echo $latitude; ?>;
       longitude = <?php echo $longitude; ?>;
        
        //For the heat map
     	newDataSet.push(new google.maps.LatLng(latitude, longitude)); 

     	 marker = new google.maps.Marker({
          position: new google.maps.LatLng(latitude, longitude),
          visible: true,
          icon: image,
          map: markerMap,
          title: hashtags
        });
   
        //FOR THE MARKER MAP
      	markerData.push(marker);
      <?php } //end of while ?>
	
    heatmapLayer.setData(newDataSet);
	<?php } //end if ?>
} //end getInput()

    
// Close the dropdown if the user clicks outside of it
window.onclick = function(event) {
  if (!event.target.matches('.dropbtn')) {
    var dropdowns = document.getElementsByClassName("dropdown-content");
    var i;
    for (i = 0; i < dropdowns.length; i++) {
      var openDropdown = dropdowns[i];
      if (openDropdown.classList.contains('show')) {
        openDropdown.classList.remove('show');
      }
    }
  }
}
</script>
  
    <script 
        src="https://maps.googleapis.com/maps/api/js?key=AIzaSyCSXnflSrYaVxtCYuidN3TKs1P-2iwAtDk&libraries=visualization&callback=initMap">
    </script>

</body>
</html>

