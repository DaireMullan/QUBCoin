package uk.ac.qub.qubcoin.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;

@IgnoreExtraProperties
public class User {

    // constants
    private static final String student = "student";
    private static final String staff = "staff";
    private static final String TAG = User.class.getName();

    // user variables
    public String username;
    public String userType;
    public HashMap<String, Boolean> modules;

    // default constructor required for calls to DataSnapshot.getValue(User.class)
    public User() {
        username = "";
        userType = "";
        modules = new HashMap<>();
    }

    public User(String username, UserType userType, HashMap<String, Boolean> moduleCodes) {
        this.username = username;
        switch (userType) {
            case STUDENT:
                this.userType = student;
                break;
            case STAFF:
                this.userType = staff;
                break;
        }
        this.modules = moduleCodes;
    }
}
