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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.RemoteViews;

import com.ultrafunk.network_info.Constants;
import com.ultrafunk.network_info.R;
import com.ultrafunk.network_info.service.NetworkStateService;

public class MobileDataStatusReceiver extends WidgetBroadcastReceiver
{
	private TelephonyManager telephonyManager = null;
	private int dataState = -1;
	private boolean isMobileDataEnabled = false;
	private boolean isAirplaneModeOn = false;
	private boolean isMobileOutOfService = false;
	private boolean isDataRoaming = false;
	private String networkOperatorAndServiceProvider = null;
	private long dataUsageBytes = 0;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		final String action = intent.getAction();

	//	Log.e(this.getClass().getSimpleName(), "onReceive(): " + action);

		updateMobileDataViews = true;

		telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		dataState = telephonyManager.getDataState();
		isMobileDataEnabled = MobileDataUtils.isMobileDataEnabled(context);
		isAirplaneModeOn = MobileDataUtils.isAirplaneModeOn(context);
		isMobileOutOfService = NetworkStateService.isMobileOutOfService();
		isDataRoaming = isDataRoaming(context);
		networkOperatorAndServiceProvider = getNetworkOperatorAndServiceProvider(context);
		dataUsageBytes = NetworkStateService.setGetDataUsageBytes();

