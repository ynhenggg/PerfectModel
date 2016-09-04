package com.example.bigmercu.perfectmodel.model.impl;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.bigmercu.perfectmodel.model.UserInfoModel;
import com.example.bigmercu.perfectmodel.model.api.UserInfoService;
import com.example.bigmercu.perfectmodel.model.db.DbOperating;
import com.example.bigmercu.perfectmodel.model.db.GithubUser;
import com.example.bigmercu.perfectmodel.model.db.GithubUserHepler;
import com.example.bigmercu.perfectmodel.util.MyAdapterFactory;
import com.example.bigmercu.perfectmodel.util.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by bigmercu on 2016/9/3.
 * Email: bigmercu@gmail.com
 */

public class UserInfoModelImpl implements UserInfoModel {

    private static final String TAG = UserInfoModelImpl.class.getSimpleName();
    private UserInfoService mUserInfoService;
    private Gson mGson;
    private DbOperating mDbOperating;
    private SQLiteDatabase mWriteSQLiteDatabase;
    private SQLiteDatabase mReadSQLiteDatabase;
    private GithubUser mGithubUser;

    public static UserInfoModelImpl getInstance() {
        return UserInfoModelImplInstanceHolder.userInfoModel;
    }

    public static class UserInfoModelImplInstanceHolder {
        private static UserInfoModelImpl userInfoModel = new UserInfoModelImpl();
    }

    public UserInfoModelImpl() {
        mUserInfoService = RetrofitClient.getInstance().create(UserInfoService.class);
        mDbOperating = new DbOperating();
        mGson = new GsonBuilder().registerTypeAdapterFactory(MyAdapterFactory.create()).create();
        mWriteSQLiteDatabase = GithubUserHepler.gitInstance().getWritableDatabase();
        mReadSQLiteDatabase = GithubUserHepler.gitInstance().getReadableDatabase();
    }

    @Override
    public void getUserInfo(final String name, final onGetDataListener listener) {

        Observable.just(name)
                .subscribeOn(Schedulers.io())
                .map(new Func1<String, GithubUser>() {
                    @Override
                    public GithubUser call(String s) {
                        List<GithubUser> list = mDbOperating.query(mReadSQLiteDatabase, name);
                        if (list.size() == 0) {
                            mUserInfoService.getUserInfo(s)
                                    .map(new Func1<ResponseBody, GithubUser>() {
                                        @Override
                                        public GithubUser call(ResponseBody responseBody) {
                                            try {
                                                mGithubUser = mGson.fromJson(responseBody.string(), GithubUser.class);
                                                mDbOperating.insert(mWriteSQLiteDatabase,mGithubUser);
                                                return mGithubUser;
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            return null;
                                        }
                                    }).subscribe(new Action1<GithubUser>() {
                                @Override
                                public void call(GithubUser githubUser) {
                                    mGithubUser = githubUser;
                                }
                            });
                        } else {
                            mGithubUser = list.get(0);
                        }
                        return mGithubUser;
                    }
                }).subscribe(new Subscriber<GithubUser>() {
            @Override
            public void onCompleted() {
                Log.d(TAG,"onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                listener.onFiled(e.getMessage());
            }

            @Override
            public void onNext(GithubUser githubUser) {
                listener.onSuccess(githubUser);
            }
        });
    }

    @Override
    public void onCancle() {
        if (mWriteSQLiteDatabase != null)
            mWriteSQLiteDatabase.close();
        if (mReadSQLiteDatabase != null)
            mReadSQLiteDatabase.close();
    }
}
