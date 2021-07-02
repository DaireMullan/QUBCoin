package uk.ac.qub.qubcoin.qrcode;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.ac.qub.qubcoin.qrcode.QrCode.getQrFilename;
import static uk.ac.qub.qubcoin.qrcode.QrCode.getTimestamp;

public class QrCodeTest {

    static String[] validQrContents = {
            "Hello World!",
            "QUBCOINQRCODE001",
            "a",
            "1234567890",
            "123456789012345678901234567890123456789012345678901234567890",
            "password",
            "1"
    };

    static String[] invalidQrContents = {
            "",
            " ",
            "    ",
            "\n",
            "\t"
    };

    @Test
    public void isQrContentsValid_correctInput() {
        for(String contents: validQrContents)
        {
            assertTrue(QrCode.isQrContentsValid(contents));
        }
    }

    @Test
    public void isQrContentsValid_incorrectInput() {
        for(String contents: invalidQrContents)
        {
            assertFalse(QrCode.isQrContentsValid(contents));
        }
    }

    private HashMap<String, String> getValidFilenameMappings() {
        HashMap<String, String> filenameMappings = new HashMap<String, String>();
        filenameMappings.put("2021-02-15_11-17-50", "QUBCoin_QR_2021-02-15_11-17-50.png");
        filenameMappings.put("2000-01-01_00-00-00", "QUBCoin_QR_2000-01-01_00-00-00.png");
        return filenameMappings;
    }

    @Test
    public void testGetQrFilename() {
        HashMap<String, String> validFilenameMappings = getValidFilenameMappings();
        for(String timestamp: validFilenameMappings.keySet())
        {
            assertEquals(validFilenameMappings.get(timestamp), getQrFilename(timestamp));
        }
    }

    @Test
    public void testGetTimestamp_CorrectFormat() {
        String timestamp = getTimestamp();
        assertEquals(timestamp.charAt(2), '-');
        assertEquals(timestamp.charAt(5), '-');
        assertEquals(timestamp.charAt(10), '_');
        assertEquals(timestamp.charAt(13), '-');
        assertEquals(timestamp.charAt(16), '-');
    }
}
