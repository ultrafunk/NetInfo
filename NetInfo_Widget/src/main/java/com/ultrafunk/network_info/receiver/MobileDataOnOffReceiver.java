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
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.ultrafunk.network_info.Constants;
import com.ultrafunk.network_info.R;
import com.ultrafunk.network_info.util.Utils;
import com.ultrafunk.network_info.service.NetworkStateService;

public class MobileDataOnOffReceiver extends WidgetBroadcastReceiver
{
	private boolean turningOn;

	@Override
	public void onReceive(Context context, Intent intent)
	{
	//	Log.e(this.getClass().getSimpleName(), "onReceive(): " + intent.getAction());

		updateMobileDataViews = true;

		if (Constants.ONCLICK_MOBILE_DATA_ONOFF.equals(intent.getAction()))
			turnOnOff(context);
	}

	private void turnOnOff(Context context)
	{
		boolean isMobileDataEnabled = MobileDataUtils.getMobileDataEnabled(context);
		boolean isAirplaneModeOn = MobileDataUtils.getAirplaneModeOn(context);
		boolean isOutOfService = NetworkStateService.isMobileOutOfService();

		if (isAirplaneModeOn)
		{
			Toast.makeText(context, context.getString(R.string.error_mobile_data_on_flight_mode), Toast.LENGTH_LONG).show();
		}
		else
		{
			boolean isWifiConnected = Utils.isWifiConnected(context);

			if (isMobileDataEnabled)
			{
				if (!isWifiConnected && !isOutOfService)
				{
					turningOn = false;
					partiallyUpdateWidgets(context);
				}

				MobileDataUtils.setMobileDataEnabled(context, false);
			}
			else
			{
				if (!isWifiConnected && !isOutOfService)
				{
					turningOn = true;
					partiallyUpdateWidgets(context);
				}

				MobileDataUtils.setMobileDataEnabled(context, true);
			}
		}
	}

	@Override
	protected void updateView(Context context, RemoteViews remoteViews, Bundle widgetOptions)
	{
		remoteViews.setViewVisibility(R.id.mobileDetailsTextView, View.GONE);
		remoteViews.setViewVisibility(R.id.mobileNameTextView, View.VISIBLE);
		remoteViews.setTextViewText(R.id.mobileNameTextView, turningOn ? context.getString(R.string.turning_on) : context.getString(R.string.turning_off));
	}
}
