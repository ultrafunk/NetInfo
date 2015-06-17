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

package com.ultrafunk.network_info.config;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.ultrafunk.network_info.R;

import java.util.ArrayList;

public class SettingsScreenDialogFragment extends DialogFragment
{
	public interface SettingsScreenDialogListener
	{
		void onDialogSelectionChanged(int selected);
	}

	SettingsScreenDialogListener mListener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());

		dialog.setTitle(getString(R.string.settings_screen));
		dialog.setPositiveButton(getString(android.R.string.cancel), new PositiveButtonClickListener());

		ArrayList<String> arrayList = new ArrayList<>();
		arrayList.add(getString(R.string.mobile_network_settings));
		arrayList.add(getString(R.string.data_usage));

		dialog.setSingleChoiceItems(arrayList.toArray(new CharSequence[arrayList.size()]), WidgetConfig.MOBILE_DATA_SETTINGS_MOBILE_NETWORK_SETTINGS, selectItemListener);

		return dialog.create();
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		mListener = (SettingsScreenDialogListener) activity;
	}

	class PositiveButtonClickListener implements DialogInterface.OnClickListener
	{
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			dialog.dismiss();
		}
	}

	DialogInterface.OnClickListener selectItemListener = new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			mListener.onDialogSelectionChanged(which);
			dialog.dismiss();
		}
	};
}