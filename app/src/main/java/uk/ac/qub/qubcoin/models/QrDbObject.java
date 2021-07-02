package uk.ac.qub.qubcoin.models;

import java.io.Serializable;

public class QrDbObject implements Serializable {

    private static final String TAG = QrDbObject.class.getName();
    private String timestamp;
    private String description;
    private float value;

    public QrDbObject() {

    }

    public QrDbObject(String timestamp, String description, float value) {
        this.timestamp = timestamp;
        this.description = description;
        this.value = value;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public float getValue() {
        return value;
    }
}
