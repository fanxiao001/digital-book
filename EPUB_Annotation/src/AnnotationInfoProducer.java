import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;


public class AnnotationInfoProducer {

	static String annotationVisitTime(String context){
		Document doc = Jsoup.parse(context,"",Parser.xmlParser());
		Elements producteurs=doc.getElementsByAttributeValue("class", "PRODUCT");
		
		for(Element e:producteurs){
		    String html=e.html();  //whole html of element product
			if(e.html().contains("../Image/CONDITION_DE_VISITE.jpg")){
				//System.out.println("original:"+html);
				String newText=html;
                
				//image element before the visit time
				String time=e.getElementsByAttributeValue("src", "../Image/CONDITION_DE_VISITE.jpg").first().toString();
				
				//get ride of element image
				time=html.substring(html.indexOf(time)+time.length());
				
				//before the last </span>
				if(time.contains("<img")) time=time.substring(0,time.indexOf("<img"));
				if(time.contains("</span>")) time=time.substring(0,time.lastIndexOf("</span>"));
				String time_before=time;
				//Jeu. ven. 17h-19h; sam. 10h-12h30; f. 1<span class="H">er</span> -15 août
				while(time.contains("<")&&time.contains(">")){
					System.out.println("before:"+time);
					int i1=time.indexOf("<");
					int i2=time.indexOf(">");
					int length=time.length();
					time=time.substring(0,i1)+time.substring(i2+1,length);
					System.out.println("after:"+time);
				}
				String span="<time horaire=\""+time+"\"><u>"+time+"</u></time>";						
				newText=newText.replaceAll(time_before, span);						
				e=e.html(newText);			
			}
		}
		
		return doc.outerHtml();
	}
	
	static String annotationContactInfo(String context){
		Document doc = Jsoup.parse(context,"",Parser.xmlParser());
		//get element with attribute class=PRODUCT
		Elements producteurs=doc.getElementsByAttributeValue("class", "PRODUCT"); 
		for(Element e:producteurs){
			    String html=e.html();
			if(e.html().contains("../Image/LOGO_PRODUCTEUR.jpg")){
				String newText=html;
				String producteur=e.getElementsByAttributeValue("src", "../Image/LOGO_PRODUCTEUR.jpg").first().toString();

				if(e.getElementsByTag("span").size()>0)
					producteur=html.substring(html.indexOf(producteur)+producteur.length(),html.indexOf("<span>"));
				else producteur=html.substring(html.indexOf(producteur)+producteur.length());
				
				String address="";						
				String name=producteur.substring(0,producteur.indexOf(","));
				if(producteur.contains("tél")){
					address=producteur.substring(producteur.indexOf(",")+1,producteur.indexOf(", tél"));
				}
				else{
					address=producteur.substring(producteur.indexOf(",")+1,producteur.lastIndexOf(","));
				}
				
				String rest="";
				if(producteur.contains("tel")){
					rest=producteur.substring(producteur.indexOf(", tél"),producteur.length());
				}
				else{
					rest=producteur.substring(producteur.indexOf(address)+address.length(),producteur.length());
				}
				String[] parts=rest.split(",");
				String tel="";
				String fax="";
				String mail="";
				
				for(int i=0;i<parts.length;i++){
					if(parts[i].contains("tél")&&parts[i].contains("fax")) tel=parts[i].trim();
					if(parts[i].contains("tél")&&(!parts[i].contains("fax"))) tel=parts[i].trim();
					if(parts[i].contains("fax")&&(!parts[i].contains("tél"))) fax=parts[i].trim();
					if(parts[i].contains("@")) mail=parts[i].trim();
				}
				
				
				String span="<span typeof=\"vcard:VCard\">"+
						"<span property=\"vcard:fn\">"+name+", </span>"+
						"<u><span rel=\"vcard:adr\"><span typeof=\"vcard:Address vcard:Work\"><span property=\"rdf:value\">"+address.trim()+"</span></span></span></u>";
				if(!tel.isEmpty()){
					span+=", <span rel=\"vcard:tel\"><span typeof=\"vcard:Tel vcard:Work\">"+
							"<span property=\"rdf:value\" />"+
							tel+"</span></span>";
				}
				if(!fax.isEmpty()){
					span+=", <span property=\"vcard:fax\">"+fax+"</span> ";
				}
				if(!mail.isEmpty()){
					span+=", <a rel=\"vcard:Email\" href=\"mailto:"+mail+"\">"+mail+"</a>";
				}
				span+="</span>";
				newText=newText.replace(producteur, span);
				e=e.html(newText);					
				
			}
		}
		String file=doc.outerHtml();
		return file;
	}
	
}
