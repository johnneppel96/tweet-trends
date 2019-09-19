 <?php
    include "db_connection.php";
	include "date_validator.php";
	include "data_fields.php";
    ?>

<!DOCTYPE html >
<html>
   <head>
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
    <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
    <link rel="stylesheet" type="text/css" href="style.css">
	   <title>Tweet Trends</title>
  </head>

  <body style = "background-color:black;">
  
<h1 style="color:skyblue;text-align:center;font-size:50px;">
	#TweetTrends
</h1>
  <p style="font-family:Helvetica;color:skyblue;text-align:center;">
    This site is used to track where #MeToo tweets are occurring. It displays both a heatmap and a marker map, which you can edit in the forms below.
    To look at corresponding hashtags related to #MeToo, simply hover the cursor over a marker and it will be displayed.
  </p>
  <div style="display:flex;">    
	<form action = "index.php" method = "POST" style="margin: 0 auto;text-align:center; font-family:Helvetica; color:white;">
    	Select two dates to show Tweets in between the two.&nbsp; &nbsp; <b>From: &nbsp;</b> Month 
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

<p style="font-family:Helvetica;color:skyblue;text-align:center;">
  This website was created by Monmouth University's Department of Computer Science and Software Engineering, graduating class of 2019. 
</p>

<p style="font-family:Helvetica;color:skyblue;text-align:center;padding:10px;">
  Developers: John Neppel and Thomas McHugh
</p>
       
<script>
//GLOBAL VARIABLES
var heatmapLayer; //The raster layer overlay for the heatmap
var heatMapData = [];
var markerData = [];
var map; //basemap for heatmap-layer
var markerMap;
var image = "https://maps.google.com/mapfiles/ms/icons/blue-dot.png";

//This function is automatically executed at the initial loading of the
//web page.
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
    
  displayTweetData();
    
   } //end of initMap
      
    
 
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

function displayTweetData() {
	removeMarkers(); //clears map in preparation for new marker points if necessary
    var hashtags ='';
    var latitude =0.0;
    var longitude = 0.0;
    var marker;
	var tweetText = '';
	
	//This is the info window that will pop up when a user clicks on a marker.
	//It is first declared here due issues with JavaScript and Google Maps API closures.
	//Declaring this here also allows for only one info-window to pop up at a time.
	var infowindow = new google.maps.InfoWindow();
	
<?php 
    $query = "SELECT latitude, longitude, Hashtags, TweetText FROM tweet_data WHERE
     tweet_timestamp >= '$combined_from_date' AND tweet_timestamp <= '$combined_to_date' 
      AND query_tag_id = (SELECT Query_Tag_ID FROM tweet_tags WHERE QueryTagText = '$Tweet_hashtag') ";
    $result = mysqli_query($db_connection, $query) or  die("Query execution failed " . mysqli_error($db_connection));
    while($row = mysqli_fetch_assoc($result)) {
                $latitude = $row['latitude'];
                $longitude = $row['longitude'];
                $Hashtags_list = $row['Hashtags'];
				$tweet_text =  mysqli_real_escape_string($db_connection, $row['TweetText']);
        ?>

       hashtags = "<?php echo $Hashtags_list; ?>";
       latitude = <?php echo $latitude; ?>;
       longitude = <?php echo $longitude; ?>;
	   tweetText = "<?php echo $tweet_text; ?>";
	   
        //For the heat map, pushes coords value item into dataset
     	heatMapData.push(new google.maps.LatLng(latitude, longitude)); 
 
     	 marker = new google.maps.Marker({
          position: new google.maps.LatLng(latitude, longitude),
          icon: image,
          map: markerMap,
          title: hashtags,
		  info: tweetText
        });
	
	 google.maps.event.addListener(marker, 'click', (function(marker) {
        return function() {
            infowindow.setContent(marker.info);
            infowindow.open(markerMap, marker);
        }
    })(marker));

        //FOR THE MARKER MAP
      	markerData.push(marker);
      <?php } //end of while ?>
	
    heatmapLayer.setData(heatMapData);
	<?php mysqli_close($db_connection); ?>
} //end displayTweetData()

	
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

