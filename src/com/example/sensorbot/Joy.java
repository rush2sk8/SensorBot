package com.example.sensorbot;

import android.app.Activity;
import android.os.Bundle;

import com.example.sensorbot.JoystickView.OnJoystickMoveListener;

public class Joy extends Activity {

	JoystickView joyView;
	BluetoothHandler mConnectedThread;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		joyView = new JoystickView(this);
		setContentView(joyView);

		mConnectedThread = MainActivity.mConnectedThread;

		joyView.setOnJoystickMoveListener(new OnJoystickMoveListener() {

			@Override
			public void onValueChanged(int angle, int power, int direction) {

				System.out.println("Power: "+power+" Direction: "+ direction);

				if(power==0) 
					mConnectedThread.write("1");

				else if(direction==0||direction==3)
					mConnectedThread.write("3");

				else if(direction<=2&&direction>=1)
					mConnectedThread.write("4");

				else if(direction>=4&&direction<=5)
					mConnectedThread.write("5");

				else if(direction==7)
					mConnectedThread.write("2");

				else if(direction==8)
					mConnectedThread.write("6");

				else if(direction==6)
					mConnectedThread.write("7");

			}
		}, JoystickView.DEFAULT_LOOP_INTERVAL);

	}

	@Override
	public void onBackPressed() {
		finish();

	}
}
