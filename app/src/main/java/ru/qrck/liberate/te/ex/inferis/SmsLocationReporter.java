/*
 * Copyright (c) 2014, Sergey Parshin, quarck@gmail.com
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of developer (Sergey Parshin) nor the
 *       names of other project contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package ru.qrck.liberate.te.ex.inferis;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class SmsLocationReporter implements LocationListener 
{
	private String requestedByAddr; // address of the remote end who rqeuested location

	private LocationManager locationManager = null;
	
	private long lastPositionUpdate = 0;
	private float lastAccuracy = 100000.0f;
	
	private boolean isGpsEnabled = false;
	private boolean isNetworkEnabled = false;
	
	private int totalSentMsgs = 0;
	
	public SmsLocationReporter(String remoteAddr)
	{
		requestedByAddr = remoteAddr;
	}
	
	public void Start(Context context)
	{
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	
		isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		lastPositionUpdate = System.currentTimeMillis(); // a bit of a hack to make sender don't sent within first 20 seconds until it receives good location
		
		if (isGpsEnabled)
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000L, 10.0f, this);
		
		if (isNetworkEnabled)
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000L, 10.0f, this);

		if (!isGpsEnabled && !isNetworkEnabled)
			SmsUtil.send(requestedByAddr, "Sorry, all network location providers are disabled!");
	}

	@Override
	public void onLocationChanged(Location location) 
	{
		float accuracy = location.getAccuracy();

		String provider = location.getProvider();	
		
		boolean gotAccurateLocation = accuracy < 10.0;
		
		long now = System.currentTimeMillis();
		
		boolean sendUpdate = false;
		boolean terminate = false;
		
		if (!isGpsEnabled || gotAccurateLocation)  // if gps disabled or if got very accurate GPS fix - send straight away and disable
		{
			sendUpdate = true;
			terminate = true;
		}
		else if ( now - lastPositionUpdate > 20000L && accuracy < 0.5f * lastAccuracy) // if we've got significantly improved fix since the last update sent 20 secs ago - send it... 
		{
			sendUpdate = true;
		}
		else if ( now - lastPositionUpdate > 360000L ) // no position improvements in the last 5 minutes? terminate
		{
			terminate = false;
		}

		if (sendUpdate)
		{  
            StringBuilder sb = new StringBuilder();
            
    		boolean isGpsLocation = (provider == LocationManager.GPS_PROVIDER);
            sb.append(isGpsLocation ? "GPS" : "Network");
            
            sb.append(" Location: Lat: ");
            sb.append(location.getLatitude());
            sb.append(", Long: ");
            sb.append(location.getLongitude());
            sb.append(", Alt: ");
            sb.append(location.getAltitude());
            
            double speed = location.getSpeed();
            if (speed > 10.0f)
            {
            	sb.append(", Speed: ");
            	sb.append(speed);
            }
 
            sb.append(", Accuracy: ");
            sb.append(accuracy);
            
            if (!isGpsEnabled)
            	sb.append(" [GPS is disabled]");
 
           	SmsUtil.send(requestedByAddr, sb.toString());
           	totalSentMsgs ++;
 
    		lastAccuracy = accuracy;
			lastPositionUpdate = now;			
		}

		if (terminate || totalSentMsgs >= 5)
		{
			locationManager.removeUpdates(this);
			locationManager = null;
		}			
	}

	@Override
	public void onProviderDisabled(String arg0) 
	{
	}

	@Override
	public void onProviderEnabled(String arg0) 
	{
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) 
	{
	}
}
