package uk.ac.qub.qubcoin.utilities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import uk.ac.qub.qubcoin.R;

public class ProgressSpinner extends Dialog {

    private final Activity activity;
    private View progressLayer;
    private View spinner;

    public ProgressSpinner(Activity a) {
        super(a);
        this.activity = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.progress_spinner_dialog);
        progressLayer = findViewById(R.id.progress_spinner_layer);
        spinner = findViewById(R.id.progress_spinner);
    }

    public void showProgress(final boolean show) {
        if (!show) {
            this.dismiss();
        } else {
            this.show();
            progressLayer.setVisibility(show ? View.VISIBLE : View.GONE);
            int shortAnimTime = this.activity.getResources().getInteger(android.R.integer.config_shortAnimTime);

            spinner.setVisibility(show ? View.VISIBLE : View.GONE);
            spinner.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    spinner.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
    }
}
