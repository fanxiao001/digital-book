import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;


public class NominationOSM {
	public static boolean executeRequest(CloseableHttpClient httpclient,String place,ArrayList<String> boarding,HashMap<String,String> p) throws ClientProtocolException, IOException{
		String API_URL="http://nominatim.openstreetmap.org/search?q=";
		
		String address="";
		//place=place.replaceAll(",", ",+");
		place=place.replaceAll("\\s", "+");
		//place=place.replaceAll("++", "+");
		address=URLEncoder.encode(place, "utf-8");

		//15,+av.+des+Vosges,+67140+Barr&format=json&polygon=1&addressdetails=1
		//String API_URL="http://spotlight.sztaki.hu:2225/";
		String Query=API_URL + address+"&format=json&polygon=1&addressdetails=1";
		
		HttpGet httpget=new HttpGet(Query);
		// Execute HTTP request
		System.out.println("executing request " + httpget.getURI());
		String content="";
		try{
		HttpResponse response = httpclient.execute(httpget);
		content = EntityUtils.toString(response.getEntity());
		}
		catch(Exception e){
			System.out.println("Exception : "+e.toString());
		}
		
		try {
			JSONArray arr=new JSONArray(content);
			if(arr.length()==0) return false;
			else{
				boarding.add(arr.getJSONObject(0).getJSONArray("boundingbox").getString(0));
				boarding.add(arr.getJSONObject(0).getJSONArray("boundingbox").getString(1));
				boarding.add(arr.getJSONObject(0).getJSONArray("boundingbox").getString(2));
				boarding.add(arr.getJSONObject(0).getJSONArray("boundingbox").getString(3));
				p.put("long",arr.getJSONObject(0).getString("lon"));
				p.put("lat",arr.getJSONObject(0).getString("lat"));				
				return true;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
		
	}
}
