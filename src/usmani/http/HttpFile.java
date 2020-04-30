package usmani.http;

import java.net.URL;

public class HttpFile {	
	
	protected URL fileURL;
	protected long fileSize=0;
	protected boolean resumeSupported=false;
	
	public HttpFile(URL u){
		fileURL=u;
	}
	
	public boolean isResumeSupported(){
		return resumeSupported;
	}
	
	public long getFileSize(){
		return fileSize;
	}
	
	public String toString(){
		return fileURL.toString();
	}
	
	public URL getURL(){
		return fileURL;
	}
	
	public void setFileSize(long fsize){
		fileSize=fsize;
	}
	
	public void setIsResumeSupported(boolean b){
		resumeSupported=b;
	}
}