package com.hankora817.nightshift.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivty extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				Intent intent = new Intent(SplashActivty.this, MainActivity.class);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				finish();
			}
		}, 2000);
	}
}
