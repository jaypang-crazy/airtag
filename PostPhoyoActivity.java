package com.mobile.phototext;

import java.io.File;

import com.melody.core.http.UtilHttpConnection;
import com.melody.core.http.UtilHttpConnection.OnDataTransferListener;
import com.melody.core.http.UtilHttpConnection.OnHttpListener;
import com.melody.core.io.CacheObj;
import com.melody.core.io.UtilFileObject;
import com.mobile.phototext.util.Global;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;



public class PostPhotoActivity extends Activity implements OnClickListener, OnDataTransferListener,OnHttpListener {

	private String path = "";
	private Uri imageUri = null;
	
	private Button postphoto_btn_cancel;
	private Button postphoto_btn_post;
	private ImageView postphoto_btn_camera;
	
	private ImageView postphoto_img_preview;
	private EditText postphoto_edit_text;
	
	private String PATH_PHOTO = "";
	private CacheObj cache;
	private UtilHttpConnection connection;
	
	private RelativeLayout postphoto_loading_frame;
	private ProgressBar upload_progress_horizontal;
	
	/*private double lat;
	private double lon;
	private LocationManager locationManager;*/
	double lat;
    double lon;
	
	Handler handler = new Handler();
	
	@Override
	public void onDestroy() {
		
		super.onDestroy();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			finish();
		}
		
		return false;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.postphoto_activity);
        
        cache = new CacheObj(this, this.getString(R.string.cache_name));
        PATH_PHOTO = Environment.getExternalStorageDirectory() + "/" + cache.getcacheId() + "/" + String.valueOf(getString(R.string.photo_folder));
        UtilFileObject.chkDir(this.PATH_PHOTO);
        
        init();
    }
    
    private void init() {
    	
    	postphoto_btn_cancel = (Button) findViewById(R.id.postphoto_btn_cancel);
    	postphoto_btn_post = (Button) findViewById(R.id.postphoto_btn_post);
    	
    	postphoto_btn_camera = (ImageView) findViewById(R.id.postphoto_btn_camera);
    	postphoto_img_preview = (ImageView) findViewById(R.id.postphoto_img_preview);
    	
    	postphoto_edit_text = (EditText) findViewById(R.id.postphoto_edit_text);
    	
    	postphoto_loading_frame = (RelativeLayout) findViewById(R.id.postphoto_loading_frame);
    	upload_progress_horizontal = (ProgressBar) findViewById(R.id.upload_progress_horizontal);
    	closeLoadingFrame(false);
    	
    	postphoto_btn_cancel.setOnClickListener(this);
    	postphoto_btn_post.setOnClickListener(this);
    	postphoto_btn_camera.setOnClickListener(this);
    }

	public void onClick(View v) {

		switch(v.getId()) {
			case R.id.postphoto_btn_cancel:
				
				finish();
				break;
			case R.id.postphoto_btn_post:
				
				uploadPhoto();
				break;
			case R.id.postphoto_btn_camera:
				
				Global.capturePhoto(this, PATH_PHOTO, Global.ACTION_REQUEST_CAMERA);
				
				break;
			default:
				break;
		}
	}
	
	private void uploadPhoto(){
		
		String text = String.valueOf(postphoto_edit_text.getText());
		String latitude = String.valueOf(lat);
		String longitude = String.valueOf(lon);
		
		if(!path.trim().equalsIgnoreCase("") && !text.trim().equalsIgnoreCase("")) {
			
			try {
				getposition();
				
				Global.showAlertDialog(this, latitude);
				Global.showAlertDialog(this, longitude);
				connection = new UtilHttpConnection(this, this);
				connection.setURL(Global.API_UPLOAD, "upload");
				connection.setMethod(UtilHttpConnection.METHOD_POST);
				connection.addField(Global.API_UPLOAD_PARAM_01, text);
				connection.addFile(Global.API_UPLOAD_PARAM_02, new File(path), "jpg");
				connection.addField(Global.API_UPLOAD_PARAM_03, latitude);
				connection.addField(Global.API_UPLOAD_PARAM_04, longitude);
				
				connection.send(false);
				closeLoadingFrame(true);
				
			
			} catch(Exception e) {
				e.printStackTrace();
				closeLoadingFrame(false);
			}
			
		} else {
			
			//Error Case
		}
	}
	
	/*public void getposition(){
		 
		 LocationListener locationListener = new LocationListener(){  
			 
			 	public void onLocationChanged(Location location) {
			 	
					// TODO Auto-generated method stub
			 		lat = location.getLatitude();
			        lon = location.getLongitude();       		       
			        //String latitude = String.valueOf(location.getLatitude());
					//String longtitude = String.valueOf(location.getLongitude());
				}
				public void onProviderDisabled(String provider) {
					// TODO Auto-generated method stub			
				}
				public void onProviderEnabled(String provider) {
					
					// TODO Auto-generated method stub					
				}
				public void onStatusChanged(String provider, int status,Bundle extras) {
					// TODO Auto-generated method stub	
				}
	        };	        	 
	        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
	        
	    }*/

		private void getposition(){
		Location location;
		
		LocationManager locationManager;
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		 location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		 lat = location.getLatitude();
		 lon = location.getLongitude();
		 //lat.setText("lat : "+latitude);
		 //lon.setText("long : "+longitude);
	    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(resultCode == Activity.RESULT_OK) {
			
			switch (requestCode) {
			
				case Global.ACTION_REQUEST_CAMERA:
					
					path = data.getExtras().getString("path");
					imageUri = data.getData();
					postphoto_img_preview.setImageURI(imageUri);
					
					Global.showAlertDialog(this, path);
					break;
				default:
					break;
			}
			
		}
	}

	public void onProcess(final int progress) {

		handler.post(new Runnable(){

			public void run() {

				upload_progress_horizontal.setProgress(progress);
			}
		});
	}
	
	private void closeLoadingFrame(boolean visibility) {
		
		if(visibility)
			postphoto_loading_frame.setVisibility(View.VISIBLE);
		else
			postphoto_loading_frame.setVisibility(View.GONE);
	}

	public void onHttpError(final String msg) {
		
		handler.post(new Runnable(){
			
			public void run() {

				Global.showAlertDialog(PostPhotoActivity.this, msg);
				closeLoadingFrame(false);
			}
		});
	}

	public void onHttpSuccess(final String data, final String connection_key) {

		handler.post(new Runnable(){
			
			public void run() {

				if(data.equalsIgnoreCase("200")) {
					
					postphoto_img_preview.setImageResource(android.R.drawable.ic_menu_gallery);
					postphoto_edit_text.setText("");
					
				} else {
					//Error Case
				}
				closeLoadingFrame(false);
				upload_progress_horizontal.setProgress(0);
			}
		});
	}

}



