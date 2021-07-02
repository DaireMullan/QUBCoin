package uk.ac.qub.qubcoin.utilities;

import android.content.Context;
import android.view.View;

import org.junit.Test;

import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.assertNull;
import static uk.ac.qub.qubcoin.utilities.HideKeyboard.hideKeyboard;

public class HideKeyboardTest {

    @Test
    public void hideKeyboardTest() {
        Exception ex = null;
        Context instrumentationContext = InstrumentationRegistry.getInstrumentation().getContext();
        View view = new View(instrumentationContext);
        try {
            hideKeyboard(instrumentationContext, view);
        } catch (Exception e) {
            ex = e;
        }
        assertNull(ex);
    }
}
