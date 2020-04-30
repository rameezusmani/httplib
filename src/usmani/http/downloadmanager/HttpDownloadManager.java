package usmani.http.downloadmanager;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import usmani.http.*;

public class HttpDownloadManager {
	
	//event listener
	private DownloadEventListener eventListener=null;
	//destination file stream
	private FileOutputStream fos;
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
		HttpClient request=new HttpClient();
		//set request type to head
		request.setRequestMethod(HttpClient.REQUEST_METHOD_HEAD);
		//set document path
		request.setURL(downFile.getURL());
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
			downFile=file;
			fos=file.getFileOutputStream();
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
			//set output file stream
			fileSaver.setFileStream(fos);
			//start the writing thread
			fileSaver.start();
			
			long bStart=start;
			int threadCount=0;
			long bCount=downFile.getFileSize();
			
			//if resume is supported
			if (downFile.isResumeSupported()){
				//if eventlistener is registered
				if (eventListener!=null){
					//raise start event
					eventListener.start(downFile);
				}
				//if event listener is registered
				if (eventListener!=null){
					//raise progress event
					eventListener.progress(downFile,bStart);
				}
				//get number of threads for the filesize
				threadCount=getNumberOfThreads(downFile.getFileSize());
				thExecutor=Executors.newFixedThreadPool(threadCount);
				//calculate number of bytes for each thread
				bCount=downFile.getFileSize()/threadCount;
				//iterate till count
				for (int a=0;a<threadCount;a++){			
					//get a thread from the pool
					Downloader d=threadPool.getDownloadThread();
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
					}
					else{
						d.setEnd(-1);
					}
					//increment the threads running
					numThreads++;
					//start downloading
					thExecutor.execute(d);
				}
			}else{				
				//get downloading thread from the pool
				Downloader d=threadPool.getDownloadThread();
				//if no thread
				if (d==null){				
					//if eventlistener is registered
					if (eventListener!=null){
						//raise an exception
						eventListener.exception(null,new Exception("Not enough threads"));
					}
				}else{
					//set starting offset
					d.setStart(start);
					//set id to 1 (default)
					d.setID(1);
					//increment the number of threads
					numThreads++;
					//if event listener is registered
					if (eventListener!=null){
						//raise start event
						eventListener.start(downFile);
					}
					//run downloader
					thExecutor.execute(d);
				}
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
	public void setEventListener(DownloadEventListener del){
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
			//close the file stream
			fos.close();
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
