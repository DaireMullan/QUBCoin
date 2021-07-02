package uk.ac.qub.qubcoin.api;

import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.qub.qubcoin.BuildConfig;
import uk.ac.qub.qubcoin.logging.Logging;

public class Utilities {

    private static final String TAG = Utilities.class.getName();

    public static String getUserAgent() {
        return "QUBCoin Android Client/" + BuildConfig.VERSION_NAME;
    }

    public static JSONObject getTransferParams(String userFrom, final String userTo, final String amount) {
        Logging.debug(TAG, "Packaging transfer params into JSON");
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("userFrom", userFrom);
            requestBody.put("userTo", userTo);
            requestBody.put("amount", amount);
            return requestBody;
        } catch (JSONException e) {
            Logging.error(TAG, "Error creating JSON: " + e.getMessage());
            return null;
        }
    }
}
