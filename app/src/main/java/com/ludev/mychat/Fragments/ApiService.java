package com.ludev.mychat.Fragments;

import com.ludev.mychat.Notifications.MyResponse;
import com.ludev.mychat.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA2flRXws:APA91bFGnVeBaVNN6Uxu7wwi_pVX5DEB4gulgFQF9GlEowt8PMo4uUIaeHh2rGcMx6U0Vwz77JeP79g7OhzY_8ExiYRDbbSQJRioz1iDl6pEYfjAunYC8NI4JFgmISQhzSm7pT9Wsb0o"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
