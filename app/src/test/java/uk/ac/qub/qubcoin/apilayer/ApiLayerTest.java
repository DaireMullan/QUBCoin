package uk.ac.qub.qubcoin.apilayer;

import org.junit.Test;

import uk.ac.qub.qubcoin.BuildConfig;

import static org.junit.Assert.assertEquals;
import static uk.ac.qub.qubcoin.api.Utilities.getUserAgent;

public class ApiLayerTest {

    @Test
    public void getUserAgent_valid() {
        assertEquals(getUserAgent(), "QUBCoin Android Client/" + BuildConfig.VERSION_NAME);
    }
}
