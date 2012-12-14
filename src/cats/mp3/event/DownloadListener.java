package cats.mp3.event;

public interface DownloadListener {
	
	public void onStart(final DownloadEvent event);
	
	public void onUpdate(final DownloadEvent event);
	
	public void onFinish(final DownloadEvent event);

}
