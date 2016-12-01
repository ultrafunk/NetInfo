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

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.ultrafunk.network_info.Constants;
import com.ultrafunk.network_info.R;
import com.ultrafunk.network_info.service.NetworkStateService;

public class WifiStatusReceiver extends WidgetBroadcastReceiver
{
	private WifiManager wifiManager = null;
	private int wifiState = -1;
	private WifiInfo wifiInfo = null;
	private String detailsString = "";

	@Override
	public void onReceive(Context context, Intent intent)
	{
		final String action = intent.getAction();

	//	Log.e(this.getClass().getSimpleName(), "onReceive(): " + action);

		updateWifiViews = true;

		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		wifiState = wifiManager.getWifiState();
		wifiInfo = wifiManager.getConnectionInfo();

		if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action))
		{
			if (isConnectionReady(intent))
			{
				String securityString = WifiUtils.getSecurityString(context, wifiManager, wifiInfo.getBSSID());
				NetworkStateService.setWifiSecurityString(securityString);
				detailsString = context.getString(R.string.security) + securityString;

				Intent serviceIntent = new Intent(context, NetworkStateService.class);
				serviceIntent.setAction(Constants.ACTION_WIFI_CONNECTED);
				context.startService(serviceIntent);
			}

			partiallyUpdateWidgets(context);
		}
		else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action) ||
				 Constants.ACTION_WIFI_SCANNING.equals(action))
		{
			partiallyUpdateWidgets(context);
		}
		else if (Intent.ACTION_SCREEN_ON.equals(action) ||
				 Constants.ACTION_WIFI_LINK_SPEED.equals(action))
		{
			if (isConnected())
			{
				setDetailsString(context);
				partiallyUpdateWidgets(context);
			}
		}
		else if (Constants.ACTION_UPDATE_WIDGET.equals(action))
		{
			if (isConnected())
				setDetailsString(context);

			partiallyUpdateWidget(context, AppWidgetManager.getInstance(context), intent.getIntExtra(Constants.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID));
		}
	}

	private boolean isConnected()
	{
		return ((wifiState == WifiManager.WIFI_STATE_ENABLED) && (wifiInfo.getIpAddress() != 0));
	}

	private boolean isConnectionReady(Intent intent)
	{
		if (isConnected() && (intent.getExtras() != null))
		{
			NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

			if ((networkInfo != null) && (networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED))
				return true;
		}

		return false;
	}

	private void setDetailsString(Context context)
	{
		String securityString = NetworkStateService.getWifiSecurityString();

		if (securityString == null)
			securityString = WifiUtils.getSecurityString(context, wifiManager, wifiInfo.getBSSID());

		if (wifiInfo.getLinkSpeed() != -1)
			detailsString = String.format("%s - %d %s", securityString, wifiInfo.getLinkSpeed(), WifiInfo.LINK_SPEED_UNITS);
		else
			detailsString = context.getString(R.string.security) + securityString;
	}

	private void setStateColor(Context context, RemoteViews remoteViews, int state)
	{
		int color = (state == STATE_ON) ? ContextCompat.getColor(context, android.R.color.white) : ContextCompat.getColor(context, R.color.medium_gray);
		remoteViews.setTextColor(R.id.wifiNameTextView, color);
		remoteViews.setInt(R.id.wifiHeaderSpacerTextView, "setBackgroundColor", color);
		remoteViews.setTextColor(R.id.wifiInfoTopTextView, color);
		remoteViews.setTextColor(R.id.wifiInfoBottomTextView, color);
	}

	@Override
	protected void updateView(Context context, RemoteViews remoteViews, Bundle widgetOptions)
	{
		if ((wifiState == WifiManager.WIFI_STATE_DISABLED) || (wifiState == WifiManager.WIFI_STATE_UNKNOWN))
		{
			setStateColor(context, remoteViews, STATE_OFF);
			remoteViews.setTextViewText(R.id.wifiNameTextView, context.getString(R.string.wifi));
			remoteViews.setImageViewResource(R.id.wifiStateImageView, R.drawable.ic_signal_wifi_off);
			remoteViews.setViewVisibility(R.id.wifiInfoTopTextView, View.VISIBLE);
			remoteViews.setTextViewText(R.id.wifiInfoTopTextView, context.getString(R.string.tap_to_change));
			remoteViews.setViewVisibility(R.id.wifiInfoBottomTextView, View.GONE);
		}
		else if (wifiState == WifiManager.WIFI_STATE_ENABLED)
		{
			setStateColor(context, remoteViews, STATE_ON);
			remoteViews.setViewVisibility(R.id.wifiInfoTopTextView, View.VISIBLE);

			if (wifiInfo.getIpAddress() != 0)
			{
				remoteViews.setImageViewResource(R.id.wifiStateImageView, R.drawable.ic_signal_wifi_on);
				remoteViews.setTextViewText(R.id.wifiNameTextView, wifiInfo.getSSID().replace("\"", ""));

				if(!detailsString.isEmpty())
				{
					remoteViews.setTextViewText(R.id.wifiInfoTopTextView, detailsString);
					remoteViews.setViewVisibility(R.id.wifiInfoBottomTextView, View.VISIBLE);

					String bottomText = "";

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
						bottomText = String.format("%s - %s", WifiUtils.getIpAddressString(wifiInfo.getIpAddress()), WifiUtils.getWifiFrequencyString(context, wifiInfo.getFrequency()));
					else
						bottomText = String.format("IP: %s", WifiUtils.getIpAddressString(wifiInfo.getIpAddress()));

					remoteViews.setTextViewText(R.id.wifiInfoBottomTextView, bottomText);
				}
			}
			else
			{
				remoteViews.setImageViewResource(R.id.wifiStateImageView, R.drawable.ic_signal_wifi_enabled);
				remoteViews.setViewVisibility(R.id.wifiInfoBottomTextView, View.GONE);

				switch (wifiInfo.getSupplicantState())
				{
					case DORMANT:
					case INTERFACE_DISABLED:
						remoteViews.setTextViewText(R.id.wifiInfoTopTextView, context.getString(R.string.disconnecting));
						break;

					case SCANNING:
						remoteViews.setTextViewText(R.id.wifiInfoTopTextView, context.getString(R.string.scanning));
						break;

					case DISCONNECTED:
					case INVALID:
					case UNINITIALIZED:
					case INACTIVE:
						remoteViews.setTextViewText(R.id.wifiNameTextView, context.getString(R.string.wifi));
						remoteViews.setTextViewText(R.id.wifiInfoTopTextView, context.getString(R.string.no_network));
						break;

					default:
						remoteViews.setTextViewText(R.id.wifiInfoTopTextView, context.getString(R.string.connecting));
						break;
				}
			}
		}
	}
}
