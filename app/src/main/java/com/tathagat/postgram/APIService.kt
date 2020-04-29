package com.tathagat.postgram

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface APIService {
    @Headers("Content-Type:application/json", "Authorization:key=AAAAxx7EWmU:APA91bHAwcJmjztKfuWL3gM-gxm1u_-AvRCdp51_X6AWj6Lm_B-BCf4vd1PAXb-Cu_HpD2WinpILDaj4vgApkOKTCPDzrWtYt4Pc63ePxBXx3m6mZ4E5X1Fff8oFtmKuHpqZZYRNNJMl")
    @POST("fcm/send")
    fun sendNotification(@Body body: Sender): Call<MyResponse>
}