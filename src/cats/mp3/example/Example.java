package cats.mp3.example;

import java.io.File;
import java.io.IOException;
import java.util.List;

import cats.mp3.Mp3;
import cats.mp3.Mp3SearchEngine;
import cats.mp3.event.DownloadEvent;
import cats.mp3.event.DownloadListener;

public class Example {
	
	public static void main(String[] args) throws IOException{
		final DownloadListener listener = new DownloadListener(){
			public void onStart(DownloadEvent event){
				System.out.println("Started: " + event.getMp3().getFileName());
			}

			public void onUpdate(DownloadEvent event){
				System.out.println(String.format("%1.0f / %1.0f (%1.2f%%)", event.getBytesRead(), event.getTotalBytes(), event.getPercent()));
			}

			public void onFinish(DownloadEvent event){
				System.out.println("Finished: " + event.isError());
			}
			
		};
		final Mp3SearchEngine engine = new Mp3SearchEngine(10);
		engine.addDownloadListener(listener);
		engine.setDestinationDirectory(new File(System.getProperty("user.home"), "Desktop"));
		final List<Mp3> mp3s = engine.search("asking alexandria");
		final Mp3 m = mp3s.get(0);
		m.init();
		m.download();
	}

}
