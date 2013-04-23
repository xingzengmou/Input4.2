package com.only.input;
    
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.net.LocalServerSocket;
import android.net.LocalSocket;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManager;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;  
import android.view.MotionEvent.PointerProperties;  
import android.hardware.input.InputManager;
import android.view.InputDevice;

 
class OnlyInput {
	private static final String TAG = "OnlyInput";
	private ServerSocket mServerSocket = null;
	private Socket socket = null;
	private BufferedReader is = null;
	private PrintWriter pw;
	private static final boolean debug = true;

	private DatagramSocket mDatagramSocket;
	private DatagramPacket mDatagramPacket;
	private int port = 6000;
	private byte[] msg = new byte[1024];
	
	public static void main(String[] args) {
		Log.e(TAG, "this is OnlyInput above 4.0 android os version 1.0");
		OnlyInput mInput = new OnlyInput();
		mInput.udpMonitor();
	}
	
	public void createServer() {
		try {
			mServerSocket = new ServerSocket(port);
			socket = mServerSocket.accept();
			try {
				is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				pw = new PrintWriter(socket.getOutputStream());
				Log.e(TAG, "connect address = " + socket.getInetAddress());
				Log.e(TAG, "socket.isclosed = " + socket.isConnected());
				while (true) {
					try {
						Log.e(TAG, "is.readline");
						String line = is.readLine();
						Log.e(TAG, "line = " + line);
						if (line != null) {
							processData(line);
						}
						pw.println("got server version 1.6");
						pw.flush();
						//Thread.sleep(500);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void udpMonitor() {
			try {
				mDatagramSocket = new DatagramSocket(port);
				Log.e(TAG, "msg[0] = " + msg[0] + " msg[1] = " + msg[1]);
				mDatagramPacket = new DatagramPacket(msg, msg.length);
			} catch (Exception e) {
				e.printStackTrace();
			}
			while (true) {
				try {
					Log.e(TAG, "udpRecvThread");
					if (mDatagramSocket == null) {
						mDatagramSocket = new DatagramSocket(port);	
					}
					mDatagramSocket.receive(mDatagramPacket);
					Log.e(TAG, "udp received");
					if (debug) {
						Log.e(TAG, "udp receive address = " + mDatagramPacket.getAddress() + " msg = " + new String(msg).trim() + " msg[20] = " + msg[20]);
					}
					processData(new String(msg).trim());
					clearMsg();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}
	
	private void clearMsg() {
		for (int i = 0; i < msg.length; i ++) {
			msg[i] = 32;
		}	
	}
		

	private Runnable monitorRunnable = new Runnable() {
		public void run() {
			Log.e(TAG, "monitorRunnable");
			try {
				is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				pw = new PrintWriter(socket.getOutputStream());
				Log.e(TAG, "connect address = " + socket.getInetAddress());
				Log.e(TAG, "socket.isclosed = " + socket.isConnected());
				while (true) {
					try {
						Log.e(TAG, "is.readline");
						String line = is.readLine();
						Log.e(TAG, "line = " + line);
						if (line != null) {
							processData(line);
						}
						pw.println("got server version 1.6");
						pw.flush();
						//Thread.sleep(500);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	
	private void processData(String data) {
		String[] listData = data.split(":");
		if (debug) Log.e(TAG, "listData = " + listData);
		if (listData[0].trim().equals("injectTouch")) {
			injectTouchProcess(listData);
		} else if (listData[0].trim().equals("injectKey")) {
			injectKeyProcess(listData);
		}
	} 

	private void injectTouchProcess(String[] listD) {
		int pointerCount = Integer.parseInt(listD[1].trim());
		int state = Integer.parseInt(listD[2].trim());
		long now = SystemClock.uptimeMillis();
		int index = 3;
		if (debug) {
			Log.e(TAG, "pointerCount = " + pointerCount + " state = " + state);
			for (String st: listD) {
				Log.e(TAG,"injectTouchProcess st = " + st);	
			}
		}
		
		if (pointerCount == 0) {
			Log.e(TAG, "injectTouchProcess data is invalid");
			return;
		}
		
		PointerProperties[] properties = new PointerProperties[pointerCount];  
		PointerCoords[] pointerCoords = new PointerCoords[pointerCount];
		
		for (int i = 0; i < pointerCount; i ++) {
			PointerProperties mPointerProperties = new PointerProperties();  
			mPointerProperties.id= i;  
			mPointerProperties.toolType = MotionEvent.TOOL_TYPE_FINGER;  
			properties[i] = mPointerProperties; 
			
			PointerCoords mPointerCoords = new PointerCoords();
			mPointerCoords.x = Float.parseFloat(listD[index++].trim());
			mPointerCoords.y = Float.parseFloat(listD[index++].trim()); 
			mPointerCoords.pressure = 1;  
			mPointerCoords.size = 1;  
			pointerCoords[i] = mPointerCoords;  
			if (debug) {
				Log.e(TAG, "pointerIndex = " + i + " x = " + mPointerCoords.x + " y = " + mPointerCoords.y + " index = " + index);
			}
		}
		
		if (1 == pointerCount) {
			injectTouchEvent(MotionEvent.obtain(now, now, state, 1, properties, pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0));
		} else {
			//injectTouchEvent(MotionEvent.obtain(now, now, (state | ((pointerCount-1) << 8)), pointerCount, properties, pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0));
			injectTouchEvent(MotionEvent.obtain(now, now, (state), pointerCount, properties, pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0));
		}
	}

	private void injectTouchEvent(final MotionEvent event) {
		if (event == null) {
			Log.e(TAG, " MotionEvent  event == null");
			return;
		}
		try {
			event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
			InputManager.getInstance().injectInputEvent(event, InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//injectKey:keyCode:scanCode:state
	//state = ACTION_DOWN, ACTION_UP and so on.
	private void injectKeyProcess(String[] listD) {
		Log.e(TAG, "injectKeyProcess keyCode = " + listD[1] + " state = " + listD[3] + " listD.length = " + listD.length);
		for (String st: listD) {
			Log.e(TAG,"injectKeyProcess st = " + st);	
		}
		int eventCode = Integer.parseInt(listD[1].trim());
		int scanCode = Integer.parseInt(listD[2].trim());
		int state = Integer.parseInt(listD[3].trim());
		long now = SystemClock.uptimeMillis();
		
		KeyEvent event = new KeyEvent(now, now, state, eventCode, 0);
		Log.e(TAG, "injectKeyEvent keyCode = " + event.getKeyCode());
		try {
			InputManager.getInstance().injectInputEvent(event,InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
		} catch (Exception e) {
			Log.e(TAG, "exception = " + e.getMessage());
		}
	}
	
}
