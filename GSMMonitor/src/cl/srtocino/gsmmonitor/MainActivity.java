package cl.srtocino.gsmmonitor;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.format.Time;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends Activity {
	private Location currentLocation;
	private TelephonyManager telephonyManager;
	private SignalListener signalListener;
	private LocationManager locationManager;
	private TextView locationText;
	private TextView mainText;
	private TextView serverResponseText;
	private Bundle measurementData;
	private ProgressBar progressBar;
	private Context thisContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		signalListener = new SignalListener();
		telephonyManager.listen(signalListener, SignalListener.LISTEN_SIGNAL_STRENGTHS);
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		thisContext = this;
		checkGoogleServices();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		String providerName = getProviderName();
		locationManager.requestLocationUpdates(providerName, 5000, 0, listener);
		locationText = (TextView) findViewById(R.id.locationText);
		mainText = (TextView) findViewById(R.id.text);
		serverResponseText = (TextView) findViewById(R.id.text2);
		progressBar = (ProgressBar) findViewById(R.id.idProgressBar);
	}
	
	public void measure(View v) {
		GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
		String networkOperator = telephonyManager.getNetworkOperator();
		Time now = new Time();
		now.setToNow();
		String mcc = networkOperator.substring(0, 3);
		String mnc = networkOperator.substring(3);
		int cid = cellLocation.getCid();
		int lac = cellLocation.getLac();
		measurementData = newMeasurement(mcc, mnc, cid, lac, now);
		String latitude = measurementData.getString("latitude");
		String longitude = measurementData.getString("longitude");
		if (latitude != null && longitude != null) {
			ServerCommunicator.getAntennaPos(mnc, latitude, longitude, new HttpTaskCallback() {

				@Override
				public void taskComplete(String response) {
					String text = "Antena más cercana: \n\n";
					try {
						JSONObject json = new JSONObject(response);
						text += "Comuna: " + json.getString("comuna") + "\n";
						text += "Dirección: " + json.getString("direccion") + "\n";
						text += "Latitud: " + json.getString("latitud") + "\n";
						text += "Longitud: " + json.getString("longitud") + "\n";
					} catch (JSONException e) {
						text = "No se encontró ninguna antena.";
					}
					serverResponseText.setText(text);
				}

				@Override
				public void taskError(Exception e) {
					Toast.makeText(thisContext, "Error de conexión :(", Toast.LENGTH_LONG).show();
				}
			
			});
		}
		printMeasurement(mcc, mnc, cid, lac, now);
	}
	
	public void submit(View v) {
		progressBar.setVisibility(View.VISIBLE);
		ServerCommunicator.postMeasurement(measurementData, new HttpTaskCallback() {
			
			@Override
			public void taskComplete(String response) {
				progressBar.setVisibility(View.GONE);
				Toast.makeText(thisContext, "Enviado!", Toast.LENGTH_LONG).show();
			}

			@Override
			public void taskError(Exception e) {
				progressBar.setVisibility(View.GONE);
				Toast.makeText(thisContext, "Error de conexión :(", Toast.LENGTH_LONG).show();
			}
			
		});
	}
	
	private Bundle newMeasurement(String mcc, String mnc, int cid, int lac, Time time) {
		Bundle data = new Bundle();
		data.putString("mcc", mcc);
		data.putString("mnc", mnc);
		data.putString("cid", cid+"");
		data.putString("lac", lac+"");
		data.putString("potency", signalListener.getSignalStrength());
		data.putString("datetime", time.year + "-" + (time.month + 1) + "-" + time.monthDay + " " +
								   time.hour + ":" + time.minute + ":" + time.second);
		if (currentLocation != null) {
			data.putString("latitude", currentLocation.getLatitude()+"");
			data.putString("longitude", currentLocation.getLongitude()+"");
			data.putString("accuracy", currentLocation.getAccuracy()+"");
		}
		return data;
	}
	
	private void printMeasurement(String mcc, String mnc, int cid, int lac, Time time) {
		String text = "";
		text += "MCC: " + mcc + "\n";
		text += "MNC: " + mnc + "\n";
		text += "CID: " + cid + "\n";
		text += "LAC: " + lac + "\n";
		text += "Potencia: " + signalListener.getSignalStrength() + "\n\n";
		if (currentLocation != null) {
			text += "Latitud: " + currentLocation.getLatitude() + "\n";
			text += "Longitud: " + currentLocation.getLongitude() + "\n";
			text += "Precisión: " + currentLocation.getAccuracy() + " metros\n";
		}
		text += "Fecha: " + printTime(time) + "\n";
		mainText.setText(text);
	}
	
	private String printTime(Time time) {
		return time.hour + ":" + time.minute + ":" + time.second + " " + 
				time.monthDay + "/" + (time.month + 1) + "/" + time.year;
	}
	
	private void checkGoogleServices() {
		int statusCode =
	            GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (statusCode == ConnectionResult.SUCCESS) {
	    } else { 
	    	GooglePlayServicesUtil.getErrorDialog(statusCode, this, statusCode).show();
	    }
	}

	private String getProviderName() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setCostAllowed(false);
		String providerName = locationManager.getBestProvider(criteria, true); 

		if (providerName == null) {
			providerName = LocationManager.GPS_PROVIDER;
		}
		
		return providerName;
	}
	
	private final LocationListener listener = new LocationListener() {

	    @Override
	    public void onLocationChanged(Location location) {
	    	currentLocation = location;
	    	Time now = new Time();
	    	now.setToNow();
	    	locationText.setText("Posición actual (" + printTime(now) + "):\n" +
	    			"Latitud: " + location.getLatitude() +
	    			"\nLongitud: " + location.getLongitude());
	    }

		@Override
		public void onProviderDisabled(String provider) {
			activateProviderDialog(provider).create().show();
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};
	
	private AlertDialog.Builder activateProviderDialog(String providerName) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this)
		.setTitle("Location required")
		.setMessage("The " + providerName + " is disabled but GSMMonitor needs it " +
					"to compute your location. \n\nDo you want to enable it?")
		.setPositiveButton("Activate", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				enableLocationSettings();
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finishActivity();
			}
		});
		
		return dialog;
	}
	
	private void enableLocationSettings() {
	    Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	    startActivity(settingsIntent);
	}
	
	private void finishActivity() {
		this.finish();
	}
}
