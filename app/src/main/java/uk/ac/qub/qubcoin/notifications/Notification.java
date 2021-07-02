package uk.ac.qub.qubcoin.notifications;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;


public class Notification {

    /**
     * Launches a toast to the screen with given string
     */
    public static void toast(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }
}
