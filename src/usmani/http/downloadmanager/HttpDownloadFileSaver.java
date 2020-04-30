package usmani.http.downloadmanager;

import java.io.FileOutputStream;

public class HttpDownloadFileSaver extends Thread {
	
	//variable to check if the thread can run
	private boolean canRun=true;
	//outputstream of destination file
	private FileOutputStream fos;
	//starting offset
	private long start=0;
	//class that will listen for events
	private DownloadEventListener eventListener=null;
	//pool of downloaded chunks
	private ChunksPool pool;
	//file to be downloaded
	private HttpDownloadFile hFile;
	
	public HttpDownloadFileSaver(ChunksPool cp,HttpDownloadFile hf) throws Exception{
		pool=cp;
		hFile=hf;
		fos=hf.getFileOutputStream();
	}
	
	public void setFileStream(FileOutputStream f){
		fos=f;
	}
	
	public void setStartingOffset(long s){
		start=s;
	}
	
	public void setEventListener(DownloadEventListener l){
		eventListener=l;
	}
	
	public void setCanRun(boolean b){
		canRun=b;
	}
	
	private void startWriting() throws Exception{
		DownloadedChunk dc;
		
		while(canRun){
			synchronized(pool){	
				dc=pool.getChunk(start);
			}			
			if (dc!=null){
				fos.write(dc.getChunkData(),0,dc.getLength());					
				start+=dc.getLength();
			}
			yield();
		}
	}
	
	public void run(){
		try{
			startWriting();
		}catch (Exception ex){
			eventListener.exception(hFile,ex);
		}
	}
}
