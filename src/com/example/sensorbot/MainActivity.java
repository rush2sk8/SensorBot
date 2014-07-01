package com.example.sensorbot;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener{

	protected static BluetoothHandler mConnectedThread;
	private TextView tv, sent ;
	private SensorManager sensorManager;
	private Button reconnect;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_main);
		tv = (TextView)findViewById(R.id.tv);
		sensorManager = (SensorManager)getSystemService("sensor");
		reconnect = (Button)findViewById(R.id.reconnect);
		 
		sent = (TextView)findViewById(R.id.sent);
	 
		startActivity(new Intent(getApplicationContext(), ChooseDeviceToConnectTo.class));
		
		reconnect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				mConnectedThread.dispose();
				mConnectedThread= null;
				startActivity(new Intent(getApplicationContext(), ChooseDeviceToConnectTo.class));
			}
		});

	}


	@Override
	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();


		sensorManager.registerListener(this, sensorManager.getDefaultSensor(1), 3);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	 

	}

	
	public void onSensorChanged(SensorEvent event) {
		if(event.sensor.getType() ==1) {
			getAcclerometer(event);
		}
	}

	private void getAcclerometer(SensorEvent event) {
		float values[];
		float x;
		float y;
		//float z;


		values = event.values;
		x = values[0];
		y = values[1];
		//z = values[2];
		tv.setText("X: " + Math.floor(x) + " Y: "+Math.floor(y));//+" Z: "+Math.floor(z));

		double xVal = Math.floor(x);
		double yVal = Math.floor(y);
		//double zVal = Math.floor(z);

		//stopping condition
		if( (xVal>=-2.0&&xVal<=0) && (yVal>=-2.0&&yVal<=2.0)){// && (zVal>=9&&yVal<=10) ) {
			mConnectedThread.write("1"); 
			sent.setText("Stopped");
		}

		//backward condititon
		else if( (xVal>=-2.0&&xVal<=0) && (yVal>=3&&yVal<=10)) {// && (zVal>=3&&yVal<=9) ) {
			mConnectedThread.write("2");
			sent.setText("Backward");
		}
		
		//forwards condiion 
		else if( (xVal>=-2.0&&xVal<=0) && (yVal>-7&&yVal<=-3)) {
			mConnectedThread.write("3");
			sent.setText("Forward");
		}

		//forward right
		else if( (xVal<-1.0&&xVal>-7.0) && (yVal>-7&&yVal<=-3) ) {
			mConnectedThread.write("4");
			sent.setText("Forward Right");
		}
		
		//forward left
		else if( (xVal>=1.0&&xVal<=7)&& (yVal>-7&&yVal<=-3)) {
			mConnectedThread.write("5");
			sent.setText("Forward Left");
		}
	}



}
