package net.nashlegend.sourcewall.request.interceptors;

import net.nashlegend.sourcewall.request.RequestObject;
import net.nashlegend.sourcewall.request.RequestObject.DetailedCallBack;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by NashLegend on 16/6/29.
 * 下载的Interceptor
 */
public class DownloadProgressInterceptor implements Interceptor {
    DetailedCallBack callBack;

    public DownloadProgressInterceptor(DetailedCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        //包装响应体并返回
        return originalResponse.newBuilder()
                .body(new ProgressResponseBody(originalResponse.body(), callBack))
                .build();
    }
}