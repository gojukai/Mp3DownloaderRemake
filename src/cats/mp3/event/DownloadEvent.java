package cats.mp3.event;

import java.io.File;

import cats.mp3.Mp3;
import cats.mp3.Mp3SearchEngine;

public class DownloadEvent {
	
	private Mp3 mp3;
	private double bytesRead;
	private double totalBytes;
	private double percent;
	
	private boolean error;
	
	private File output;
	
	public DownloadEvent(final Mp3 mp3, final double bytesRead, final double totalBytes, final File output, final boolean error){
		this.mp3 = mp3;
		this.bytesRead = bytesRead;
		this.totalBytes = totalBytes;
		this.output = output;
		
		percent = (bytesRead / totalBytes) * 100;
		
		this.error = error;
	}
	
	public Mp3SearchEngine getEngine(){
		return mp3.getEngine();
	}
	
	public File getOutputFile(){
		return output;
	}
	
	public double getBytesRead(){
		return bytesRead;
	}
	
	public Mp3 getMp3(){
		return mp3;
	}
	
	public double getTotalBytes(){
		return totalBytes;
	}
	
	public double getPercent(){
		return percent;
	}
	
	public boolean isError(){
		return error;
	}
	
	public boolean isSuccessfull(){
		return !error;
	}

}
