package usmani.http.downloadmanager;

import java.util.Vector;

public class ChunksPool {
	
	//collection of downloaded chunks
	private Vector<DownloadedChunk> chunks;
	/*
	 * No argument constructor
	 */
	public ChunksPool(){
		//initialize the collection of chunks
		chunks=new Vector<DownloadedChunk>();
	}	
	
	/*
	 * 
	 * This method will add a chunk to the pool
	 * 
	 * Parameters:
	 * 
	 * b[in]: Chunk to be added
	 */
	public void add(DownloadedChunk b){
		synchronized(chunks){
			//add the chunk to the pool
			chunks.add(b);
		}
		
	}
	
	/*
	 * This function clears the collection of chunks
	 * in a pool
	 */
	public void clear(){
		chunks.clear();
	}
	
	/*
	 * 
	 * This method checks whether the chunk pool is empty
	 * 
	 * return val : true if pool is empty otherwise false
	 */
	public boolean isEmpty(){
		return chunks.isEmpty();
	}
	
	/*
	 * 
	 * Returns the chunk whose offset matches with start
	 * 
	 * Paramaters:
	 * 
	 * start[in]: offset to match
	 * 
	 * Return value: Downloaded chunk matched
	 * 
	 */
	public DownloadedChunk getChunk(long start){
		
		synchronized(chunks){
			if (chunks.size()==0){
				return null;
			}
		
			else{
			
				DownloadedChunk temp;
			
				for (int a=0;a<chunks.size();a++){
				
					temp=chunks.get(a);				
				
					if (temp.getOffset()==start){
						chunks.remove(a);
						return temp;
					}				
				}			
				return null;
			}
		}
	}
}
