package com.example.sensorbot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Rushad Antia
 * 
 * This class reduces the baggage of setting up a bluetooth 
 * connection.
 * 
 * 
 * */

public class BluetoothHandler extends Activity {

	//used for debugging
	private static final String TAG = "bluetooth2";

	//id necessary for making a bluetooth connection
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	//an android tool that can properly recieve messages
	private Handler h;

	//bluetooth adapter
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private ConnectedThread mConnectedThread;
	private final int RECIEVE_MESSAGE =1;
	private Context context;

	//MAC address to connect to 
	private static String address;

	/**
	 * called by the android OS that will safely dispose of the activity
	 * */
	public void dispose() {
		
		//killz the adapter
		btAdapter = null;
		try {
		    
		    //closes the socket
			btSocket.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
		
		//disposes the closed socket
		btSocket = null;
	
		try {
		    
		    //joins the other thread to the main thread
			mConnectedThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//disposes the thread
		mConnectedThread = null;
	}


    /**
     * Sends data to the end device
     * */
	public void write(String data) {

    
		if(mConnectedThread.isAlive()) //error checking
			mConnectedThread.write(data);

	}

    /**
     * Self Explanatory
     * @returns true if socket is available 
     * */
	public boolean isSocketAvailable() {
		return btSocket.isConnected();
	}

    /**
     * Creates an instance oF the bluetooth handler
     * 
     * */
	public BluetoothHandler(Context c, String ad) {

        //makes the ivs = to the parameters
		address = ad;
		btAdapter = BluetoothAdapter.getDefaultAdapter();	
		context = c;// get Bluetooth adapter
	
	    //checks bluetooth radio
		checkBTState();
		
		//creates handler to recieve the data
		createHandler();

		// Set up a pointer to the remote node using it's address.
		BluetoothDevice device = btAdapter.getRemoteDevice(address);

		try {
		    
		    //creates a bluetooth socket (connection) between this device and the other device
			btSocket = createBluetoothSocket(device);
	
		} catch (IOException e) {
			errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
		}


		// Discovery is resource intensive.  Make sure it isn't going on
		// when you attempt to connect and pass your message.
		btAdapter.cancelDiscovery();

		// Establish the connection.  This will block until it connects.
		Log.d(TAG, "...Connecting...");
		try {
			btSocket.connect();
			Log.d(TAG, "....Connection ok...");
		} catch (IOException e) {
			try {


				btSocket.close();
			} catch (IOException e2) {
				errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
			}
		}

		// Create a data stream so we can talk to server.
		Log.d(TAG, "...Create Socket...");

		mConnectedThread = new ConnectedThread(btSocket);
		mConnectedThread.start();

	}

	private void createHandler(){
		h = new Handler() {
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case RECIEVE_MESSAGE:													// if receive massage

					//Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
					break;
				}
			};
		};

	}


	@Override
	public void onPause() {
		super.onPause();

		Log.d(TAG, "...In onPause()...");

		try     {
			btSocket.close();
		} catch (IOException e2) {
			errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
		}
	}

	private void checkBTState() {

		if(btAdapter==null) { 
			errorExit("Fatal Error", "Bluetooth not support");
		} else {
			if (btAdapter.isEnabled()) {
				Log.d(TAG, "...Bluetooth ON...");
			} else {

				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, 1);
			}
		}
	}

	private void errorExit(String title, String message){
		Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
		finish();
	}


	private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
		if(Build.VERSION.SDK_INT >= 10){
			try {
				final Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
				return (BluetoothSocket) m.invoke(device, MY_UUID);
			} catch (Exception e) {
				Log.e(TAG, "Could not create Insecure RFComm Connection",e);
			}
		}
		return  device.createRfcommSocketToServiceRecord(MY_UUID);
	}

	private class ConnectedThread extends Thread {
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			InputStream tmpIn = null;
			OutputStream tmpOut = null;


			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) { }

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			byte[] buffer = new byte[256];  // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);	
					h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();		
				} catch (IOException e) {
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */

		public void write(String message) {
			Log.d(TAG, "...Data to send: " + message + "...");
			byte[] msgBuffer = message.getBytes();

			try {
				if(mmOutStream!=null)
					mmOutStream.write(msgBuffer);

			} catch (IOException e) {
				Log.d(TAG, "...Error data send: " + e.getMessage() + "...");     
				Toast.makeText(getApplicationContext(), "Caught an exception while sending data", Toast.LENGTH_LONG).show();

			}
		}


	}


	public void onBackPressed() {
		//do nothing
	}

}
