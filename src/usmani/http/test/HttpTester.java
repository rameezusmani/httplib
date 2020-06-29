package usmani.http.test;

import usmani.http.*;
import usmani.http.downloadmanager.*;

public class HttpTester {

	public static void main(String[] args){
		System.out.println("Start");
		try{
			HttpClient cl=new HttpClient(new java.net.URL("http://rameezusmani.com"));
			cl.openConnection();
			cl.setRequestMethod(HttpClient.REQUEST_METHOD_GET);
			HttpResponse response=cl.executeRequest(true);
			cl.close();
			
			HttpDownloadManager mgr=new HttpDownloadManager();
			HttpDownloadFile file=new HttpDownloadFile(new java.net.URL("http://rameezusmani.com"));
			file.setDestinationPath("D:\\rameez.html");
			file.open();
			mgr.setDownloadEventListener(new DownloadEventListener(){
				public void start(HttpDownloadFile f) {
					System.out.println("started downloading "+f.getURL().toString()+" to "+f.getDestinationPath());					
				}

				public void progress(HttpDownloadFile f, long bn) {
					System.out.println("Download progress in "+f.getURL().toString()+"::"+bn+" bytes");
				}

				public void exception(HttpDownloadFile f, Exception ex) {
					System.out.println("Exception in "+f.getURL().toString()+"::"+ex.getMessage());
				}

				public void complete(HttpDownloadFile f) {
					System.out.println("Download completed "+f.getURL().toString()+" to "+f.getDestinationPath());					
				}
				
			});
			mgr.download(file);
			
		}catch(Exception ex){
			System.out.println("Error: "+ex.getMessage());
			ex.printStackTrace();
		}
		System.out.println("End");
	}
}
