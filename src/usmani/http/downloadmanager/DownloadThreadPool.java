package usmani.http.downloadmanager;

public class DownloadThreadPool {

	private java.util.Vector<Downloader> downThreads;
	private int[] usageCounter;
	private int poolSize;	
	
	public DownloadThreadPool(int size){
		
		downThreads=new java.util.Vector<Downloader>();
		usageCounter=new int[size];
		poolSize=size;
		
		for (int a=0;a<size;a++){
			Downloader d=new Downloader();
			downThreads.add(d);
			usageCounter[a]=0;
		}
	}
	
	public int getPoolSize(){
		return poolSize;
	}
	
	public void freeThread(Downloader d){
		int i=downThreads.indexOf(d);
		downThreads.remove(i);
		usageCounter[i]=0;
	}
	
	public Downloader getDownloadThread(){
		
		for (int a=0;a<poolSize;a++){
			if (usageCounter[a]==0){
				usageCounter[a]=1;
				return downThreads.get(a);
			}
		}		
		return null;
	}
	
}
