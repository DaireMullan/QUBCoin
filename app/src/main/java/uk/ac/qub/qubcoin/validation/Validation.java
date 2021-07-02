package uk.ac.qub.qubcoin.validation;

import uk.ac.qub.qubcoin.logging.Logging;

public class Validation {

    public static boolean isEmailValid(String email) {
        return email.contains("@") && email.endsWith("qub.ac.uk") && email.split("@")[0].trim().length() > 0;
    }

    public static boolean isPasswordValid(String password) {
        return password.length() > 5;
    }

    public static boolean isModuleCodeValid(String moduleCode) {
        if(moduleCode.trim().length() != 4){
            return false;
        }
        try {
            int mCode = Integer.parseInt(moduleCode);
            return (mCode >= 0 && mCode <= 9999);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isModuleNameValid(String moduleName) {
        return moduleName.trim().length() > 1;
    }

    public static boolean isCourseCodeValid(String courseCode) {
        return (courseCode.trim().length() == 3 || courseCode.trim().length() == 4);
    }

    public static boolean isQUBCoinValueValid(String value) {
        try {
            float val = Float.parseFloat(value.trim());
            return (val > 0.0);
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public static boolean isQUBCoinReasonValid(String reason) {
        return (reason.trim().length() > 0);
    }
}
