package com.github.codetanzania.api;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.codetanzania.model.Reporter;
import com.github.codetanzania.model.ServiceGroup;
import com.github.codetanzania.model.adapter.ServiceRequests;
import com.github.codetanzania.util.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import tz.co.codetanzania.R;

public class Open311Api {

    public interface ServiceGroupEndpoint {
        @GET("/servicegroups")
        @Headers({
                "Content-Type: application/json; charset='utf-8'",
                "Accept: application/json"
        })
        Call<List<ServiceGroup>> get(@Header("Authorization") String jwtToken);
    }

    public interface ServiceRequestEndpoint {
        @POST("/servicerequests")
        @Headers({"Content-Type: application/json"})
        Call<ResponseBody> openTicket(@Header("Authorization") String authorization, @Body Map<String, Object> body);

        @GET("/servicerequests")
        @Headers({"Accept: application/json"})
        Call<ServiceRequests> getByUserId(/*@Header("Authorization")*/String jwtToken);

        @GET("/servicerequests")
        @Headers({"Accept: application/json"})
        Call<ResponseBody> getByUserId(@Header("Authorization") String authorization, @Query("query")String query);

        @GET("/servicerequests")
        @Headers({"Accept: application/json"})
        Call<ResponseBody> getByTicketId(@Header("Authorization") String authorization, @Query("query")String query);
    }

    public interface AuthEndpoint {
        @POST("/signin")
        @Headers({"Accept: application/json", "Content-Type: application/json"})
        Call<ResponseBody> signIn(@Body Map<String, String> reporter);
    }

    public interface ServicesEndpoint {
        @GET("/services")
        @Headers({"Accept: application/json", "Content-Type: application/json"})
        Call<ResponseBody> getAll(@Header("Authorization") String authHeader);
    }

    public static class ServiceBuilder {

        private Context mContext;

        public ServiceBuilder(Context mContext) {
            this.mContext = mContext;
        }

        private Retrofit retrofit() {

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .create();

            return new Retrofit.Builder()
                    .baseUrl(mContext.getString(R.string.enpoint_url))
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        public ServiceRequestEndpoint getServiceRequests() {
            return retrofit().create(ServiceRequestEndpoint.class);
        }

        public Call<ResponseBody> getIssuesByUser(@NonNull String token, @NonNull String phone,
                                                  Callback<ResponseBody> callback){
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put("reporter.phone", phone);
            GsonBuilder gsonBuilder = new GsonBuilder().setLenient();
            String queryParams = gsonBuilder.create().toJson(queryMap);

            return build(Open311Api.ServiceRequestEndpoint.class)
                    .getByUserId(String.format("Bearer %s", token), queryParams);
        }

        public <T> T build(Class<T> clazz) {
            return retrofit().create(clazz);
        }
    }
}
