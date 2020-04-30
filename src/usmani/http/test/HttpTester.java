package usmani.http.test;

import usmani.http.*;
import usmani.http.downloadmanager.*;

public class HttpTester {

	public static void main(String[] args){
		System.out.println("Start");
		try{
			HttpClient cl=new HttpClient(new java.net.URL("http://rameezusmani.com"));
			cl.openConnection();
			cl.setRequestMethod(HttpClient.REQUEST_METHOD_HEAD);
			HttpResponse response=cl.executeRequest(true);
			cl.close();
			
			HttpDownloadManager mgr=new HttpDownloadManager();
			
		}catch(Exception ex){
			System.out.println("Error: "+ex.getMessage());
			ex.printStackTrace();
		}
		System.out.println("End");
	}
}
