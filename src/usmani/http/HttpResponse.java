package usmani.http;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.io.InputStream;

public class HttpResponse {
	
	private static final String DEFAULT_CHARSET="UTF-8";
	
	private HttpURLConnection connection;
	private InputStream iStream;
	private String charSet=DEFAULT_CHARSET;
	
	public HttpResponse(HttpURLConnection connection,InputStream is){
		this.connection=connection;
		this.iStream=is;
	}
	
	public InputStream getInputStream(){
		return this.iStream;
	}
	
	public long getContentLength(){
		return connection.getContentLengthLong();
	}
	
	public String getContentType(){
		return connection.getContentType();
	}
	
	public int getResponseCode(){
    	try{
    		return connection.getResponseCode();
    	}catch(Exception ex){
    		return -1;
    	}
    }
    
    public String getResponseMessage(){
    	try{
    		return connection.getResponseMessage();
    	}catch(Exception ex){
    		return null;
    	}
    }
    
    public String getResponseHeader(String header){
    	return connection.getHeaderField(header);
    }
    
    public Map<String,List<String>> getResponseHeaders(){
    	return connection.getHeaderFields();
    }
    
    public String getAsString()
    throws Exception{
    	byte[] buff=new byte[1024];
    	String body="";
    	int bread=0;
    	while((bread=iStream.read(buff,0,1024))!=-1){        	
    		body+=new String(buff,0,bread,charSet);
    	}
    	return body;
    }
}
