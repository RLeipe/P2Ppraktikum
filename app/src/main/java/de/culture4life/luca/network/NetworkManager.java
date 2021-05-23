package de.culture4life.luca.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import de.culture4life.luca.BuildConfig;
import de.culture4life.luca.Manager;
import de.culture4life.luca.network.endpoints.LucaEndpointsV3;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.CertificatePinner;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkManager extends Manager {

    private static final String API_BASE_URL_PRODUCTION = "https://192.168.178.35/api/v3/";
    private static final String API_BASE_URL_STAGING = "https://192.168.178.35/api/v3/";
    public static final String API_BASE_URL = BuildConfig.DEBUG ? API_BASE_URL_STAGING : API_BASE_URL_PRODUCTION;
    private static final long DEFAULT_REQUEST_TIMEOUT = TimeUnit.SECONDS.toMillis(50);
    private static final String USER_AGENT = createUserAgent();

    private final RxJava3CallAdapterFactory rxAdapter;

    private Gson gson;
    private Retrofit retrofit;
    private LucaEndpointsV3 lucaEndpointsV3;
    private ConnectivityManager connectivityManager;

    public NetworkManager() {
        rxAdapter = RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io());
    }

    @Override
    protected Completable doInitialize(@NonNull Context context) {
        return Completable.fromAction(() -> connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))
                .andThen(setupNetworking());
    }

    private Completable setupNetworking() {
        return Completable.fromAction(() -> {

            gson = new GsonBuilder()
                    .setLenient()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .create();

            OkHttpClient okHttpClient = createOkHttpClient();

            retrofit = new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(rxAdapter)
                    .client(okHttpClient)
                    .build();

            lucaEndpointsV3 = retrofit.create(LucaEndpointsV3.class);
        });
    }

    @NonNull
    private OkHttpClient createOkHttpClient() throws NoSuchAlgorithmException, KeyManagementException {

        final TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };

        // Install the all-trusting trust manager
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        // Create an ssl socket factory with our all-trusting manager
        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        Interceptor userAgentInterceptor = chain -> chain.proceed(chain.request()
                .newBuilder()
                .header("User-Agent", USER_AGENT)
                .build());



        /*Interceptor timeoutInterceptor = chain -> {
            int timeout = (int) getRequestTimeout(chain.request());
            return chain.withConnectTimeout(timeout, TimeUnit.MILLISECONDS)
                    .withReadTimeout(timeout, TimeUnit.MILLISECONDS)
                    .withWriteTimeout(timeout, TimeUnit.MILLISECONDS)
                    .proceed(chain.request());
        };*/


        CertificatePinner certificatePinner = new CertificatePinner.Builder()
                .add("**.luca-app.de", "sha256/wjD2X9ht0iXPN2sSXiXd2aF6ar5cxHOmXZnnkAiwVpU=") // CN=*.luca-app.de,O=neXenio GmbH,L=Berlin,ST=Berlin,C=DE,2.5.4.5=#130c43534d303233353532353339
                .build();

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0])
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .callTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(userAgentInterceptor)
                .certificatePinner(certificatePinner);

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
            builder.authenticator((route, response) -> {
                String credential = Credentials.basic(BuildConfig.STAGING_API_USERNAME, BuildConfig.STAGING_API_PASSWORD);
                return response.request().newBuilder()
                        .header("Authorization", credential)
                        .build();
            });
        }

        return builder.build();
    }

    @Deprecated
    public LucaEndpointsV3 getLucaEndpoints() {
        return lucaEndpointsV3;
    }

    public Single<LucaEndpointsV3> getLucaEndpointsV3() {
        return Single.defer(() -> getInitializedField(lucaEndpointsV3));
    }

    public Completable assertNetworkConnected() {
        return isNetworkConnected()
                .flatMapCompletable(isNetworkConnected -> {
                    if (isNetworkConnected) {
                        return Completable.complete();
                    } else {
                        return Completable.error(new NetworkUnavailableException("Network is not connected"));
                    }
                });
    }

    public Single<Boolean> isNetworkConnected() {
        return Single.defer(() -> getInitializedField(connectivityManager))
                .flatMapMaybe(manager -> Maybe.fromCallable(manager::getActiveNetworkInfo))
                .map(NetworkInfo::isConnectedOrConnecting)
                .defaultIfEmpty(false);
    }

    private static String createUserAgent() {
        String appVersionName = BuildConfig.VERSION_NAME;
        String deviceName = Build.MANUFACTURER + " " + Build.MODEL;
        String androidVersionName = Build.VERSION.RELEASE;
        return "luca/" + appVersionName + " (Android " + androidVersionName + ";" + deviceName + ")";
    }

    private static long getRequestTimeout(@NonNull Request request) {
        long timeout;
        String path = request.url().encodedPath();
        if (path.contains("/sms/request")) {
            timeout = TimeUnit.SECONDS.toMillis(45);
        } else {
            timeout = DEFAULT_REQUEST_TIMEOUT;
        }
        return timeout;
    }

    public static boolean isHttpException(@NonNull Throwable throwable, int expectedStatusCode) {
        if (!(throwable instanceof HttpException)) {
            return false;
        }
        int actualStatusCode = ((HttpException) throwable).code();
        return expectedStatusCode == actualStatusCode;
    }

}
