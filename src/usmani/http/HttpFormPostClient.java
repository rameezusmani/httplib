package usmani.http;

import java.net.URL;
import java.util.Hashtable;

public class HttpFormPostClient 
extends HttpClient{
	
	protected Hashtable<String,String> postFormData;
	
	public HttpFormPostClient(URL url,Hashtable<String,String> formData){
		super(url);
		this.postFormData=formData;
	}
	
	protected void setDefaultConnectionProperties()
	throws Exception{
    	super.setDefaultConnectionProperties();
    	setRequestMethod("POST");
    	connection.setDoOutput(true);
    	setRequestHeader("Content-Type",CONTENT_TYPE_FORM_URL_ENCODED);
    }
	
	protected HttpResponse executeRequestEmpty()
	throws Exception {
		return super.executeRequest(getReadError());
	}
	
	public HttpResponse executeRequest()
	throws Exception {
		return super.executeRequest(getFormDataBytes(),getReadError());
	}
	
	private byte[] getFormDataBytes(){
		String str=getFormDataString();
		return str.getBytes();
	}
	
	private String getFormDataString(){
		return getUrlUTF8EncodedData(this.postFormData);
	}
	
//  public static String postDataAndGetResponse(String url,Hashtable<String,String> formData)
//  throws Exception {
//  	String data=getUrlUTF8EncodedData(formData);
//      return postDataAndGetResponse(url,data);
//  }
	
//  public static String postDataAndGetResponse(String url,String data)
//  throws Exception {
//      return postDataAndGetResponse(url,data.getBytes());
//  }
	
//  public static String postDataAndGetResponse(String url,byte[] data)
//  throws Exception {
//
//  	HttpURLConnection sconn=getHttpConnection(url,true,true,false);
//  	sconn.setRequestMethod("POST");
//  	//sconn.setRequestProperty("Cache-Control","no-cache");
//  	//sconn.setRequestProperty("Connection","keep-alive");
//  	//sconn.setRequestProperty("Pragma","no-cache");   	
//  	sconn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
//  	//sconn.setRequestProperty("Accept","text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
//  	//sconn.setRequestProperty("User-Agent","Java/1.6.0_03");
//
//      OutputStream os=sconn.getOutputStream();
//      os.write(data);
//      InputStream is=sconn.getInputStream();
//      byte[] buff=new byte[1024];
//      String body="";
//      int bread=0;
//      while((bread=is.read(buff,0,1024))!=-1){
//      	body+=new String(buff,0,bread,"UTF-8");
//      }
//      os.close();
//      is.close();
//      sconn.disconnect();
//      return body;
//  }

}
