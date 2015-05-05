import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

 
/*
 * test for annotation by Spotlight
 * */
public class DBPediaSpotlightClient{
	public static HttpResponse executeRequest(CloseableHttpClient httpclient,String text,double confidence,int support,String type) throws ClientProtocolException, IOException{
		String API_URL="http://spotlight.dbpedia.org/";
		//String API_URL="http://spotlight.sztaki.hu:2225/";
		String Query=API_URL + "rest/annotate?" 
				+ "text=" + URLEncoder.encode(text, "utf-8")
				+ "&confidence="+confidence
				+ "&support="+support
				+ "&types="+type;
		//         HttpGet httpget = new HttpGet("http://spotlight.dbpedia.org/rest/annotate?text=President+Obama+on+Monday+will+call+for+a+new+minimum+tax+rate+for+individuals+making+more+than+%241+million+a+year+to+ensure+that+they+pay+at+least+the+same+percentage+of+their+earnings+as+other+taxpayers%2C+according+to+administration+officials.&confidence=0.0&support=0&spotter=CoOccurrenceBasedSelector&disambiguator=Default&policy=whitelist&types=&sparql=");
		//HttpGet httpget = new HttpGet("http://spotlight.dbpedia.org/rest/annotate?text=President+Obama+has+informed+that+Alsace+region&confidence=0.0&support=0&spotter=CoOccurrenceBasedSelector&disambiguator=Default&policy=whitelist&types=Person&sparql=");

		HttpGet httpget=new HttpGet(Query);
		httpget.addHeader("Accept", "application/json");

		// Execute HTTP request
		System.out.println("executing request " + httpget.getURI());
		HttpResponse response = httpclient.execute(httpget);
		return response;
	}

	public static void parserJson(HashMap<String,AnnotationItem> map,HttpResponse response) throws ParseException, IOException{

		try{  
			String content = EntityUtils.toString(response.getEntity());
			JSONObject obj = new JSONObject(content);

			if(!obj.has("Resources")) return;
			JSONArray arr = obj.getJSONArray("Resources");
			
			for (int i = 0; i < arr.length(); i++)
			{
			    String surfaceForm = arr.getJSONObject(i).getString("@surfaceForm");
			    String url=arr.getJSONObject(i).getString("@URI");
			    map.put(surfaceForm, new AnnotationItem(url));
			}

		} catch (JSONException e) {
			//throw new AnnotationException("Received invalid response from DBpedia Spotlight API.");
			System.out.println("error"+e.getMessage());
		}
	}
}