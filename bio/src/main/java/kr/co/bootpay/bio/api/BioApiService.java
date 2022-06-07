package kr.co.bootpay.bio.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;

public class BioApiService {
    private Context context;
    private ApiRestApi api;

    public BioApiService(Context context) {
        this.context = context;

        OkHttpClient client = new OkHttpClient
                .Builder()
//                .addInterceptor(logging)
//                .cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context)))
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        api =  new Retrofit.Builder()
                .baseUrl("https://api.bootpay.co.kr/")
//                .baseUrl("https://api.bootpay.co.kr")
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiRestApi.class);
    }

    public Context getContext() {
        return this.context;
    }

    public ApiRestApi getApi() { return api; }

    public interface ApiRestApi {
        @GET("/v2/sdk/easy/wallet.json")
        Call<ResponseBody> getWalletList(
                @Header("Bootpay-Device-UUID") String deviceUUID,
                @Header("Bootpay-User-Token") String userToken
        );
    }
}
