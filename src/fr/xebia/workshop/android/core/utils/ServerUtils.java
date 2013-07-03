/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.xebia.workshop.android.core.utils;

import android.util.Log;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Random;

/** Helper class used to communicate with the demo server. */
public final class ServerUtils {

    private static final int MAX_ATTEMPTS = 2;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();


    private static final String GCM_REGISTRATION_URL = Commons.SERVER_ROOT_URL + "/device/create";
    private static final String USER_REGISTRATION_URL = Commons.SERVER_ROOT_URL + "/user/create";
    private static final String TAG = ServerUtils.class.getSimpleName();


    public static boolean registerForGcm(String registrationId, String deviceId, Long userId) {
        try {
            HttpResponse httpResponse = doHttpCall(buildRegistationIdRequest(registrationId, deviceId, userId));
            return httpResponse != null;
        } catch (IOException e) {
            return false;
        } catch (JSONException e) {
            return false;
        }
    }

    private static HttpPost buildRegistationIdRequest(String registrationId, String deviceId, Long userId) throws JSONException, UnsupportedEncodingException {
        HttpPost putRequest = new HttpPost(GCM_REGISTRATION_URL);
        putRequest.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        JSONObject regObject = new JSONObject();
        regObject.put("deviceId", deviceId);
        regObject.put("registrationId", registrationId);
        JSONObject userObject = new JSONObject();
        userObject.put("id", userId);
        regObject.put("user", userObject);
        putRequest.setEntity(new StringEntity(regObject.toString()));
        return putRequest;
    }

    public static Long registerUser(PlusClient plusClient) {
        try {
            HttpResponse httpResponse = doHttpCall(buildUserRegistationRequest(plusClient));
            return new JSONObject(EntityUtils.toString(httpResponse.getEntity())).getLong("id");
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    private static HttpRequestBase buildUserRegistationRequest(PlusClient plusClient) throws JSONException, UnsupportedEncodingException {
        HttpPost postRequest = new HttpPost(USER_REGISTRATION_URL);
        postRequest.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        JSONObject regObject = new JSONObject();
        regObject.put("email", plusClient.getAccountName());
        Person currentPerson = plusClient.getCurrentPerson();
        regObject.put("firstName", currentPerson.getName().getGivenName());
        regObject.put("lastName", currentPerson.getName().getFamilyName());
        postRequest.setEntity(new StringEntity(regObject.toString()));
        return postRequest;
    }

    public static HttpResponse doHttpCall(HttpRequestBase request) {
        // TODO Add logs to explain
        HttpClient httpClient = new DefaultHttpClient();
        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            try {
                // TODO check connection before call
                HttpResponse httpResponse = httpClient.execute(request);
                if (httpResponse.getStatusLine().getStatusCode() != 200) {
                    if (!sleep(backoff)) return null;
                } else {
                    return httpResponse;
                }
            } catch (Exception e) {
                if (i == MAX_ATTEMPTS) {
                    break;
                }

                if (!sleep(backoff)) return null;

                // increase backoff exponentially
                backoff *= 2;
            }
        }
        return null;
    }

    private static boolean sleep(long backoff) {
        try {
            Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
            Thread.sleep(backoff);
        } catch (InterruptedException e1) {
            // Activity finished before we complete - exit.
            Log.d(TAG, "Thread interrupted: abort remaining retries!");
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }
}
