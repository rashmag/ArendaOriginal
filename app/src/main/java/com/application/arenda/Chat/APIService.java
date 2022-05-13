package com.application.arenda.Chat;

import com.application.arenda.Notifications.MyResponse;
import com.application.arenda.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA8pk9emU:APA91bHKnJR9LBrKpM5ub5ga58SKzKCNt5nqQl-KCLjrZ2h8oIBKfS8wDK3cDSIbbXEt1o7jXhmbijXgwSgYSTS5vc_Rz09ue-c92rylOEnpR5DtsyK6-d6Y0QhQa79g6TIYuL4yKkTe"
    })
    @POST("fcm/send")
    Call<MyResponse> sendNotifications(@Body Sender body);
}