		if (Constants.ACTION_DATA_CONNECTION_CHANGED.equals(action) ||
			Constants.ACTION_DATA_STATE_CHANGED.equals(action) ||
			Constants.ACTION_SERVICE_STATE_CHANGED.equals(action))
		{
			// Needed to get around a known bug in Android 5.x: https://code.google.com/p/android/issues/detail?id=78924
			if ((dataState == TelephonyManager.DATA_CONNECTED) && (dataUsageBytes == 0) && !NetworkStateService.isWaitingForDataUsage())
			{
				NetworkStateService.setWaitingForDataUsage(true);
				Intent serviceIntent = new Intent(context, NetworkStateService.class);
				serviceIntent.setAction(Constants.ACTION_DATA_CONNECTED);
				context.startService(serviceIntent);
			}

			partiallyUpdateWidgets(context);
		}
		else if (Constants.ACTION_DATA_USAGE_UPDATE.equals(action) || Intent.ACTION_SCREEN_ON.equals(action))
		{
			partiallyUpdateWidgets(context);
		}
		else if (Constants.ACTION_UPDATE_WIDGET.equals(action))
		{
			partiallyUpdateWidget(context, AppWidgetManager.getInstance(context), intent.getIntExtra(Constants.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID));
		}
	}

	private boolean isDataRoaming(Context context)
	{
		if ((dataState == TelephonyManager.DATA_CONNECTED) && telephonyManager.isNetworkRoaming())
		{
			ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

			return (networkInfo != null) ? networkInfo.isRoaming() : false;
		}
		else
		{
			return telephonyManager.isNetworkRoaming() && MobileDataUtils.isDataRoaming(context);
		}
	}

	private String getNetworkOperatorAndServiceProvider(Context context)
	{
		String networkOperatorName = telephonyManager.getNetworkOperatorName();

		if (networkOperatorName.isEmpty())
			networkOperatorName = context.getString(R.string.not_available);

		if (isDataRoaming)
		{
			String simOperatorName = telephonyManager.getSimOperatorName();
			String networkOperator = telephonyManager.getNetworkOperator();
			String simOperator = telephonyManager.getSimOperator();

			if (!simOperatorName.isEmpty() && !networkOperator.equalsIgnoreCase(simOperator))
				return networkOperatorName + " - " + simOperatorName;
		}

		return networkOperatorName;
	}

	private void setStateColor(Context context, RemoteViews remoteViews, int state)
	{
		int color = (state == STATE_ON) ? ContextCompat.getColor(context, android.R.color.white) : ContextCompat.getColor(context, R.color.medium_gray);
		remoteViews.setTextColor(R.id.mobileNameTextView, color);
		remoteViews.setInt(R.id.mobileHeaderSpacerTextView, "setBackgroundColor", color);
		remoteViews.setTextColor(R.id.mobileInfoTopTextView, color);
		remoteViews.setTextColor(R.id.mobileInfoBottomTextView, color);
	}

	@Override
	protected void updateView(Context context, RemoteViews remoteViews, Bundle widgetOptions)
	{
		if (isMobileOutOfService)
		{
			setStateColor(context, remoteViews, isMobileDataEnabled ? STATE_ON : STATE_OFF);
			remoteViews.setTextViewText(R.id.mobileNameTextView, context.getString(R.string.mobile_data));
			remoteViews.setImageViewResource(R.id.mobileStateImageView, isMobileDataEnabled ? R.drawable.ic_signal_cellular_disabled : R.drawable.ic_signal_cellular_disabled_off);
			remoteViews.setViewVisibility(R.id.mobileInfoTopTextView, View.VISIBLE);
			remoteViews.setTextViewText(R.id.mobileInfoTopTextView, context.getString(R.string.no_service));
			remoteViews.setViewVisibility(R.id.mobileInfoBottomTextView, View.GONE);
			return;
		}

		if (dataState == TelephonyManager.DATA_DISCONNECTED)
		{
			remoteViews.setViewVisibility(R.id.mobileInfoTopTextView, View.VISIBLE);
			remoteViews.setViewVisibility(R.id.mobileInfoBottomTextView, View.VISIBLE);

			if (isMobileDataEnabled && !isAirplaneModeOn)
			{
				setStateColor(context, remoteViews, STATE_ON);
				remoteViews.setTextViewText(R.id.mobileNameTextView, networkOperatorAndServiceProvider);
				remoteViews.setImageViewResource(R.id.mobileStateImageView, R.drawable.ic_signal_cellular_enabled);
				remoteViews.setTextViewText(R.id.mobileInfoTopTextView, String.format("%s (%s)", context.getString(R.string.not_connected), MobileDataUtils.getNetworkTypeString(telephonyManager.getNetworkType(), true)));
				remoteViews.setTextColor(R.id.mobileInfoBottomTextView, ContextCompat.getColor(context, R.color.medium_gray));
				remoteViews.setTextViewText(R.id.mobileInfoBottomTextView, MobileDataUtils.getDataUsageString(context, NetworkStateService.getDataUsageBytes()));
			}
			else
			{
				setStateColor(context, remoteViews, STATE_OFF);
				remoteViews.setTextViewText(R.id.mobileNameTextView, isAirplaneModeOn ? context.getString(R.string.mobile_data) : networkOperatorAndServiceProvider);

				if (isAirplaneModeOn)
				{
					remoteViews.setTextViewText(R.id.mobileInfoTopTextView, context.getString(R.string.airplane_mode));
					remoteViews.setViewVisibility(R.id.mobileInfoBottomTextView, View.GONE);
					remoteViews.setImageViewResource(R.id.mobileStateImageView, R.drawable.ic_signal_cellular_disabled_off);
				}
				else
				{
					remoteViews.setTextViewText(R.id.mobileInfoTopTextView, MobileDataUtils.getNetworkTypeString(telephonyManager.getNetworkType(), false) + (isDataRoaming ? " - " + context.getString(R.string.roaming) : ""));
					remoteViews.setTextViewText(R.id.mobileInfoBottomTextView, MobileDataUtils.getDataUsageString(context, NetworkStateService.getDataUsageBytes()));
					remoteViews.setImageViewResource(R.id.mobileStateImageView, R.drawable.ic_signal_cellular_enabled_off);
				}
			}
		}
		else
		{
			setStateColor(context, remoteViews, STATE_ON);
			remoteViews.setTextViewText(R.id.mobileNameTextView, networkOperatorAndServiceProvider);
			remoteViews.setImageViewResource(R.id.mobileStateImageView, R.drawable.ic_signal_cellular_on);
			remoteViews.setViewVisibility(R.id.mobileInfoTopTextView, View.VISIBLE);
			remoteViews.setTextViewText(R.id.mobileInfoTopTextView, MobileDataUtils.getNetworkTypeString(telephonyManager.getNetworkType(), false) + (isDataRoaming ? " - " + context.getString(R.string.roaming) : ""));
			remoteViews.setViewVisibility(R.id.mobileInfoBottomTextView, View.VISIBLE);

			boolean isConnecting = ((dataState == TelephonyManager.DATA_CONNECTING) || NetworkStateService.isWaitingForDataUsage());
			remoteViews.setTextViewText(R.id.mobileInfoBottomTextView, isConnecting ? context.getString(R.string.connecting) : MobileDataUtils.getDataUsageString(context, dataUsageBytes));
		}
	}
}
