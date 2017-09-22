package com.hankora817.nightshift.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OffBroadCast extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String onoff = intent.getStringExtra("onoff");
		if (onoff.equals("on"))
			context.startService(new Intent(context, TopService.class));
		else if (onoff.equals("off"))
			context.stopService(new Intent(context, TopService.class));
	}
}
