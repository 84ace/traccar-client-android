/*
 * Copyright 2012 - 2021 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.client

import android.content.Context
import android.content.pm.PackageManager
import android.telephony.TelephonyManager
import android.view.View
import android.net.Uri
import android.provider.Settings.Secure
import org.json.JSONObject
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.Locale
import java.util.UUID
import cesarferreira.androiduniquedeviceid.UniqueDeviceIdProvider

object ProtocolFormatter {

    fun formatDateToISO8601String(date: Date): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"))
        return formatter.format(date)
    }

    fun getDeviceName(): String {
        val manufacturer = android.os.Build.MANUFACTURER
        val model = android.os.Build.MODEL
        if (model.startsWith(manufacturer)) {
            return model
        }
        return manufacturer + " " + model
    }

    fun getUDID(context: Context): String {
        val idProvider = UniqueDeviceIdProvider(context) 
        return idProvider.getUniqueId()
    }

    fun formatRequestIMSMonitor(position: Position, context: Context): String {
        // Compression zip and zlib 0
        // JSON object matching IMS
        val payload = JSONObject()
        payload.put("type", "stats")

        val udid = ProtocolFormatter.getUDID(context)
        val metaData = JSONObject()
        metaData.put("person_name", position.deviceId)
        metaData.put("person_key", udid+"-person")
        metaData.put("device_name", ProtocolFormatter.getDeviceName())
        metaData.put("device_key", udid+"-device")
        payload.put("metadata", metaData)

        val locationStat = JSONObject()
        val loc = JSONArray()
        // [lon, lat]
        loc.put(position.longitude)
        loc.put(position.latitude)
        locationStat.put("location", loc)

        val timestamp = ProtocolFormatter.formatDateToISO8601String(position.time)
        locationStat.put("timestamp", timestamp)
        locationStat.put("speed", position.speed)
        locationStat.put("altitude", position.altitude)

        val stats = JSONObject()
        val locationStats = JSONArray()
        locationStats.put(locationStat)
        stats.put("location_stats", locationStats)

        payload.put("body", stats)

        return payload.toString()
    }

    fun formatRequest(url: String, position: Position, alarm: String? = null): String {
        val serverUrl = Uri.parse(url)
        val builder = serverUrl.buildUpon()
            .appendQueryParameter("id", position.deviceId)
            .appendQueryParameter("timestamp", (position.time.time / 1000).toString())
            .appendQueryParameter("lat", position.latitude.toString())
            .appendQueryParameter("lon", position.longitude.toString())
            .appendQueryParameter("speed", position.speed.toString())
            .appendQueryParameter("bearing", position.course.toString())
            .appendQueryParameter("altitude", position.altitude.toString())
            .appendQueryParameter("accuracy", position.accuracy.toString())
            .appendQueryParameter("batt", position.battery.toString())
        if (position.mock) {
            builder.appendQueryParameter("mock", position.mock.toString())
        }
        if (alarm != null) {
            builder.appendQueryParameter("alarm", alarm)
        }
        return builder.build().toString()
    }
}
