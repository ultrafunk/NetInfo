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

package com.ultrafunk.network_info.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;

import com.ultrafunk.network_info.Constants;

class MobileDataStateListener extends PhoneStateListener
{
	private final Context context;

	MobileDataStateListener(Context context)
	{
		this.context = context;
	}

	@Override
	public void onDataConnectionStateChanged(int state)
	{
		switch (state)
		{
			case TelephonyManager.DATA_CONNECTED:
			case TelephonyManager.DATA_DISCONNECTED:
				LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.ACTION_DATA_CONNECTION_CHANGED));
				break;
		}
	}
	
	@Override
	public void onServiceStateChanged(ServiceState serviceState)
	{
		switch (serviceState.getState())
		{
			case ServiceState.STATE_EMERGENCY_ONLY:
			case ServiceState.STATE_OUT_OF_SERVICE:
				NetworkStateService.setMobileOutOfService(true);
				LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.ACTION_SERVICE_STATE_CHANGED));
				break;

			default:
				{
					NetworkStateService.setMobileOutOfService(false);

					// If the device is network roaming but mobile data roaming is disabled, this
					// broadcast is necessary to properly update the widget on service state changes.
					if ((serviceState.getState() == ServiceState.STATE_IN_SERVICE) && serviceState.getRoaming())
						LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.ACTION_SERVICE_STATE_CHANGED));
				}
				break;
		}
	}
}
