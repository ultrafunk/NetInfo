/*
 * Copyright 2014 ultrafunk.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ultrafunk.network_info.receiver;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import com.ultrafunk.network_info.Constants;
import com.ultrafunk.network_info.R;
import com.ultrafunk.network_info.service.NetworkStateService;

public class WifiOnOffReceiver extends WidgetBroadcastReceiver
{
	private boolean turningOn;

	@Override
	public void onReceive(Context context, Intent intent)
	{
	//	Log.e(this.getClass().getSimpleName(), "onReceive(): " + intent.getAction());

		updateWifiViews = true;

		if (Constants.ONCLICK_WIFI_ONOFF.equals(intent.getAction()))
			turnOnOff(context);
	}

	private void turnOnOff(Context context)
	{
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

		if (wifiManager.isWifiEnabled())
		{
			turningOn = false;
			partiallyUpdateWidgets(context);
			wifiManager.setWifiEnabled(false);
		}
		else
		{
			turningOn = true;
			partiallyUpdateWidgets(context);
			wifiManager.setWifiEnabled(true);

			Intent serviceIntent = new Intent(context, NetworkStateService.class);
			serviceIntent.setAction(Constants.ACTION_WIFI_CONNECTING);
			context.startService(serviceIntent);
		}
	}

	@Override
	protected void updateView(Context context, RemoteViews remoteViews, Bundle widgetOptions)
	{
		remoteViews.setViewVisibility(R.id.wifiDetailsTextView, View.GONE);
		remoteViews.setViewVisibility(R.id.wifiNameTextView, View.VISIBLE);
		remoteViews.setTextViewText(R.id.wifiNameTextView, turningOn ? context.getString(R.string.turning_on) : context.getString(R.string.turning_off));
	}
}
