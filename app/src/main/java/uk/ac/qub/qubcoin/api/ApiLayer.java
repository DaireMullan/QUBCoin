package uk.ac.qub.qubcoin.api;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.qub.qubcoin.logging.Logging;

public class ApiLayer {

    private static final String TAG = ApiLayer.class.getName();
    private static final String BACKUP_ERROR_MESSAGE =
            "There was an issue retrieving data from QUBCoin server, try again later";

    public static void getTotalSupply(ApiStatus apiStatus) {
        RequestFactory.getTotalSupplyRequest().getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                parseGetResponse(response, apiStatus);
            }

            @Override
            public void onError(ANError anError) {
                Logging.error(TAG, BACKUP_ERROR_MESSAGE + ": " + anError.getMessage());
                apiStatus.error(BACKUP_ERROR_MESSAGE);
            }
        });
    }

    public static void getBalanceOf(String username, ApiStatus apiStatus) {
        RequestFactory.getBalanceOfRequest(username).getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                parseGetResponse(response, apiStatus);
            }

            @Override
            public void onError(ANError anError) {
                Logging.error(TAG, BACKUP_ERROR_MESSAGE + ": " + anError.getMessage());
                apiStatus.error(BACKUP_ERROR_MESSAGE);
            }
        });
    }

    public static void transfer(String userFrom, String userTo, String amount, ApiStatus apiStatus) {
        RequestFactory.getTransferRequest(userFrom, userTo, amount).getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status = response.getString("status");
                    switch (status) {
                        case "success":
                            apiStatus.success(null);
                            break;
                        case "fail":
                            apiStatus.fail(null);
                            break;
                        case "error":
                            apiStatus.error(response.getString("message"));
                            break;
                        default:
                            apiStatus.error(BACKUP_ERROR_MESSAGE);
                            break;
                    }
                } catch (JSONException e) {
                    Logging.error(TAG, BACKUP_ERROR_MESSAGE + ": " + e.getMessage());
                    apiStatus.error(BACKUP_ERROR_MESSAGE);
                }
            }

            @Override
            public void onError(ANError anError) {
                Logging.error(TAG, BACKUP_ERROR_MESSAGE + ": " + anError.getMessage());
                apiStatus.error(BACKUP_ERROR_MESSAGE);
            }
        });
    }

    private static void parseGetResponse(JSONObject response, ApiStatus apiStatus) {
        try {
            String status = response.getString("status");
            switch (status) {
                case "success":
                    apiStatus.success((JSONObject) response.get("data"));
                    break;
                case "fail":
                    apiStatus.fail((JSONObject) response.get("data"));
                    break;
                case "error":
                    apiStatus.error(response.getString("message"));
                    break;
                default:
                    apiStatus.error(BACKUP_ERROR_MESSAGE);
                    break;
            }
        } catch (JSONException | ClassCastException e) {
            Logging.error(TAG, BACKUP_ERROR_MESSAGE + ": " + e.getMessage());
            apiStatus.error(BACKUP_ERROR_MESSAGE);
        }
    }

}
