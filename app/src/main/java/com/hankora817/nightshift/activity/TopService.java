package com.hankora817.nightshift.activity;

import com.hankora817.nightshift.R;
import com.hankora817.nightshift.utils.ColorUtil;
import com.hankora817.nightshift.utils.PreferenceUtil;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class TopService extends Service
{
	private static final int NOTIFICATION_ID = 817;
	private RelativeLayout mNightLayout;
	private NotificationManager mNotificationManager;
	private final IBinder mBinder = new LocalBinder();
	
	public class LocalBinder extends Binder
	{
		TopService getService()
		{
			return TopService.this;
		}
	}
	
	
	@Nullable
	@Override
	public IBinder onBind(Intent intent)
	{
		return mBinder;
	}
	
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.i("onCreate Service", "onCreate");
		addNightView();
	}
	
	
	private void addNightView()
	{
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		mNightLayout = new RelativeLayout(this);
		mNightLayout.setLayoutParams(params);
		mNightLayout.setBackgroundColor(Color.parseColor(ColorUtil.getColorValue(PreferenceUtil.getSeekValue(this))));
		
		WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT);
		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		wm.addView(mNightLayout, windowParams);
		
		showNotificatin();
	}
	
	
	private void showNotificatin()
	{
		Intent pushIntent = new Intent(getApplicationContext(), MainActivity.class);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, pushIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setColor(Color.parseColor("#3ebdb6"))
																					.setLargeIcon(
																							BitmapFactory.decodeResource(getResources(),
																									R.drawable.app_icon))
																					.setSmallIcon(R.drawable.app_icon)
																					.setContentTitle(getString(R.string.title_notification))
																					.setStyle(
																							new NotificationCompat.BigTextStyle().bigText(getString(R.string.message_notification)))
																					.setContentText(getString(R.string.message_notification))
																					.setAutoCancel(false)
																					.setOngoing(true);
		
		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
	
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (mNightLayout != null)
		{
			((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(mNightLayout);
			mNightLayout = null;
		}
		if (mNotificationManager != null)
			mNotificationManager.cancel(NOTIFICATION_ID);
	}
	
	
	public void setNightValue(int value)
	{
		if (mNightLayout != null)
			mNightLayout.setBackgroundColor(Color.parseColor(ColorUtil.getColorValue(value)));
	}
}
