package uk.ac.qub.qubcoin.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import uk.ac.qub.qubcoin.R;
import uk.ac.qub.qubcoin.logging.Logging;
import uk.ac.qub.qubcoin.models.QrDbObject;
import uk.ac.qub.qubcoin.notifications.Notification;
import uk.ac.qub.qubcoin.utilities.ProgressSpinner;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static uk.ac.qub.qubcoin.qrcode.QrCode.getTimestamp;
import static uk.ac.qub.qubcoin.utilities.HideKeyboard.hideKeyboard;
import static uk.ac.qub.qubcoin.validation.Validation.isQUBCoinReasonValid;
import static uk.ac.qub.qubcoin.validation.Validation.isQUBCoinValueValid;

public class CreateQrCodeActivity extends AppCompatActivity {

    private static final String TAG = CreateQrCodeActivity.class.getName();
    private static final String MODULE_ID_INTENT_NAME = "MODULE_ID";
    private String moduleId;
    private EditText qubcoinValueView;
    private EditText qrReasonView;
    private ProgressSpinner progressSpinnerDialog;

    private FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_qr_code);

        moduleId = (String) getIntent().getExtras().getString(MODULE_ID_INTENT_NAME);

        qubcoinValueView = (EditText) findViewById(R.id.qr_value);
        qrReasonView = (EditText) findViewById(R.id.qr_reason);

        progressSpinnerDialog = new ProgressSpinner(this);

        db = FirebaseDatabase.getInstance();
    }

    public void onClickCancel(View view) {
        hideKeyboard(this, view);
        progressSpinnerDialog.showProgress(false);
        finish();
    }

    public void onClickCreateQr(View view) {
        final String value = qubcoinValueView.getText().toString();
        final String reason = qrReasonView.getText().toString();

        if (validateForm(value, reason)) {
            hideKeyboard(this, view);
            createQr(value, reason);
        }
    }

    private void createQr(String value, String reason) {
        String timestamp = getTimestamp();
        QrDbObject qr = new QrDbObject(timestamp, reason, Float.parseFloat(value));
        // add new qr code to module
        DatabaseReference dbRef = db.getReference("modules");
        String uId = dbRef.child(moduleId).child("qrCodes").push().getKey();
        dbRef.child(moduleId).child("qrCodes").child(uId).setValue(qr).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressSpinnerDialog.showProgress(false);
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Logging.debug(TAG, "qrcode creation:success");
                    Notification.toast(CreateQrCodeActivity.this, "Qr Code created!");
                    finish();
                } else {
                    // If sign in fails, display a message to the user.
                    Logging.warn(TAG, "qrcode creation:failure" + task.getException());
                    Notification.toast(CreateQrCodeActivity.this, "Error creating QR Code\nPlease try again");
                }
            }
        });
    }

    private boolean validateForm(String value, String reason){
        qubcoinValueView.setError(null);
        qrReasonView.setError(null);
        boolean cancel = false;
        View focusView = null;

        // Check for a valid value, if the user entered one.
        if (TextUtils.isEmpty(value) || !isQUBCoinValueValid(value)) {
            qubcoinValueView.setError(getString(R.string.error_invalid_qubcoin_value));
            focusView = qubcoinValueView;
            cancel = true;
        }

        int val = Integer.parseInt(value);
        if (val > 5) {
            qubcoinValueView.setError(getString(R.string.error_qubcoin_value_too_large));
            focusView = qubcoinValueView;
            cancel = true;
        }

        // Check for a valid qr reason, if the user entered one.
        if (TextUtils.isEmpty(reason) || !isQUBCoinReasonValid(reason)) {
            qrReasonView.setError(getString(R.string.error_invalid_qr_reason));
            focusView = qrReasonView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner
            progressSpinnerDialog.showProgress(true);
        }
        return !cancel;
    }
}