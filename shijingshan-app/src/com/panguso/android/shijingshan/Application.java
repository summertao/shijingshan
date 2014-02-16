package com.panguso.android.shijingshan;

import java.util.UUID;

import com.panguso.android.shijingshan.column.ColumnPage;

import android.content.Context;
import android.content.res.Resources;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

public class Application extends android.app.Application {
	/** The device's UUID. */
	private String mUUID;

	/**
	 * Get the device's UUID.
	 * 
	 * @see http://www.cnblogs.com/xiaowenji/archive/2011/01/11/1933087.html
	 * @return The device's UUID.
	 * @author Luo Yinzhuo
	 */
	public String getUUID() {
		if (mUUID == null) {
			final TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			final String tmDevice = "" + tm.getDeviceId();
			final String tmSerial = "" + tm.getSimSerialNumber();
			final String androidId = "" + Secure.getString(getContentResolver(), Secure.ANDROID_ID);
			UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32)
			        | tmSerial.hashCode());
			mUUID = deviceUuid.toString();
		}
		return mUUID;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Resources resources = getResources();
		/** Initialize {@link ColumnPage}'s parameters. */
		ColumnPage.initialize(resources.getDimension(R.dimen.column_offset_left),
		        resources.getDimension(R.dimen.column_offset_top),
		        resources.getDimension(R.dimen.column_margin),
		        resources.getDimension(R.dimen.column_size));
	}
}
