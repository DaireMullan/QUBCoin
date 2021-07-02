package uk.ac.qub.qubcoin.api;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.Priority;
import com.androidnetworking.gsonparserfactory.GsonParserFactory;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import uk.ac.qub.qubcoin.BuildConfig;
import uk.ac.qub.qubcoin.logging.Logging;

import static uk.ac.qub.qubcoin.api.Utilities.getTransferParams;
import static uk.ac.qub.qubcoin.api.Utilities.getUserAgent;


class RequestFactory {

    private static final String TAG = RequestFactory.class.getName();
    private static final String url = BuildConfig.API_URL;
    private static final OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();

    /**
     * Returns an Android networking request object to query for total supply
     */
    public static ANRequest getTotalSupplyRequest() {
        Logging.debug(TAG, "Generating total supply request object");
        AndroidNetworking.setParserFactory(new GsonParserFactory());
        return AndroidNetworking.get(url + "/totalSupply")
                .setTag("getTotalSupply")
                .setPriority(Priority.LOW)
                .setUserAgent(getUserAgent())
                .setOkHttpClient(okHttpClient)
                .build();
    }

    /**
     * Returns an Android networking request object to query for balance for a user
     */
    public static ANRequest getBalanceOfRequest(String username) {
        Logging.debug(TAG, "Generating balanceOf request object");
        AndroidNetworking.setParserFactory(new GsonParserFactory());
        return AndroidNetworking.get(url + "/balanceOf/" + username)
                .setTag("getBalanceOf")
                .setPriority(Priority.LOW)
                .setUserAgent(getUserAgent())
                .setOkHttpClient(okHttpClient)
                .build();
    }

    /**
     * Returns an Android networking request object to invoke a transfer
     */
    public static ANRequest getTransferRequest(String userFrom, final String userTo, final String amount) {
        JSONObject requestBody = getTransferParams(userFrom, userTo, amount);
        Logging.debug(TAG, "Generating transfer request object: " + requestBody.toString());
        AndroidNetworking.setParserFactory(new GsonParserFactory());
        return AndroidNetworking.post(url + "/transfer")
                .addJSONObjectBody(requestBody)
                .setTag("transfer")
                .setPriority(Priority.LOW)
                .setUserAgent(getUserAgent())
                .setOkHttpClient(okHttpClient)
                .build();
    }
}
