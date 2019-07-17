package mapboxapiconnector;

public class Tester {
	
	public static void main (String[] args) {
     ForwardGeocoderMapbox geocoder = ForwardGeocoderMapbox.getInstance();
     System.out.println(geocoder.isLocationValidAndUSOrCA("Toronto"));
     System.out.println(geocoder.getCoordinates());
     System.out.println("END");
	}
}
