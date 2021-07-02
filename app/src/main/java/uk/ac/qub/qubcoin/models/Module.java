package uk.ac.qub.qubcoin.models;

import java.io.Serializable;
import java.util.HashMap;

public class Module implements Serializable {

    private String code;
    private String name;
    private HashMap<String, QrDbObject> qrCodes;

    // default constructor required for calls to DataSnapshot.getValue(Module.class)
    public Module() {

    }

    public Module(String code, String name, HashMap<String, QrDbObject> qrCodes) {
        this.code = code;
        this.name = name;
        this.qrCodes = qrCodes;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public HashMap<String, QrDbObject> getQrCodes() {
        return qrCodes;
    }

    public static String combineModuleCodes(String courseCode, String moduleCode) {
        return courseCode.trim().concat(moduleCode.trim());
    }
}
