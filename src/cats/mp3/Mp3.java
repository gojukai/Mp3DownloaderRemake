package cats.mp3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import cats.mp3.event.DownloadEvent;

public class Mp3{
	
	private Mp3SearchEngine engine;
	
	private String fileName;
	private String ref;
	
	private String size;
	private String plays;
	private String likes;
	private String dislikes;
	
	private String downloadLink;
	
	private Runnable downloader;
			
	public Mp3(final Mp3SearchEngine engine, final String fileName, final String ref){
		this.engine = engine;
		this.fileName = fileName.replaceAll("&#039;", "'").replaceAll("%20", " ").replaceAll("&amp;", "&").replaceAll("&quot;", "\"").replaceAll(File.pathSeparator, "-");
		if(!this.fileName.endsWith(".mp3")) this.fileName += ".mp3";
		this.ref = ref;
				
		size = null;
		plays = null;
		likes = null;
		dislikes = null;
		
		downloadLink = null;
		
		downloader = new Runnable(){
			public void run(){
				InputStream input = null;
				FileOutputStream output = null;
				double total = 0;
				double bytesRead = 0;
				int singleRead = 0;
				byte[] buffer = new byte[15600];
				File out = new File(engine.getDestinationDirectory(), Mp3.this.fileName);
				try{
					final URL url = new URL(downloadLink);
					final URLConnection connection = url.openConnection();
					connection.setUseCaches(false);
					connection.setReadTimeout(engine.getTimeout());
					connection.setConnectTimeout(engine.getTimeout());
					connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1309.0 Safari/537.17");
					connection.addRequestProperty("User-Content", "application/x-www-form-urlencoded");
					input = connection.getInputStream();
					output = new FileOutputStream(out);
					total = connection.getContentLength();
					engine.fireDownloadStartEvents(new DownloadEvent(Mp3.this, bytesRead, total, out, false));
					engine.fireDownloadUpdateEvents(new DownloadEvent(Mp3.this, bytesRead, total, out, false));
					while((singleRead = input.read(buffer)) > 0){
						output.write(buffer, 0, singleRead);
						bytesRead += singleRead;
						engine.fireDownloadUpdateEvents(new DownloadEvent(Mp3.this, bytesRead, total, out, false));
					}
					input.close();
					output.close();
					engine.fireDownloadFinishEvents(new DownloadEvent(Mp3.this, bytesRead, total, out, false));
				}catch(IOException e){
					engine.fireDownloadFinishEvents(new DownloadEvent(Mp3.this, bytesRead, total, out, true));
				}finally{
					try{
					if(input != null) 
						input.close();
					if(output != null)
						output.close();
					}catch(IOException e){
						engine.fireDownloadFinishEvents(new DownloadEvent(Mp3.this, bytesRead, total, out, true));
					}
				}
			}
		};
	}
	
	public void download(){
		engine.service.execute(downloader);
	}
	
	public boolean isDownloadable(){
		return downloadLink != null;
	}
	
	public boolean canDownload(){
		return isDownloadable() && engine.getDestinationDirectory() != null;
	}
	
	public Mp3SearchEngine getEngine(){
		return engine;
	}
	
	public String getSize(){
		return size;
	}
	
	public String getPlays(){
		return plays;
	}
	
	public String getLikes(){
		return likes;
	}
	
	public String getDislikes(){
		return dislikes;
	}
	
	public String getDownloadLink(){
		return downloadLink;
	}
	
	public String getFileName(){
		return fileName;
	}
	
	public String getReference(){
		return ref;
	}
	
	public boolean init() throws IOException{
		final URL url = new URL(ref);
		final URLConnection connection = url.openConnection();
		connection.setUseCaches(false);
		connection.setReadTimeout(engine.getTimeout());
		connection.setConnectTimeout(engine.getTimeout());
		connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1309.0 Safari/537.17");
		connection.addRequestProperty("User-Content", "application/x-www-form-urlencoded");
		final Scanner reader = new Scanner(connection.getInputStream(), "UTF-8");
		while(reader.hasNextLine()){
			final String line = reader.nextLine().trim();
			if(line.contains("<span class=\"tsSize\">"))
				size = line.split("<span class=\"tsSize\">")[1].split("</span>")[0];
			if(line.contains("<span class=\"tsPlays\">"))
				plays = line.split("<span class=\"tsPlays\">")[1].split("</span>")[0];
			if(line.contains("<span class=\"tsFavs\">"))
				likes = line.split("<span class=\"tsFavs\">")[1].split("</span>")[0];
			if(line.contains("<span class=\"tsDowns\">"))
				dislikes = line.split("<span class=\"tsDowns\">")[1].split("</span>")[0];
			if(line.contains("bigDownloadBtn basicDownload"))
				downloadLink = line.split("\"")[1];
			if(downloadLink != null) break;
		}
		reader.close();
		return size != null && plays != null && likes != null && dislikes != null && downloadLink != null;
	}
	
	public String toString(){
		return String.format("Name: %s | Size: %s | Plays: %s | Likes: %s | Dislikes: %s | Download: %s", fileName, size, plays, likes, dislikes, downloadLink);
	}
	
}
