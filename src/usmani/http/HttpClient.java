/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package usmani.http;

import java.io.*;
import java.util.*;
import java.net.*;

/**
 *
 * @author Rameez Usmani
 */
public class HttpClient {
	
	private static String DEFAULT_USER_AGENT="Java/1.6.0_03";
	protected static int DEFAULT_READ_TIMEOUT=1000*10; //10 seconds
	
	//public static String CONTENT_TYPE_MULTIPART="multipart/form-data; boundary=";
	public static String CONTENT_TYPE_MULTIPART="multipart/form-data";
	public static String CONTENT_TYPE_FORM_URL_ENCODED="application/x-www-form-urlencoded";
	
	public static String REQUEST_METHOD_GET="GET";
	public static String REQUEST_METHOD_POST="POST";
	public static String REQUEST_METHOD_PUT="PUT";
	public static String REQUEST_METHOD_HEAD="HEAD";
	
	protected HttpURLConnection connection;
	protected URL url;
	protected InputStream iStream;
	protected OutputStream oStream;
	
	private String userAgent=DEFAULT_USER_AGENT;
	private int readTimeout=DEFAULT_READ_TIMEOUT;
	
	private boolean readError=false;
	
	//empty constructor
	//user must called setURL before any other operation
	public HttpClient(){
		
	}
	
	public HttpClient(URL url) 
	throws Exception {
		this.url=url;
		openConnection();
	}
	
	public void openConnection()
	throws Exception{
		connection=getHttpConnection(this.url);
		setDefaultConnectionProperties();
	}
	
	public void close(){
		if (iStream!=null){
			try{
				iStream.close();
			}catch(Exception ex){}
		}
		if (oStream!=null){
			try{
				oStream.close();
			}catch(Exception ex){}
		}
		if (connection!=null){
			connection.disconnect();
		}
	}
	
	public boolean getReadError(){
		return readError;
	}
	
	public void setReadError(boolean re){
		this.readError=re;
	}
	
	public URL getURL(){
		return url;
	}
	
	public void setURL(URL u)
	throws Exception {
		this.close(); //try close if already connection opened
		this.url=u;
		this.openConnection();
	}
	
	public URL getCurrentURL(){
		return connection.getURL();
	}
	
	public void setRequestMethod(String method)
	throws Exception {
		connection.setRequestMethod(method);
	}
	
	public String getRequestMethod(){
		return connection.getRequestMethod();
	}
	
	public String getContentType(){
		return getRequestHeader("Content-Type");
	}
	
	public void setContentType(String cType){
		setRequestHeader("Content-Type",cType);
	}
	
	public void setRequestHeader(String name,String value){
		connection.setRequestProperty(name,value);
	}
	
	public void addRequestHeader(String name,String value){
		connection.addRequestProperty(name,value);
	}
	
	public String getRequestHeader(String name){
		return connection.getRequestProperty(name);
	}
	
	public Map<String,List<String>> getRequestHeaders(){
		return connection.getRequestProperties();
	}
	
	public HttpURLConnection getUnderlyingConnection(){
		return connection;
	}
	
    public static String encodeUTF8Data(String name,String val){
    	return name+"="+encodeString(val);
    }

    public static String encodeString(String unEncodedString){
    	return URLUTF8Encoder.encode(unEncodedString);
    }
    
    public static String getUrlUTF8EncodedData(Hashtable<String,String> formData){
    	String data="";                
        Enumeration<String> en=formData.keys();        
        while(en.hasMoreElements()){
            String objKey=en.nextElement();
            String objVal=formData.get(objKey);            
            data+=encodeUTF8Data(objKey.toString(),objVal.toString())+"&";
        }
        return data;
    }
    
    private static HttpURLConnection getHttpConnection(URL u)
    throws Exception{    	
    	HttpURLConnection conn=(HttpURLConnection)u.openConnection();
    	//conn.connect();
    	return conn;
    }
    
    protected void setDefaultConnectionProperties()
    throws Exception {
    	connection.setDoInput(true);
    	connection.setDoOutput(false);
    	connection.setUseCaches(false);
    	connection.setReadTimeout(readTimeout);
    	addRequestHeader("User-Agent",userAgent);
    }
    
    protected void startOutput()
    throws Exception {
    	oStream=connection.getOutputStream();
    }
    
    protected void endOutput()
    throws Exception {
    	oStream.flush();
    }
    
    protected void writeOutput(byte[] data)
    throws Exception {
    	oStream.write(data);
    }
    
    protected void writeOutput(byte[] data,int off,int len)
    throws Exception {
    	oStream.write(data,off,len);
    }
    
    public HttpResponse executeRequest(byte[] data,boolean readError)
    throws Exception {
    	startOutput();
    	writeOutput(data);
    	endOutput();
    	return executeRequest(readError);
    }
    
    public HttpResponse executeRequest(boolean readError)
    throws Exception { 
    	try{
    		iStream=connection.getInputStream();
    	}catch(Exception ex){
    		if (readError){
    			iStream=connection.getErrorStream();
    		}else{
    			throw ex;
    		}
    	}
    	return new HttpResponse(getUnderlyingConnection(),iStream);
    }
//    public static byte[] getHttpResponseBytes(String url)
//    throws Exception{
//    	HttpURLConnection sconn = getHttpConnection(url,true,true,false);
//        InputStream is=sconn.getInputStream();
//        byte[] buff=new byte[1024];
//        ByteArrayOutputStream bor=new ByteArrayOutputStream();        
//        int bread=0;
//        while((bread=is.read(buff,0,buff.length))!=-1){        	
//        	bor.write(buff,0,bread);        	
//        }
//        is.close();
//        sconn.disconnect();
//        byte[] byteBuff=bor.toByteArray();
//        bor.close(); 
//        return byteBuff;
//    }
//    
//    public static String getHttpResponseBodyAsBase64(String url)
//    throws Exception {
//    	HttpURLConnection sconn = getHttpConnection(url,true,true,false);
//        InputStream is=sconn.getInputStream();
//        byte[] buff=new byte[1024];
//        String body="";
//        int bread=0;
//        while((bread=is.read(buff,0,1024))!=-1){        	
//            body+=new String(buff,0,bread);
//        }
//        is.close();
//        sconn.disconnect();
//        return body;
//    }
//

}
