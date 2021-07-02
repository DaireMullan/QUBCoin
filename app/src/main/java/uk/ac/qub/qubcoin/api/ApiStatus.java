package uk.ac.qub.qubcoin.api;

import org.json.JSONObject;

public interface ApiStatus {

    void success(JSONObject data);
    void fail(JSONObject data);
    void error(String message);
}
