# httplib
Java based HTTP Client library and a pause/resume supported multi threaded HTTP download manager library

# HOW IT IS DIFFERENT

- You have a complete download manager built into the library
- Download large files and library will take care of number of threads to run simultaneously to download files quickly
- Library will itself detect whether file you are downloading has resume supported or not

To understand how pause/resume works using bytes range you can read this:
https://developer.mozilla.org/en-US/docs/Web/HTTP/Range_requests

# HOW TO USE

- copy usmani.http and usmani.http.downloadmanager folders in your source code and start using it

###### Making a simple HTTP request
        HttpClient cl=new HttpClient(new java.net.URL("http://rameezusmani.com"));
        cl.setRequestMethod(HttpClient.REQUEST_METHOD_GET);
        HttpResponse response=cl.executeRequest(true);
        String str=response.getAsString();
        System.out.println(str);
        cl.close();
      
###### Download a complete file using DownloadManager
        HttpDownloadManager mgr=new HttpDownloadManager();
        HttpDownloadFile file=new HttpDownloadFile(new java.net.URL("http://rameezusmani.com"));
        file.setDestinationPath("D:\\rameez.html");
        file.open();
        mgr.setDownloadEventListener(new DownloadEventListener(){
            public void start(HttpDownloadFile f) {
                System.out.println("started downloading "+f.getURL().toString()+" to "+f.getDestinationPath());	
            }

            public void progress(HttpDownloadFile f, long bn) {
                System.out.println("Download progress in "+f.getURL().toString()+"::"+bn+" bytes");
            }

            public void exception(HttpDownloadFile f, Exception ex) {
                System.out.println("Exception in "+f.getURL().toString()+"::"+ex.getMessage());
            }

            public void complete(HttpDownloadFile f) {
                System.out.println("Download completed "+f.getURL().toString()+" to "+f.getDestinationPath());			
            }
            
        });
        mgr.download(file);

###### Resume a previously paused file download

**_For example if you were downloading a file that was 20MB large but only 2KB(2048 bytes) was downloaded and then for some reason you aborted the download. If you want to resume that download from after 2KB you can do like this_**

        HttpDownloadManager mgr=new HttpDownloadManager();
        HttpDownloadFile file=new HttpDownloadFile(new java.net.URL("http://rameezusmani.com"));
        file.setBytesDone(2048) //indicating that 2KB file was already downloaded now start downloading from 2049th byte
        file.setDestinationPath("D:\\rameez.html");
        file.open();
        mgr.setDownloadEventListener(new DownloadEventListener(){
            public void start(HttpDownloadFile f) {
                System.out.println("started downloading "+f.getURL().toString()+" to "+f.getDestinationPath());	
            }

            public void progress(HttpDownloadFile f, long bn) {
                System.out.println("Download progress in "+f.getURL().toString()+"::"+bn+" bytes");
            }

            public void exception(HttpDownloadFile f, Exception ex) {
                System.out.println("Exception in "+f.getURL().toString()+"::"+ex.getMessage());
            }

            public void complete(HttpDownloadFile f) {
                System.out.println("Download completed "+f.getURL().toString()+" to "+f.getDestinationPath());			
            }
            
        });
        mgr.download(file);