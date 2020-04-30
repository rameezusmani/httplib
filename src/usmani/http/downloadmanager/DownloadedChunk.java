package usmani.http.downloadmanager;

public class DownloadedChunk {

	private byte[] buffer;
	private int length;
	private long offset;
	private Downloader down;
	
	public DownloadedChunk(){
		buffer=null;
		length=0;
		offset=0;
	}
	
	public void setValues(byte[] b,int l,long of){
		buffer=b;
		length=l;
		offset=of;
	}
	
	public void setDownloader(Downloader d){
		down=d;
	}
	
	public Downloader getDownloader(){
		return down;
	}
	
	public byte[] getChunkData(){
		return buffer;
	}
	
	public int getLength(){
		return length;
	}
	
	public long getOffset(){
		return offset;
	}
}
