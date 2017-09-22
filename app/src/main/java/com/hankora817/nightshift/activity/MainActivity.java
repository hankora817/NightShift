package com.hankora817.nightshift.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.hankora817.nightshift.R;
import com.hankora817.nightshift.utils.ColorUtil;
import com.hankora817.nightshift.utils.PreferenceUtil;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TimePicker;

public class MainActivity extends AppCompatActivity
{
	private final int ALARM_REQUEST_CODE = 2003;
	private final int ALARM_OFF_CODE = 2004;
	private final String FLAG_TIME_START = "time_start";
	private final String FLAG_TIME_END = "time_end";
	private InterstitialAd interstitialAd;
	private RelativeLayout mNightLayout;
	private TopService mService;
	private boolean mBound = false;
	private Context mContext;
	private Button btnStartTime, btnEndTime;
	private Switch mSwitchOnOff, mSwitchScedule;
	private SeekBar mSeekbar;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mContext = this;
		
		btnStartTime = (Button) findViewById(R.id.btn_start_time);
		btnEndTime = (Button) findViewById(R.id.btn_end_time);
		mSwitchOnOff = (Switch) findViewById(R.id.switch_on_off);
		mSwitchScedule = (Switch) findViewById(R.id.switch_sceduled);
		mSeekbar = (SeekBar) findViewById(R.id.seek_night);
		
		mSwitchScedule.setChecked(PreferenceUtil.isSceduled(mContext));
		mSeekbar.setProgress(PreferenceUtil.getSeekValue(mContext));
		btnStartTime.setText(PreferenceUtil.getStartTime(mContext));
		btnEndTime.setText(PreferenceUtil.getEndTime(mContext));
		
		mSeekbar.setOnSeekBarChangeListener(seekBarChangeListener);
		mSwitchOnOff.setOnCheckedChangeListener(onOffCheckedListener);
		mSwitchScedule.setOnCheckedChangeListener(onSceduleListener);
		btnStartTime.setOnClickListener(timeClickListener);
		btnEndTime.setOnClickListener(timeClickListener);
		
		if (isServiceRunningCheck())
		{
			stopService(new Intent(mContext, TopService.class));
			bindService(new Intent(mContext, TopService.class), mConnection, Context.BIND_AUTO_CREATE);
			mSwitchOnOff.setChecked(true);
		}
		else
		{
			mSwitchOnOff.setChecked(false);
		}
		
