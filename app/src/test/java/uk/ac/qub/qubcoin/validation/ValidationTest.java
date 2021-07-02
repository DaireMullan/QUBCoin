package uk.ac.qub.qubcoin.validation;

import org.junit.ComparisonFailure;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class ValidationTest {

    private static String[] validEmails = {
            "dmullan25@qub.ac.uk",
            "des.greer@qub.ac.uk",
            "a@qub.ac.uk",
            "41234567@ads.qub.ac.uk",
            "eeecs@qub.ac.uk",
            "student@eeecs.qub.ac.uk"
    };

    private static String[] invalidEmails = {
            "dmullan25@ulster.ac.uk",
            "john.doe@outlook.com",
            "@",
            "@qub",
            "@qub.ac.uk",
            "@qub.ac.uk1",
            "",
            "..",
            "qub.ac.uk@dmullan25",
            "41234567"
    };

    @Test
    public void isEmailValid_correctInput() {
        for (String validEmail : validEmails) {
            assertTrue(Validation.isEmailValid(validEmail));
        }
    }

    @Test
    public void isEmailValid_incorrectInput() {
        for (String invalidEmail : invalidEmails) {
            assertFalse(Validation.isEmailValid(invalidEmail));
        }
    }

    private static String[] validPasswords = {
            "password",
            "password123",
            "supersecret",
            "qubcoin",
            "1233456",
            "!-sk1K6ms.xZ"
    };

    private static String[] invalidPasswords = {
            "",
            "a",
            "pass",
            "123",
            "qub"
    };

    @Test
    public void isPasswordValid_correctInput() {
        for (String validPassword : validPasswords) {
            assertTrue(Validation.isPasswordValid(validPassword));
        }
    }

    @Test
    public void isPasswordValid_incorrectInput() {
        for (String invalidPassword : invalidPasswords) {
            assertFalse(Validation.isPasswordValid(invalidPassword));
        }
    }

    private static String[] validModuleCodes = {
            "0000",
            "9999",
            "0123",
            "1234",
            "1420"
    };

    @Test
    public void isModuleCodeValid_correct() {
        for (String validCode : validModuleCodes) {
            assertTrue(Validation.isModuleCodeValid(validCode));
        }
    }

    private static String[] invalidModuleCodes = {
            "",
            "1",
            "a",
            "yeo",
            "longstringofcharacters",
            "ye know yersel so ye do",
            "abcd",
            "3002.",
            "!2323"
    };

    @Test
    public void isModuleCodeValid_incorrect() {
        for (String validCode : invalidModuleCodes) {
            assertFalse(Validation.isModuleCodeValid(validCode));
        }
    }


    private static String[] validModuleNames = {
            "Computer Science Project",
            "Medicine",
            "German Language",
            "Study",
            "Vaccinations"
    };

    @Test
    public void isModuleNameValid_correct() {
        for (String validName : validModuleNames) {
            assertTrue(Validation.isModuleNameValid(validName));
        }
    }

    private static String[] invalidModuleNames = {
            "",
            "1",
            "a"
    };

    @Test
    public void isModuleNameValid_incorrect() {
        for (String validName : invalidModuleNames) {
            assertFalse(Validation.isModuleNameValid(validName));
        }
    }

    private static String[] validCourseCodes = {
            "CSC",
            "ECO",
            "MATH"
    };

    @Test
    public void isCourseCodeValid_correct() {
        for (String validCode : validCourseCodes) {
            assertTrue(Validation.isCourseCodeValid(validCode));
        }
    }

    private static String[] invalidCourseCodes = {
            "",
            "1",
            "a",
            "er",
            "COMPSCI",
            "german"
    };

    @Test
    public void isCourseCodeValid_incorrect() {
        for (String validCode : invalidCourseCodes) {
            assertFalse(Validation.isCourseCodeValid(validCode));
        }
    }

    private static String[] validQUBCoinValues = {
            "0.000001",
            "0.000002",
            "100000000",
            "1",
            "0.1",
            "9999999999.99999999",
            Float.toString(Float.POSITIVE_INFINITY)
    };

    @Test
    public void isQUBCoinValueValid_correct() {
        for (String value : validQUBCoinValues) {
            assertTrue(Validation.isQUBCoinValueValid(value));
        }
    }

    private static String[] invalidQUBCoinValues = {
            "YEO",
            "",
            Float.toString(Float.NaN),
            Float.toString(Float.NEGATIVE_INFINITY),
            Float.toString((float) -0.1),
            Float.toString((float) -1),
            "  ",
            "\n",
            "\t"
    };

    @Test
    public void isQUBCoinValueValid_incorrect() {
        for (String value : invalidQUBCoinValues) {
            assertFalse(Validation.isQUBCoinValueValid(value));
        }
    }

    private static final String[] validQUBCoinReasons = {
            "This qubcoin is duly awarded for attending the lecture for this module on the specified day at the required time",
            "Attendance",
            "Coding competition",
            "On time to class"
    };

    @Test
    public void isQUBCoinReasonValid_correct() {
        for (String value : validQUBCoinReasons) {
            assertTrue(Validation.isQUBCoinReasonValid(value));
        }
    }

    private static final String[] invalidQUBCoinReasons = {
            "",
            "  ",
            "\n",
            "\t"
    };

    @Test
    public void isQUBCoinReasonValid_incorrect() {
        for (String value : invalidQUBCoinReasons) {
            assertFalse(Validation.isQUBCoinReasonValid(value));
        }
    }
}