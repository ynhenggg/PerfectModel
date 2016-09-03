package com.example.bigmercu.perfectmodel.model.api;

import com.example.bigmercu.perfectmodel.entity.UserInfoEntity;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by bigmercu on 2016/9/3.
 * Email: bigmercu@gmail.com
 */

public interface UserInfoService {
    @GET("users/{user}")
    Observable<UserInfoEntity> getUserInfo(@Path("user") String user);
}
