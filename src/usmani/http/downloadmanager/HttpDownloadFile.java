package usmani.http.downloadmanager;

import java.io.*;

import usmani.http.*;

public class HttpDownloadFile extends HttpFile {

	private FileOutputStream output;
	private String destinationPath;
	private long done;
	
	public HttpDownloadFile(java.net.URL u){
		super(u);
	}
	
	public void open() throws Exception {
		output=new FileOutputStream(destinationPath);
	}
	
	public void close() throws Exception {
		output.close();
	}
	
	public FileOutputStream getFileOutputStream(){
		return output;
	}
	
	public void setDestinationPath(String p){
		destinationPath=p;
	}
	
	public String getDestinationPath(){
		return destinationPath;
	}
	
	public void setBytesDone(long b){
		done=b;
	}
	
	public long getBytesDone(){
		return done;
	}
}
