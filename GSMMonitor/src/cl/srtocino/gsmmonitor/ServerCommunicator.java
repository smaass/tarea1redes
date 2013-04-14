package cl.srtocino.gsmmonitor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class ServerCommunicator {
	static private final String getAntennaPos = "http://ec2-54-225-191-225.compute-1.amazonaws.com/cosas/index.php/tarea_redes/closest_antenna";
	static private final String postMeasurement = "http://ec2-54-225-191-225.compute-1.amazonaws.com/cosas/index.php/tarea_redes/add_measurement";
	
	static public void getAntennaPos(String mnc, String latitude, String longitude, HttpTaskCallback callback) {
		Bundle postParams = new Bundle();
		postParams.putString("mnc", mnc);
		postParams.putString("latitud", latitude);
		postParams.putString("longitud", longitude);
		new PostTask(postParams, callback).execute(getAntennaPos);
	}
	
	static public void postMeasurement(Bundle measurementData, HttpTaskCallback callback) {
		new PostTask(measurementData, callback).execute(postMeasurement);
	}
	
	static private class PostTask extends HttpTask {
		private Bundle postData;
		
		public PostTask(Bundle postData, HttpTaskCallback callback) {
			super(callback);
			this.postData = postData;
		}
		
		@Override
		protected String doInBackground(String... url) {
			HttpClient client = new DefaultHttpClient();
		    HttpPost post = new HttpPost(url[0]);
		    try {
		      List<NameValuePair> params = new ArrayList<NameValuePair>();
		      if (postData != null) {
					for (String key : postData.keySet()) {
						params.add(new BasicNameValuePair(key, postData.getString(key)));
					}
		      }
		      post.setEntity(new UrlEncodedFormEntity(params));
		      HttpResponse response = client.execute(post);
		      return Utils.getStringFromStream(new InputStreamReader(response.getEntity().getContent()));
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
		    return null;
		}
	}
	
	/*
	static private class GetTask extends HttpTask {
		
		public GetTask(HttpTaskCallback callback) {
			super(callback);
		}
		
		@Override
		protected String doInBackground(String... url) {
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(url[0]);
				HttpResponse response = client.execute(request);
				return Utils.getStringFromStream(new InputStreamReader(response.getEntity().getContent()));
			} catch (IOException e) {
				onNetworkError(e);
			}
			return null;
		}
	}*/
	
	static protected abstract class HttpTask extends AsyncTask<String, Integer, String> {
		protected HttpTaskCallback callback;
		
		public HttpTask(HttpTaskCallback callback) {
			this.callback = callback;
		}
		
		@Override
		protected abstract String doInBackground(String... url);
		
		@Override
		protected void onPostExecute(String response) {
			Log.e("Http response", response);
			callback.taskComplete(response);
		}
		
		protected void onNetworkError(IOException e) {
			e.printStackTrace();
			callback.taskError(e);
		}
	}
}