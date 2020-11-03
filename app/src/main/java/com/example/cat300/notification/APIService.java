package com.example.cat300.notification;

import com.example.cat300.notification.MyResponse;
import com.example.cat300.notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAAd-D4_8:APA91bFlxW902NUTFX7GgCdeEyDcRy1bfn1k5eiSdp0qdqc0dM5GHOWhula1jFOpVGimpeQbVDUPYHAXtQmc0PQfb15EE3mwP71K-H_y4zp7474OHemIduKiavBPOC7DSSfak-bvivTT"
            }

    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
