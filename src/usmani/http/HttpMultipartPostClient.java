package usmani.http;

import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class HttpMultipartPostClient 
extends HttpFormPostClient {
	
	private static final String DEFAULT_BOUNDARY="----------ydxdqdjimrsdamseeldsrefqnkgvhgyu";
	private List<HttpPart> files;
	private String boundary=DEFAULT_BOUNDARY;
	
	public HttpMultipartPostClient(URL url,Hashtable<String,String> formData,List<HttpPart> files){
		super(url,formData);
		this.files=files;
	}
	
	public void setBoundary(String boundary){
		this.boundary=boundary;
		setMultipartContentTypeHeader();
	}
	
	public String getBoundary(){
		return this.boundary;
	}
	
	public void addPart(HttpPart file){
		files.add(file);
	}
	
	public List<HttpPart> getFiles(){
		return this.files;
	}
	
	public HttpResponse executeRequest()
	throws Exception {
		getUnderlyingConnection().setChunkedStreamingMode(0);
		writeMultipartRequestBody();
		return super.executeRequestEmpty();
	}
	
	private void writeMultipartRequestBody()
	throws Exception {
		startOutput();
		writeOutput(getInitialBoundaryString().getBytes());
		writeOutput(getContentDisposition(postFormData).getBytes());
		for (int a=0;a<files.size();a++){
			HttpPart file=this.files.get(a);
			writeOutput(getContentDisposition(file).getBytes());
			writeOutput(file.data);
			writeOutput("\r\n".getBytes());
		}
		writeOutput(getTerminatingBoundaryString().getBytes());
		endOutput();
	}
	
	protected void setDefaultConnectionProperties()
	throws Exception {
    	super.setDefaultConnectionProperties();
    	setMultipartContentTypeHeader();
	}
	
	private void setMultipartContentTypeHeader(){
		String cTypeMultipart=CONTENT_TYPE_MULTIPART+"; boundary="+getBoundary();
    	setRequestHeader("Content-Type",cTypeMultipart);
    	cTypeMultipart=null;
	}
	
	private String getInitialBoundaryString(){
		return "--"+getBoundary()+"\r\n";
	}
	
	private String getTerminatingBoundaryString(){
		return "\r\n--"+getBoundary()+"--\r\n";
	}
	
	private String getContentDisposition(HttpPart file){
		StringBuffer res=new StringBuffer();
		res.append("Content-Disposition: form-data; name=\"").append(file.fieldName)
		.append("\"; filename=\"").append(file.fileName).append("\"\r\n") 
		.append("Content-Type: ").append(file.mimeType).append("\r\n\r\n");
		return res.toString();
	}
	
	private String getContentDisposition(String key,String value){
		StringBuffer cd=new StringBuffer();
		cd.append(getInitialBoundaryString())
			.append("Content-Disposition: form-data; name=\"")
			.append(key).append("\"").append("\r\n\r\n").append(value).append("\r\n");			
		return cd.toString();
	}
	
	private String getContentDisposition(Hashtable<String,String> params){
		StringBuffer sbuff=new StringBuffer();
		Enumeration<String> keys = params.keys(); 
		while(keys.hasMoreElements()){
			String key = (String)keys.nextElement();
			String value = (String)params.get(key);
			sbuff.append(getContentDisposition(key,value));
		}
		return sbuff.toString();
	}
	
//  public static String postMultipartDataAndGetResponse(String url,Hashtable<String,String> formData,HttpFile hf)
//  throws Exception {    
//  	List<HttpFile> fls=new Vector<HttpFile>();
//  	fls.add(hf);
//  	HttpMultipartRequest hmr=new HttpMultipartRequest(url,formData,fls);
//  	hmr.createRequest();
//  	return postMultipartDataAndGetResponse(url,hmr.getRequestBytes(),hmr.getBoundaryString());
//  }
//  
//  public static String postMultipartDataAndGetResponse(String url,byte[] data,String boundary)
//  throws Exception {
//  	HttpURLConnection sconn=getHttpConnection(url,true,true,false);
//  	sconn.setRequestMethod("POST");
//  	sconn.setRequestProperty("Cache-Control","no-cache");
//  	sconn.setRequestProperty("Connection","keep-alive");
//  	sconn.setRequestProperty("Pragma","no-cache");   	
//  	sconn.setRequestProperty("Content-Type",MULTIPART_HEADER+boundary);
//  	sconn.setRequestProperty("Accept","text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
//  	sconn.setRequestProperty("User-Agent","Java/1.6.0_03");
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
