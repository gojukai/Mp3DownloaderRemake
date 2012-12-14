package cats.mp3;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cats.mp3.event.DownloadEvent;
import cats.mp3.event.DownloadListener;

public class Mp3SearchEngine{
	
	public static final String BASE_URL = "http://www.hulkshare.com";
	public static final int DEFAULT_TIMEOUT = 60000;
	
	private int timeout;
	
	private File output;
	
	protected ExecutorService service;
	
	private LinkedList<DownloadListener> listeners;
	
	public Mp3SearchEngine(final int timeout, final int threadLimit){
		this.timeout = timeout;
		service = Executors.newFixedThreadPool(threadLimit);
		
		listeners = new LinkedList<DownloadListener>();
		
		output = new File(System.getProperty("user.home"));
	}
	
	public Mp3SearchEngine(final int threadLimit){
		this(DEFAULT_TIMEOUT, threadLimit);
	}
	
	public void addDownloadListener(final DownloadListener dl){
		listeners.add(dl);
	}
	
	public void removeDownloadListener(final DownloadListener dl){
		listeners.remove(dl);
	}
	
	protected void fireDownloadStartEvents(final DownloadEvent e){
		for(final DownloadListener dl : listeners)
			dl.onStart(e);
	}
	
	protected void fireDownloadFinishEvents(final DownloadEvent e){
		for(final DownloadListener dl : listeners)
			dl.onFinish(e);
	}
	
	protected void fireDownloadUpdateEvents(final DownloadEvent e){
		for(final DownloadListener dl : listeners)
			dl.onUpdate(e);
	}
	
	public void setDestinationDirectory(final File dir){
		this.output = dir;
	}
	
	public File getDestinationDirectory(){
		return output;
	}
	
	public int getTimeout(){
		return timeout;
	}
	
	public void setTimeout(final int timeout){
		this.timeout = timeout;
	}
	
	public void shutdown(final boolean immediately){
		if(immediately)
			service.shutdownNow();
		else
			service.shutdown();
	}
	
	public void shutdown(){
		shutdown(false);
	}
	
	public List<Mp3> search(final String search) throws IOException{
		final List<Mp3> list = new LinkedList<Mp3>();
		final URL url = new URL(String.format("http://www.hulkshare.com/search.php?q=%s&advancedSearch=0&type=tracks&sortDuration=0&sortAmount=0&sortCountry=0&sortGender=0&sortGenre=0&sortStyle=0&sortDuration=0&per_page=150&sort=", URLEncoder.encode(search, "UTF-8")));
		final URLConnection connection = url.openConnection();
		connection.setUseCaches(false);
		connection.setReadTimeout(timeout);
		connection.setConnectTimeout(timeout);
		connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1309.0 Safari/537.17");
		connection.addRequestProperty("User-Content", "application/x-www-form-urlencoded");
		final Scanner reader = new Scanner(connection.getInputStream(), "UTF-8");
		while(reader.hasNextLine()){
			String line = reader.nextLine().trim();
			if(line.contains("<div class=\"userAv\">")){
				line = reader.nextLine().trim();
				final String[] split = line.split("\"");
				final String ref = BASE_URL + split[1];
				final String fileName = split[3];
				list.add(new Mp3(this, fileName, ref));
			}
		}
		reader.close();
		return Collections.unmodifiableList(list);
	}

}
