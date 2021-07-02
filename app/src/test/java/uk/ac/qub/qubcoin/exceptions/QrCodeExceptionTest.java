package uk.ac.qub.qubcoin.exceptions;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QrCodeExceptionTest {

    @Test
    public void testQrCodeException() {
        String message = "QR Code image cannot be created";
        try {
            throw new QrCodeException(message);
        } catch (QrCodeException e) {
            assertEquals(e.getMessage(), message);
        }
    }
}
