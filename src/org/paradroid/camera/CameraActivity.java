package org.paradroid.camera;

import org.paradroid.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class CameraActivity extends Activity {
	private final static String TAG = CameraActivity.class.getSimpleName();

	RecorderWrapper mRecorderWrapper; 
	FrameLayout mFrameLayout;
	private CameraPreview mPreview;
	Button captureButton;
	PowerManager.WakeLock wl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,"Paradroid Camera");
		
		mFrameLayout = (FrameLayout) findViewById(R.id.camera_preview);

		// Add a listener to the Capture button
		captureButton  = (Button) findViewById(R.id.button_capture);
		captureButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mRecorderWrapper.isRecording()) {
							mRecorderWrapper.stopRecording();
							refreshButton();
							wl.release();
						} else {
							// initialize video camera
							mRecorderWrapper.startRecording(mPreview.getHolder().getSurface());
							wl.acquire();
							refreshButton();
						}
					}
				}
				);

		Log.d(TAG, "Activity Created");
	}

	public void refreshButton(){
		captureButton.setText(mRecorderWrapper.isRecording() ? "Stop" : "Capture");
	}

	@Override
	protected void onPause() {
		super.onPause();
		mRecorderWrapper.release();
		mFrameLayout.removeAllViews();
		Log.d(TAG, "Activity Suspended");
	}

	@Override
	protected void onResume(){
		super.onResume();
		if ((mRecorderWrapper==null) || (!mRecorderWrapper.isRecording())){
			mRecorderWrapper = new RecorderWrapper(); 
			mPreview = new CameraPreview(this, mRecorderWrapper.getCamera());        
			mFrameLayout.addView(mPreview);
		}
		refreshButton();
		Log.d(TAG, "Activity Resumed");
	}

}

