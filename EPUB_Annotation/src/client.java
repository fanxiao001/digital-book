import java.io.File;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;


public class client {
	public final static void main(String[] args) throws Exception {
		
		//three parameters needed : path of epub file, path of folder that contains supplementary files
		if(args.length<2) return;

		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		String pathEpub = args[0];
		String pathBlackList=args[1]+File.separator+"blackList.txt";
		String pathWhiteList=args[1]+File.separator+"whiteList.txt";

		//unzip ePub file
		ArrayList<String> targetFiles=new ArrayList<String>();
		targetFiles=UnzipEpub.upZip(pathEpub); //list of target files
		
		//copy js and css files from folder args[1]
		String pathIndexJS=args[1]+File.separator+"index.js";
		System.out.println(pathIndexJS);
		String pathColorboxJS=args[1]+File.separator+"jquery.colorbox.js";
		String pathColorboxCSS=args[1]+File.separator+"colorbox.css";
		String pathColorboxImg=args[1]+File.separator+"images";
		
		String dst = args[0].substring(0,args[0].lastIndexOf(".epub"));
		
		FileUtils.copyFileToDirectory(new File(pathIndexJS), new File(dst+File.separator+"EPUB"+File.separator+"Script"));
		System.out.print(dst+File.separator+"EPUB"+File.separator+"Script");
		FileUtils.copyFileToDirectory(new File(pathColorboxJS), new File(dst+File.separator+"EPUB"+File.separator+"Script"));
		FileUtils.copyFileToDirectory(new File(pathColorboxCSS),new File(dst+File.separator+"EPUB"+File.separator+"Style"));
		FileUtils.copyDirectory(new File(pathColorboxImg), new File(dst+File.separator+"EPUB"+File.separator+"Style"+File.separator+"images"));

		ArrayList<String> stopwords= new ArrayList<String>();
		HashMap<String,String> whitewords= new HashMap<String,String>();

		//get stopwords and whitewords
		stopwords=ParserJson.getStopwords(ParserJson.getFileContent(pathBlackList));
		whitewords=ParserJson.getWhitewords(ParserJson.getFileContent(pathWhiteList));

		try {

			//iterate target files
			for(int numFile=0;numFile<targetFiles.size();numFile++){

				System.out.println("----------------------------------------");
				System.out.println("En cours de traiter :" + targetFiles.get(numFile));

				String file=ParserJson.getFileContent(targetFiles.get(numFile)); //get file content
				Document doc = Jsoup.parse(file,"",Parser.xmlParser()); //get DOM of file
				doc.outputSettings().escapeMode(EscapeMode.xhtml); //set output mode to xhtml

				//text in element PARREGSE and TEXVIN need to be treated
				Elements link = doc.getElementsByAttributeValue("class","PARREGSE"); 
				link.addAll(doc.getElementsByAttributeValue("class","TEXVIN"));
				
				
				//iterate elements
				for(Element e:link){
					String text="";
					String newText="";
					HashMap<String,AnnotationItem> map=new HashMap<String,AnnotationItem>();
					text=e.html();
					newText=e.html();

					HttpResponse response;
					response=DBPediaSpotlightClient.executeRequest(httpclient, text, 0.2, 25, "Place");

					if ( response!=null && response.getStatusLine().getStatusCode() == 200) {
						DBPediaSpotlightClient.parserJson(map,response); //parser response
					}
					else{
						System.out.println("Erreur HttpResponse : " + response.getStatusLine());
					}

					//remove stopwords from list of annotations
					for(String stopword:stopwords){
						map.remove(stopword);
					}					

					//add white words to list of annotations
					for(String key:whitewords.keySet()){
						map.put(key, new AnnotationItem(whitewords.get(key)));
					}

					System.out.println("num of annotation"+map.size());


					for(String key: map.keySet()){
						ArrayList<String> boardingbox=new ArrayList<String>();
						HashMap<String,String> p=new HashMap<String,String>();

						//get boardingbox, longitude and latitude using OSM Nomination Service
						if(NominationOSM.executeRequest(httpclient, key , boardingbox,p)){
							String b="";
							for(int k=0;k<boardingbox.size();k++){
								if(k!=boardingbox.size()-1){
									b+=boardingbox.get(k)+",";
								}
								else b+=boardingbox.get(k);
							}
							map.get(key).setBoardingbox(b);
							map.get(key).setLongitude(Double.valueOf(p.get("long").toString()));
							map.get(key).setLatitude(Double.valueOf(p.get("lat").toString()));
						}

					}

					//mark the place with element tag "place" and attributes "about","long","lat","boarding"
					for ( String key : map.keySet() ) {
						key=key.trim();
						String span="<place about='"+map.get(key).getURL()+"' long='"+map.get(key).getLongitude()+"' lat='"
								+map.get(key).getLatitude()+"' boarding='"+map.get(key).getBoardingbox()+"'><u>"+key+"</u></place>";
						newText=newText.replace(key,span);
						newText.replace("\n", "");
					}
					e=e.html(newText);
				}
				file=doc.outerHtml();
				
				//annotate contact information of wine producer
				file=AnnotationInfoProducer.annotationContactInfo(file);

				//annotate visit time of wine producer
				file=AnnotationInfoProducer.annotationVisitTime(file);

				PrintWriter writer = new PrintWriter(targetFiles.get(numFile));
				doc = Jsoup.parse(file,"",Parser.xmlParser());
				doc.outputSettings().escapeMode(EscapeMode.xhtml);

				//add js and css files used
				Element head=doc.getElementsByTag("head").first();
				head.appendElement("link").attr("rel","stylesheet").attr("href","../Style/colorbox.css");
				head.appendElement("link").attr("rel","stylesheet").attr("href","http://code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css");
				head.appendElement("script").attr("language", "javascript")
				.attr("src", "http://code.jquery.com/jquery-latest.js");
				head.appendElement("script").attr("language", "javascript").attr("src", "http://maps.google.com/maps/api/js?sensor=false");
				head.appendElement("script").attr("src", "../Script/index.js");
				head.appendElement("script").attr("src", "http://code.jquery.com/ui/1.11.4/jquery-ui.js");
				head.appendElement("script").attr("src", "../Script/jquery.colorbox.js");
				//Element body=doc.getElementsByTag("body").first();
				//body.prependElement("div").attr("id", "googleMap").appendElement("a").attr("class", "b-close").text("x");
                Element body=doc.body();
                Element div=body.parent().appendElement("div").attr("id","dialog-form").attr("title","Trouver un producteur");
                div.appendElement("p").attr("class","validateTips").text("All form fields are required.");
                Element fieldset=div.appendElement("form").appendElement("fieldset");
                fieldset.appendElement("label").attr("for","date").text("Date");
                fieldset.appendElement("input").attr("id","getDate").attr("type","date").attr("name","date").attr("value","");
                fieldset.appendElement("label").attr("for","time").text("Time");
                fieldset.appendElement("input").attr("id","getTime").attr("type","time").attr("name","time");
                fieldset.appendElement("input").attr("type","submit").attr("tabindex","-1").attr("style","position:absolute; top:-1000px");
				
                Element html=doc.getElementsByTag("html").first();
                html.attr("xmlns:vcard","http://www.w3.org/2006/vcard/ns#"); 
                html.attr("xmlns:rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
                
                file=doc.outerHtml();
				writer.print(file);
				writer.close();
									

			}


		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources

			httpclient.close();   	
		}

	}

}
