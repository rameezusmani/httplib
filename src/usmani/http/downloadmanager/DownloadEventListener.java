package usmani.http.downloadmanager;

/*
 * 
 * Interface for listening different downloading events
 * Class that wants to listen for events must implement this
 * interface
 */
public interface DownloadEventListener {
	
	//this method is called when the files parameters are set
	void start(HttpDownloadFile f);
	//this method is called when some bytes are downloaded
	//or some bytes are present in the file by previous download
	void progress(HttpDownloadFile f,long bn);
	//this method is called if exception is occured
	void exception(HttpDownloadFile f,Exception ex);	
	//this method is called when downloading is completed
	void complete(HttpDownloadFile f);
}
