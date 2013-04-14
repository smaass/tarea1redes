package cl.srtocino.gsmmonitor;


public interface HttpTaskCallback {
	public void taskComplete(String response);
	public void taskError(Exception e);
}
