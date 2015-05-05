
public class AnnotationItem {

	private String URL;
	private String boardingbox=""; //four values of each edge linked by "," 
	private double latitude;
	private double longitude;
	
	public AnnotationItem() {
		URL="";
		// TODO Auto-generated constructor stub
	}
	public AnnotationItem(String uRL, double latitude, double longitude) {
		super();
		URL = uRL;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	public AnnotationItem(String uRL) {
		super();
		URL = uRL;
	}

	public String getBoardingbox() {
		return boardingbox;
	}
	public void setBoardingbox(String boardingbox) {
		this.boardingbox = boardingbox;
	}


	public String getURL() {
		return URL;
	}
	public void setURL(String uRL) {
		URL = uRL;
	}

	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public void print(){
		System.out.println("URL: "+ this.URL + ", Longitude: "+this.longitude+", Latitude: "+this.latitude);

	}

}
