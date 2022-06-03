package org.traccar.client

import android.location.Location
import android.os.Build
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.traccar.client.ProtocolFormatter.formatRequest
import org.traccar.client.ProtocolFormatter.formatRequestIMSMonitor
import org.traccar.client.ProtocolFormatter.formatDateToISO8601String
import org.traccar.client.ProtocolFormatter.getUDID

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class ProtocolFormatterTest {

    @Test
    fun testFormatRequest() {
        val position = Position("123456789012345", Location("gps"), 0.0)
        val url = formatRequest("http://localhost:5055", position)
        Assert.assertEquals("http://localhost:5055?id=123456789012345&timestamp=0&lat=0.0&lon=0.0&speed=0.0&bearing=0.0&altitude=0.0&accuracy=0.0&batt=0.0", url)
    }

    @Test
    fun testFormatPathPortRequest() {
        val position = Position("123456789012345", Location("gps"), 0.0)
        val url = formatRequest("http://localhost:8888/path", position)
        Assert.assertEquals("http://localhost:8888/path?id=123456789012345&timestamp=0&lat=0.0&lon=0.0&speed=0.0&bearing=0.0&altitude=0.0&accuracy=0.0&batt=0.0", url)
    }

    @Test
    fun testFormatAlarmRequest() {
        val position = Position("123456789012345", Location("gps"), 0.0)
        val url = formatRequest("http://localhost:5055/path", position, "alert message")
        Assert.assertEquals("http://localhost:5055/path?id=123456789012345&timestamp=0&lat=0.0&lon=0.0&speed=0.0&bearing=0.0&altitude=0.0&accuracy=0.0&batt=0.0&alarm=alert%20message", url)
    }

    @Test
    fun testformatRequestIMSMonitor() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val position = Position("123456789012345", Location("gps"), 0.0)
        val expectedTimestamp = formatDateToISO8601String(position.time)
        val expectedUDID = getUDID(context)
        val payload = formatRequestIMSMonitor(position, context)
        Assert.assertEquals(
            "{\"type\":\"stats\",\"metadata\":{\"person_name\":\"123456789012345\",\"person_key\":\""+expectedUDID+"-person\",\"device_name\":\"unknown robolectric\",\"device_key\":\""+expectedUDID+"-device\"},\"body\":{\"location_stats\":[{\"location\":[0,0],\"timestamp\":\""+expectedTimestamp+"\",\"speed\":0,\"altitude\":0}]}}",
            payload
        )
    }
}
