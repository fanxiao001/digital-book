import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class UnzipEpub {

	//unzip epub file and return the list of files in directory /EPUB/Content as target files
	public static ArrayList<String> upZip(String zipFilePath) throws IOException{
		
		ZipFile zipFile = new ZipFile(zipFilePath);		
		
		String path = zipFilePath.substring(0,zipFilePath.lastIndexOf("/")+1); //unzip files to the same directory of input file
		
		String pathUnzipFile=path+zipFilePath.substring(zipFilePath.lastIndexOf("/")+1, zipFilePath.lastIndexOf(".epub"));
		
		//content files, files to be treated
		ArrayList<String> targetFiles=new ArrayList<String>();
		
		//create the main directory
		File f1= new File(pathUnzipFile);
		if(f1.exists()) //FileUtils.cleanDirectory(f1);
			f1.delete();
		f1.mkdir();
		new File(pathUnzipFile+File.separator+"META-INF").mkdir();
		new File(pathUnzipFile+File.separator+"EPUB").mkdir();
		new File(pathUnzipFile+File.separator+"EPUB"+File.separator+"Content").mkdir();
		new File(pathUnzipFile+File.separator+"EPUB"+File.separator+"Image").mkdir();
		new File(pathUnzipFile+File.separator+"EPUB"+File.separator+"Navigation").mkdir();
		new File(pathUnzipFile+File.separator+"EPUB"+File.separator+"Style").mkdir();
		new File(pathUnzipFile+File.separator+"EPUB"+File.separator+"Script").mkdir();

		Enumeration files = zipFile.entries();

		while (files.hasMoreElements()) {
		
			//copy folders and files
			ZipEntry entry = (ZipEntry) files.nextElement();
			if(entry.getName().startsWith("EPUB/Content/")) targetFiles.add(pathUnzipFile+File.separator+entry.getName());
			
			if (entry.isDirectory()) {
				File file = new File(pathUnzipFile + File.separator + entry.getName());
				file.mkdir();

			} else {
				File f = new File(pathUnzipFile+ File.separator + entry.getName());
				FileOutputStream fos = new FileOutputStream(f);
				InputStream is = zipFile.getInputStream(entry);
				byte[] buffer = new byte[1024];
				int bytesRead = 0;
				while ((bytesRead = is.read(buffer)) != -1) {
					fos.write(buffer, 0, bytesRead);
				}
				fos.close();
			}
			
		}
		return targetFiles;
		
	}
}