		/**
		 * load ad type banner
		 */
		AdView mAdView = (AdView) findViewById(R.id.adview_banner);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);
		
		/**
		 * load ad type front
		 */
		interstitialAd = new InterstitialAd(mContext);
		interstitialAd.setAdUnitId(getString(R.string.banner_ad_unit_id_front));
		adRequest = new AdRequest.Builder().build();
		interstitialAd.setAdListener(new AdListener()
		{
			@Override
			public void onAdLoaded()
			{
				super.onAdLoaded();
				if (interstitialAd.isLoaded())
					interstitialAd.show();
			}
		});
		interstitialAd.loadAd(adRequest);
	}
	
	
	@Override
	protected void onResume()
	{
		super.onResume();
		if (isServiceRunningCheck())
			mSwitchOnOff.setChecked(true);
		else
			mSwitchOnOff.setChecked(false);
	}
	
	private View.OnClickListener timeClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if (v.getId() == R.id.btn_start_time)
				showTimePickDialog(FLAG_TIME_START, (Button) v);
			else if (v.getId() == R.id.btn_end_time)
				showTimePickDialog(FLAG_TIME_END, (Button) v);
		}
	};
	
	private CompoundButton.OnCheckedChangeListener onOffCheckedListener = new CompoundButton.OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			if (!isServiceRunningCheck() && isChecked)
			{
				bindService(new Intent(mContext, TopService.class), mConnection, Context.BIND_AUTO_CREATE);
			}
			else
			{
				if (mService != null)
				{
					unbindService(mConnection);
					mBound = false;
				}
			}
		}
	};
	
	private CompoundButton.OnCheckedChangeListener onSceduleListener = new CompoundButton.OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			PreferenceUtil.setSceduled(isChecked, mContext);
			AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			if (isChecked)
			{
				final Calendar startCalendar = Calendar.getInstance();
				final Calendar endCalendar = Calendar.getInstance();
				try
				{
					SimpleDateFormat sdFormat = new SimpleDateFormat("HH:mm", Locale.KOREA);
					Date startDate = sdFormat.parse(btnStartTime.getText().toString());
					Date endDate = sdFormat.parse(btnEndTime.getText().toString());
					startCalendar.setTime(startDate);
					endCalendar.setTime(endDate);
					
					Calendar newStartCal = Calendar.getInstance();
					newStartCal.set(Calendar.HOUR_OF_DAY, startCalendar.get(Calendar.HOUR_OF_DAY));
					newStartCal.set(Calendar.MINUTE, startCalendar.get(Calendar.MINUTE));
					newStartCal.set(Calendar.SECOND, 0);
					newStartCal.set(Calendar.MILLISECOND, 0);
					
					Calendar newEndCal = Calendar.getInstance();
					newEndCal.set(Calendar.HOUR_OF_DAY, endCalendar.get(Calendar.HOUR_OF_DAY));
					newEndCal.set(Calendar.MINUTE, endCalendar.get(Calendar.MINUTE));
					newEndCal.set(Calendar.SECOND, 0);
					newEndCal.set(Calendar.MILLISECOND, 0);
					
					setStartAlarm(newStartCal);
					setEndAlarm(newEndCal);
				}
				catch (ParseException e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				Intent intent = new Intent(mContext, OffBroadCast.class);
				PendingIntent pender = PendingIntent.getBroadcast(mContext, ALARM_REQUEST_CODE, intent, 0);
				PendingIntent pender2 = PendingIntent.getBroadcast(mContext, ALARM_OFF_CODE, intent, 0);
				alarm.cancel(pender);
				alarm.cancel(pender2);
			}
		}
	};
	
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (mBound)
		{
			unbindService(mConnection);
			startService(new Intent(mContext, TopService.class));
		}
	}
	
	private ServiceConnection mConnection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			Log.i("onServiceConnected", "service connected");
			TopService.LocalBinder binder = (TopService.LocalBinder) service;
			mService = binder.getService();
			mBound = true;
		}
		
		
		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			Log.i("onServiceDisconnected", "service disconnected");
			mBound = false;
		}
	};
	
	
	public boolean isServiceRunningCheck()
	{
		ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
		{
			if ("com.hankora817.nightshift.activity.TopService".equals(service.service.getClassName()))
			{
				return true;
			}
		}
		return false;
	}
	
	
	private void addNightView()
	{
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		mNightLayout = new RelativeLayout(this);
		mNightLayout.setLayoutParams(params);
		mNightLayout.setBackgroundColor(Color.parseColor(ColorUtil.getColorValue(PreferenceUtil.getSeekValue(mContext))));
		
		WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT);
		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		wm.addView(mNightLayout, windowParams);
	}
	
	
	private void removeNightView()
	{
		if (mNightLayout != null)
		{
			((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(mNightLayout);
			mNightLayout = null;
		}
	}
	
	
	private void setStartAlarm(Calendar pickCal)
	{
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(mContext, OffBroadCast.class);
		intent.putExtra("onoff", "on");
		PendingIntent pender = PendingIntent.getBroadcast(mContext, ALARM_REQUEST_CODE, intent, 0);
		alarm.cancel(pender);
		
		long atime = Calendar.getInstance().getTimeInMillis();
		long btime = pickCal.getTimeInMillis();
		long triggerTime = btime;
		if (atime > btime)
			triggerTime += 1000 * 60 * 60 * 24;
		
		alarm.setRepeating(AlarmManager.RTC, triggerTime, 24 * 60 * 60 * 1000, pender);
	}
	
	
	private void setEndAlarm(Calendar pickCal)
	{
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(mContext, OffBroadCast.class);
		intent.putExtra("onoff", "off");
		PendingIntent pender = PendingIntent.getBroadcast(mContext, ALARM_OFF_CODE, intent, 0);
		alarm.cancel(pender);
		
		long atime = Calendar.getInstance().getTimeInMillis();
		long btime = pickCal.getTimeInMillis();
		long triggerTime = btime;
		if (atime > btime)
			triggerTime += 1000 * 60 * 60 * 24;
		
		alarm.setRepeating(AlarmManager.RTC, triggerTime, 24 * 60 * 60 * 1000, pender);
	}
	
	
	private void showTimePickDialog(final String timeType, final Button btnView)
	{
		final Calendar selectedCalendar = Calendar.getInstance();
		try
		{
			SimpleDateFormat sdFormat = new SimpleDateFormat("HH:mm", Locale.KOREA);
			Date tempDate = sdFormat.parse(btnView.getText().toString());
			selectedCalendar.setTime(tempDate);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		
		View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_set_test_time, null);
		final TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker_test);
		timePicker.setIs24HourView(true);
		if (Build.VERSION.SDK_INT > 23)
		{
			timePicker.setHour(selectedCalendar.get(Calendar.HOUR_OF_DAY));
			timePicker.setMinute(selectedCalendar.get(Calendar.MINUTE));
		}
		else
		{
			timePicker.setCurrentHour(selectedCalendar.get(Calendar.HOUR_OF_DAY));
			timePicker.setCurrentMinute(selectedCalendar.get(Calendar.MINUTE));
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setView(view);
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				int hour, min;
				if (Build.VERSION.SDK_INT > 23)
				{
					hour = timePicker.getHour();
					min = timePicker.getMinute();
				}
				else
				{
					hour = timePicker.getCurrentHour();
					min = timePicker.getCurrentMinute();
				}
				String pick_time = String.format("%d:%d", hour, min);
				btnView.setText(pick_time);
				Calendar newCal = Calendar.getInstance();
				newCal.set(Calendar.HOUR_OF_DAY, hour);
				newCal.set(Calendar.MINUTE, min);
				newCal.set(Calendar.SECOND, 0);
				newCal.set(Calendar.MILLISECOND, 0);
				
				if (timeType.equals(FLAG_TIME_START))
				{
					PreferenceUtil.setStartTime(pick_time, mContext);
					if (mSwitchScedule.isChecked())
						setStartAlarm(newCal);
				}
				else if (timeType.equals(FLAG_TIME_END))
				{
					PreferenceUtil.setEndTime(pick_time, mContext);
					if (mSwitchScedule.isChecked())
						setEndAlarm(newCal);
				}
			}
		});
		builder.show();
	}
	
	private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener()
	{
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
		{
//				Log.i("seekbar value", "progress : " + progress);
			PreferenceUtil.setSeekValue(progress, mContext);
			if (mBound)
				mService.setNightValue(progress);
			else
			{
				if (mNightLayout == null)
					addNightView();
				if (mNightLayout != null)
					mNightLayout.setBackgroundColor(Color.parseColor(ColorUtil.getColorValue(progress)));
			}
		}
		
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar)
		{
		}
		
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar)
		{
			if (!mBound)
				removeNightView();
		}
	};
}
