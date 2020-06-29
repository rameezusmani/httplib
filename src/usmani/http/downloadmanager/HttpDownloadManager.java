package usmani.http.downloadmanager;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import usmani.http.*;

public class HttpDownloadManager {
	
	//event listener
	private DownloadEventListener eventListener=null;
	//pool of downloaded chunks
	private ChunksPool pool;
	//current offset
	private long start=0;
	//pool of threads
	private DownloadThreadPool threadPool;
	//number of threads
	private int numThreads=0;
	//writing thread
	private HttpDownloadFileSaver fileSaver;
	//downloading file
	private HttpDownloadFile downFile;
	//concurrent executor
	private ExecutorService thExecutor;
	
	/*
	 * No argument constructor
	 */	
	public HttpDownloadManager(){		
		//instantiate pool of downloaded data chunk
		pool=new ChunksPool();
		//instantiate pool of downloading threads
		threadPool=new DownloadThreadPool(3);
	}
	
	/*
	 * Returns the number of threads required to download
	 * a file
	 * 
	 * Parameters:
	 * 
	 * size[in]: size in bytes of the file
	 */
	private int getNumberOfThreads(long size){
		//get original size
		long sizeMB=size-start;
		//calculate size in MBs from bytes
		sizeMB=(size/1024)/1024;
		//if file is greater than 3 MBs
		if (sizeMB>=3){
			//number of threads are 3
			return 3;
		}
		//if less than greater than or equal to 2MBs
		else if (sizeMB>=2){
			//number of threads are 2
			return 2;
		}else{
			//number of threads are 1
			return 1;
		}		
	}
	
	/*
	 * Set parameters of file to be downloaded
	 * it executes HEAD request and get parameters 
	 * e.g. filesize etc
	 */	
	private void setParams() throws Exception {
		HttpClient request=new HttpClient(downFile.getURL());
		//set request type to head
		request.setRequestMethod(HttpClient.REQUEST_METHOD_HEAD);
		//insert host header
		request.addRequestHeader("Host",downFile.getURL().getHost());
		//get response object
		HttpResponse response=request.executeRequest(false);
		
		//if response code is not OK (200)
		if (response.getResponseCode()!=200){
			request=null; //free request memory
			response=null;//free response memory
			//throw exception
			throw new Exception("Invalid page");
		}
		//if response code is OK (200)
		else{
			//set file size from contentLength
			downFile.setFileSize(response.getContentLength());
			System.out.println("Filesize="+downFile.getFileSize());
			//get Accept-Ranges header to check if resume
			//is supported
			String range=response.getResponseHeader("Accept-Ranges");
			//if Accept-Ranges header is present
			if(range!=null){
				//if bytes are accepted by the server
				if (range.compareTo("bytes")==0){
					//set resume supported to true
					downFile.setIsResumeSupported(true);
				}
			}
		}	
		request=null; //free request memory
		response=null;//free response memory
	}
	
	/*
	 * Download the file from web
	 * 
	 * Params:
	 * 
	 * u[in]: URL of the file to be downloaded
	 * f[in]: File stream of the destination file
	 */
	public void download(HttpDownloadFile file) throws Exception{
		try{
			//file must be opened before sending to download here
			downFile=file;
			start=file.getBytesDone();
			
			//set the parameters of the file
			setParams();
			
			//create chunk writing thread
			fileSaver=new HttpDownloadFileSaver(pool,downFile);
			//set priority to NORMAL
			fileSaver.setPriority(Thread.NORM_PRIORITY);
			//register an event listener
			fileSaver.setEventListener(eventListener);
			//set starting offset of file
			fileSaver.setStartingOffset(start);
			//start the writing thread
			fileSaver.start();
			
			long bStart=start;
			int threadCount=1; //default thread size is 1
			long bCount=downFile.getFileSize(); //size of file to be downloaded
			
			//if eventlistener is registered
			if (eventListener!=null){
				//raise start event
				eventListener.start(downFile);
				//publish progress event
				eventListener.progress(downFile,bStart);
			}
			//if resume is supported
			if (downFile.isResumeSupported()){
				threadCount=getNumberOfThreads(downFile.getFileSize());
				//calculate number of bytes for each thread
				//default was size of file but now as there will be multiple threads
				//so each thread will get slice of data (number of bytes)
				bCount=(long)downFile.getFileSize()/threadCount;
			}
			//create java thread pool to execute one or multiple threads for downloading
			thExecutor=Executors.newFixedThreadPool(threadCount);
			//iterate till thread count
			for (int a=0;a<threadCount;a++){			
				//get a thread from the pool
				Downloader d=threadPool.getDownloadThread();
				if (d==null){				
					if (eventListener!=null){
						//raise an exception
						eventListener.exception(null,new Exception("Not enough threads"));
					}
					continue;
				}
				d.setHttpDownloader(this);
				d.setURL(file.getURL());
				//set starting byte to bStart
				d.setStart(bStart);
				//set id
				d.setID(a+1);
				//if not last thread
				if (a!=(threadCount-1)){
					//set ending value to count+start-1
					d.setEnd(bStart+bCount-1);
					//add count to start
					bStart+=bCount;
				}else{
					//set end to -1 which means till the end of file
					d.setEnd(-1);
				}
				//increment the threads running
				numThreads++;
				//start downloading
				thExecutor.execute(d);
			}
		}catch (Exception ex){
			throw ex;
		}
	}
	/*
	 * Set the event listener for listening different
	 * events
	 * 
	 * Parameters:
	 * 
	 * del[in]: event listener to be registered
	 */
	public void setDownloadEventListener(DownloadEventListener del){
		eventListener=del;
	}
	
	/*
	 * It process the completion of thread
	 * 
	 * Params:
	 * 
	 * d[in]: completed thread
	 */
	public void complete(Downloader d) throws Exception {
		//free the thread in the pool
		threadPool.freeThread(d);
		//decrement the number of threads running
		numThreads--;
		//if all threads finished
		if (numThreads==0){
			//wait until chunk pool becomes empty
			while(!pool.isEmpty()){
				Thread.yield();
			}
			//stop the writing thread
			fileSaver.setCanRun(false);
			//wait for writing thread
			fileSaver.join();
			//close the file stream
			downFile.close();
			//if eventlistener is registered
			if (eventListener!=null){	
				//raise completion event
				eventListener.complete(downFile);
			}
		}	
	}	
	
	/*
	 * 
	 * Handles the exception occured while downloading file
	 *  
	 *  Parameters:
	 *  
	 *  d[in]: Downloader in which exception is occured  
	 */
	public void exceptionOccured(Downloader d){
		//stop the downloading thread
		d.setCanRun(false);
	}
	
	
	/*
	 * save the bytes downloaded to the files
	 * 
	 * Params:
	 * 
	 * chunk[in]: bytes downloaded 
	 */	
	public void saveBytes(DownloadedChunk chunk) throws Exception {
		//save the chunks pool from being edit
		synchronized(pool){			
			//add the chunk to the pool
			pool.add(chunk);
		}
		//if eventlistener is registered
		if (eventListener!=null){
			//raise the progress event
			eventListener.progress(downFile,chunk.getLength());
		}
	}
}
