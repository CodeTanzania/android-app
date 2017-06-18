package com.github.codetanzania.ui.activity;

import android.support.v7.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Base activity for activities that need to fetch data using Retrofit.
 */

public abstract class RetrofitActivity<T> extends AppCompatActivity implements Callback<T> {
    private Call<T> mHttpCall;

    /* Stores data from server. Use activity callbacks to retain
     * this data so that we do not need to query it again when activity
     * onResume() is invoked e.g when screen orientation changes. */
    protected T mData;

    /*
     * Method invoked to initialize mHttpCall.
     */
    protected abstract Call<T> initializeCall();

    /*
     * Method to assign data from Http response.
     */
    protected abstract  T getData(Response<T> response);

    @Override
    protected void onResume() {
        super.onResume();

        // If no data, make call
        if (mData == null && (mHttpCall == null || mHttpCall.isCanceled())) {
            mHttpCall = initializeCall();
            mHttpCall.enqueue(this);
        }
    }

    @Override
    protected void onPause() {
        cancelPendingCall();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        cancelPendingCall();
        super.onDestroy();
    }

    private void cancelPendingCall() {
        if (mHttpCall != null
                && mHttpCall.isExecuted()
                && !mHttpCall.isCanceled()) {
            mHttpCall.cancel();
        }
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            mData = getData(response);
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        mHttpCall = null;
    }
}
