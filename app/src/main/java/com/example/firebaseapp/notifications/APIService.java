package com.example.firebaseapp.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA1nZsWzQ:APA91bHQW_gpEIGQuP9JsJshMg1pd_XQ15tOrnIpWbW3GN5_QeP04JhGfENRaQoTiHMgC5vhAxrFgA3MQ7lsRmhUE3W_BUh9yi4AJosvDB6zmqNwZhZz28wJqDB_-ey5pMTMSP-WRaZ5"
    })
    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);

}
