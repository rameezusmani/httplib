package usmani.http.downloadmanager;

import java.net.URL;
import java.io.*;

import usmani.http.*;

public class Downloader extends HttpClient implements Runnable{
	
	//max buffer size
	private static final int MAX_BUFFER_SIZE=1024*20;
	
	//input stream for downloading data
	private InputStream responseStream;
	//reference to HttpDownloader for raising events
	private HttpDownloadManager hDownloader;
	//start and ending bytes to retreive and total bytes downloaded by this downloader
	private long start=0,end=-1,bytesDownloaded=0;
	//variable to check whether the thread can run
	private boolean canRun=true;
	//id of this downloader
	private int id=0;
	
	public Downloader(URL u,HttpDownloadManager d){
		this.setURL(u);
		this.setHttpDownloader(d);
	}
	
	public Downloader(){}
	
	public void setHttpDownloader(HttpDownloadManager d){
		this.hDownloader=d;
	}
	
	/*
	 * This methods perform the downloading of data
	 */
	private void download() throws Exception{
		
		String range="bytes=";
		//set range for resume supported files
		range+=String.valueOf(start)+"-";
		//if end is greater than 0
		if (end>0){
			//add end to range
			range+=end;
		}
		//add Host header
		addRequestHeader("Host",getURL().getHost());
		//add Range header
		addRequestHeader("Range",range);
		//open connection
		this.openConnection();
		//read response from the client
		HttpResponse response=this.executeRequest(false);
		//get input stream of the response
		responseStream=response.getInputStream();
		
		byte[] buffer=null;
		//number of bytes read
		int read;
		//offset for reading
		int offset=0;
		//count of total bytes in buffer
		int bytesCount=0;

		while(canRun){
			if (bytesCount==0){
				//buffer to store retrieved data
				buffer=new byte[MAX_BUFFER_SIZE];
			}
			//read from the stream into buffer
			read=responseStream.read(buffer,offset,MAX_BUFFER_SIZE-bytesCount);
			
			//if server disconnected
			if (read==-1){	
				//if data is in buffer
				if (bytesCount>0){
					createDownloadedChunk(buffer,bytesCount);
					//set bytesCount to 0
					bytesCount=0;
				}
				
				//release buffer memory
				buffer=null;
				//close the connection
				this.close();
				//raise completion event
				hDownloader.complete(this);
				//return
				return;
			}
			//if server is still connected
			else{
				//add number of bytes read to count
				bytesCount+=read;
				//if buffer is completely filled
				if (bytesCount==MAX_BUFFER_SIZE){
					createDownloadedChunk(buffer,bytesCount);
					//add number of bytes read to start
					start+=bytesCount;
					//set offset to zero
					offset=0;
					//set number of bytes in buffer to 0
					bytesCount=0;
				}
				//if buffer is not completely filled
				else{					
					//add number of bytes read to offset
					offset+=read;
				}
			}
		}
	}
	
	private DownloadedChunk createDownloadedChunk(byte[] buffer,int bytesCount)
	throws Exception {
		//reference to the chunk downloaded
		DownloadedChunk chunk=new DownloadedChunk();
		//initialize values in chunk
		chunk.setValues(buffer,bytesCount,start);
		//raise save event in downloader
		chunk.setDownloader(this);
		//save bytes
		hDownloader.saveBytes(chunk);
		//increment the number of bytes downloaded
		bytesDownloaded+=bytesCount;
		//return created chunk
		return chunk;
	}
	
	/*
	 * returns the id of the thread
	 */
	public int getID(){
		return id;
	}
	
	/*
	 * sets the id of the thread
	 * 
	 * Parameters:
	 * 
	 * i[in]: id to be set
	 */
	public void setID(int i){
		id=i;
	}
	
	public void setCanRun(boolean b){
		canRun=b;
	}
	
	/*
	 * Set the starting offset from which to download
	 * 
	 * Parameters:
	 * 
	 * s[in]: starting offset
	 */	
	public void setStart(long s){
		start=s;
	}
	
	/*
	 * This method sets the ending offset 
	 * 
	 * Parameters:
	 * 
	 * e[in]: ending offset
	 */	
	public void setEnd(long e){
		end=e;
	}
	
	public long getStart(){
		return start;	
	}
	
	public long getEnd(){
		return end;
	}
	
	public long getBytesDownloaded(){
		return bytesDownloaded;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */	
	public void run(){
		try{
			download();
		}catch (Exception ex){
			System.out.println("Exception in run "+ex.getMessage()+" "+id);
			if (hDownloader!=null){
				hDownloader.exceptionOccured(this);
			}	
		}
	}
}
