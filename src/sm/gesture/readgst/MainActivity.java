package sm.gesture.readgst;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import de.dfki.ccaal.gestures.IGestureRecognitionListener;
import de.dfki.ccaal.gestures.IGestureRecognitionService;
import de.dfki.ccaal.gestures.classifier.Distribution;

public class MainActivity extends Activity {
	
	IGestureRecognitionService recognitionService;
	String activeTrainingSet = "sm";  //Training Set : set as sm.gst
	
	/**
	 * Connect GestureRecognition Service
	 * Using aidl method
	 */
	private final ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			//Setup a proxy at local activiy
			recognitionService = IGestureRecognitionService.Stub.asInterface(service);
			try {
				recognitionService.startClassificationMode(activeTrainingSet);
				//Register listener:
				recognitionService.registerListener(IGestureRecognitionListener.Stub.asInterface(gestureListenerStub));
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			recognitionService = null;
		}
	};
		
		/**
		 * What is stub?
		 *  Method stub: in computer programming, a piece of code used to stand in 
		 *  for some other programming functionality
		 *  
		 *  IBinder is an implementation of remote service.
		 */
		IBinder gestureListenerStub = new IGestureRecognitionListener.Stub() {
			//Learn. no use
			@Override
			public void onGestureLearned(String gestureName) throws RemoteException {
//				Toast.makeText(GestureTrainer.this, String.format("Gesture %s learned", gestureName), Toast.LENGTH_SHORT).show();
//				System.err.println("Gesture %s learned");
			}
			//Train. no use
			@Override
			public void onTrainingSetDeleted(String trainingSet) throws RemoteException {
//				Toast.makeText(GestureTrainer.this, String.format("Training set %s deleted", trainingSet), Toast.LENGTH_SHORT).show();
//				System.err.println(String.format("Training set %s deleted", trainingSet));
			}
			//Recognize. use!
			@Override
			public void onGestureRecognized(final Distribution distribution) throws RemoteException {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						//Use toast is not good for display. I think using TextView would be better
						//Here toast the best match gesture and the distance. Distance can be used to threshold noise gesture.
						Toast.makeText(MainActivity.this, String.format("%s: %f", distribution.getBestMatch(), distribution.getBestDistance()), Toast.LENGTH_LONG).show();
						System.err.println(String.format("%s: %f", distribution.getBestMatch(), distribution.getBestDistance()));
					}
				});
			}
		};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onPause() {
		try {
			recognitionService.unregisterListener(IGestureRecognitionListener.Stub.asInterface(gestureListenerStub));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recognitionService = null;
		unbindService(serviceConnection);
		super.onPause();
	}

	@Override
	protected void onResume() {
		//bind service
		Intent bindIntent = new Intent("de.dfki.ccaal.gestures.GESTURE_RECOGNIZER");
		bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
		super.onResume();
	}
}
