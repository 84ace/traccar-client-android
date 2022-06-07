/*
 * Copyright 2015 - 2021 Anton Tananaev (anton@traccar.org)
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
@file:Suppress("DEPRECATION")
package org.traccar.client

import android.os.AsyncTask
import android.util.Log
import java.io.IOException
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.Socket
import java.util.stream.Collectors
import java.util.zip.Deflater
import org.json.JSONObject
import org.json.JSONException

fun compress(payload: String): ByteArray {
    val deflater = Deflater()
    val b = payload.toByteArray(Charsets.UTF_8)
    deflater.setInput(b)
    val outputStream = ByteArrayOutputStream(b.size)
    deflater.finish()
    val buffer = ByteArray(1024)
    while (!deflater.finished()) {
        var count = deflater.deflate(buffer)
        outputStream.write(buffer, 0, count)
    }
    outputStream.close()
    return outputStream.toByteArray()
}

object RequestManager {

    private const val TIMEOUT = 15 * 1000

    fun sendRequestIMS(uri: String, payload: String): Boolean {
        val compressed = compress(payload)

        var url: URL
        try {
            url = URL(uri)
        } 
        catch (e: Exception) {
            url = URL("http://"+uri)
        }

        val host = url.getHost()
        val port = url.getPort()

        var socket: Socket
        try {
            socket = Socket(host, port)
        }
        catch (e: Exception) {
            return false
        }
        try {
        val dataInputStream = DataInputStream(socket.getInputStream())
        val dataOutputStream = DataOutputStream(socket.getOutputStream())
            try {
                dataOutputStream.write(compressed)
                dataOutputStream.flush()
                // Read response
                val readStream = BufferedReader(InputStreamReader(socket.getInputStream()));
                val startTime = System.currentTimeMillis();
                while (true) {
                    if ((System.currentTimeMillis() - startTime) > 10*1000) {
                        return false
                    }
                    if (readStream.ready()) {
                        val response = readStream.lines().collect(Collectors.joining());
                        readStream.close();
                        try {
                            val jsonResponse = JSONObject(response);
                            val success = jsonResponse.getBoolean("success")
                            if (success) {
                                return true
                            }
                        } catch (e: JSONException) {
                            return false
                        }
                        return false
                    }
                }
            }
            finally {
                dataInputStream.close()
                dataOutputStream.close()
            }
        } finally {
            socket.close()
        }
    }

    fun sendRequest(request: String?): Boolean {
        var inputStream: InputStream? = null
        return try {
            val url = URL(request)
            val connection = url.openConnection() as HttpURLConnection
            connection.readTimeout = TIMEOUT
            connection.connectTimeout = TIMEOUT
            connection.requestMethod = "POST"
            connection.connect()
            inputStream = connection.inputStream
            while (inputStream.read() != -1) {}
            true
        } catch (error: IOException) {
            false
        } finally {
            try {
                inputStream?.close()
            } catch (secondError: IOException) {
                Log.w(RequestManager::class.java.simpleName, secondError)
            }
        }
    }

    fun sendRequestAsync(request: String, handler: RequestHandler) {
        RequestAsyncTask(handler).execute(request)
    }

    fun sendRequestIMSAsync(uri: String, payload: String, handler: RequestHandler) {
        RequestAsyncIMSTask(handler).execute(uri, payload)
    }

    interface RequestHandler {
        fun onComplete(success: Boolean)
    }

    private class RequestAsyncTask(private val handler: RequestHandler) : AsyncTask<String, Unit, Boolean>() {

        override fun doInBackground(vararg request: String): Boolean {
            return sendRequest(request[0])
        }

        override fun onPostExecute(result: Boolean) {
            handler.onComplete(result)
        }
    }

    private class RequestAsyncIMSTask(private val handler: RequestHandler) : AsyncTask<String, Unit, Boolean>() {

        override fun doInBackground(vararg vargs: String): Boolean {
            return sendRequestIMS(vargs[0], vargs[1])
        }

        override fun onPostExecute(result: Boolean) {
            handler.onComplete(result)
        }
    }
}
