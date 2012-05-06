package org.paradroid.camera;

import java.io.IOException;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.Surface;

public class RecorderWrapper {

	public static final String TAG = RecorderWrapper.class.getSimpleName();
	
	private Camera mCamera;
	private MediaRecorder mMediaRecorder;
	private boolean isRecording;
	
	public RecorderWrapper(){
		mCamera = getCameraInstance();
		isRecording = false; 
	}
	
	private void releaseMediaRecorder(){
		if (mMediaRecorder != null) {
			mMediaRecorder.reset();   // clear recorder configuration
			mMediaRecorder.release(); // release the recorder object
			mMediaRecorder = null;
			mCamera.lock();           // lock camera for later use
		}
	}

	/** A safe way to get an instance of the Camera object. */
	private static Camera getCameraInstance(){
		Camera c = null;
		try {
			c = Camera.open(0); // attempt to get a Camera instance
			Log.v(TAG,"Got Camera.");
		}
		catch (Exception e){
			Log.v(TAG,e.getLocalizedMessage());
		}
		return c;
	}
	
	private void releaseCamera(){
		if (mCamera != null){
			mCamera.release();        // release the camera for other applications
			mCamera = null;
			Log.v(TAG,"Camera Release");
		}
	}
	
	public void release(){
		releaseMediaRecorder();       // if you are using MediaRecorder, release it first
		releaseCamera();              // release the camera immediately on pause event
	}
	
	public boolean isRecording(){
		return isRecording; 
	}

	private boolean prepareVideoRecorder(Surface surface){
		//	    mCamera = getCameraInstance();
		mMediaRecorder = new MediaRecorder();

		// Step 1: Unlock and set camera to MediaRecorder
		mCamera.unlock();
		mMediaRecorder.setCamera(mCamera);

		// Step 2: Set sources
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		// Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
		mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
		
//		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//		mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//		mMediaRecorder.setVideoSize(1280,720);

		// Step 4: Set output file
		mMediaRecorder.setOutputFile(MediaEnviornment.getOutputMediaFile(MediaEnviornment.MEDIA_TYPE_VIDEO).toString());

		// Step 5: Set the preview output
		mMediaRecorder.setPreviewDisplay(surface);

		// Step 6: Prepare configured MediaRecorder
		try {
			mMediaRecorder.prepare();
		} catch (IllegalStateException e) {
			Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
			releaseMediaRecorder();
			return false;
		} catch (IOException e) {
			Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
			releaseMediaRecorder();
			return false;
		}
		Log.v(TAG,"MediaRecorder is prepared");
		return true;
	}
	
	public Camera getCamera(){
		return mCamera; 
	}
	
	public void startRecording(Surface surface){

		
		if (prepareVideoRecorder(surface)) {
			// Camera is available and unlocked, MediaRecorder is prepared,
			// now you can start recording
			mMediaRecorder.start();
			// inform the user that recording has started
			isRecording = true;			
		} else {
			// prepare didn't work, release the camera
			releaseMediaRecorder();
			isRecording = false;
		}		
	}
	
	public void stopRecording(){
		// stop recording and release camera
		mMediaRecorder.stop();  // stop the recording
		releaseMediaRecorder(); // release the MediaRecorder object
		mCamera.lock();         // take camera access back from MediaRecorder

		// inform the user that recording has stopped
		isRecording = false;		
	}
	
}
