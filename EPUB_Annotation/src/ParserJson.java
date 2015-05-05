
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ParserJson {

	public static String getFileContent(String path){
		BufferedReader bufferreader = null;
		String wordList="";
		try{
			
			String CurrentLine;			
			//get words
			bufferreader = new BufferedReader(new FileReader(path));

			while ((CurrentLine = bufferreader.readLine()) != null) {
				wordList=wordList+CurrentLine+'\n';
			}
			
		}catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bufferreader != null)bufferreader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return wordList;
	}
	public static ArrayList<String> getStopwords(String content){
		

		ArrayList<String> words= new ArrayList<String>();
		try{  
			JSONObject obj = new JSONObject(content);

			if(!obj.has("stopwords")) return null;
			JSONArray arr = obj.getJSONArray("stopwords");
			
			for (int i = 0; i < arr.length(); i++)
			{
			    String word = arr.getJSONObject(i).getString("word");
			    words.add(word);
			}

			return words;

		} catch (JSONException e) {
			//throw new AnnotationException("Received invalid response from DBpedia Spotlight API.");
			System.out.println("error"+e.getMessage());
		}
		return words;
		
	}
	
	public static HashMap<String,String> getWhitewords(String content){
		

		HashMap<String,String> words= new HashMap<String,String>();
		try{  
			JSONObject obj = new JSONObject(content);

			if(!obj.has("whitewords")) return null;
			JSONArray arr = obj.getJSONArray("whitewords");
			
			for (int i = 0; i < arr.length(); i++)
			{
			    String word = arr.getJSONObject(i).getString("word");
			    String url=arr.getJSONObject(i).getString("url");
			    words.put(word, url);
			}

			return words;

		} catch (JSONException e) {
			//throw new AnnotationException("Received invalid response from DBpedia Spotlight API.");
			System.out.println("error"+e.getMessage());
		}
		return words;
		
	}
}
