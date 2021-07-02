package uk.ac.qub.qubcoin.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import uk.ac.qub.qubcoin.FirebaseDbHelper;
import uk.ac.qub.qubcoin.R;
import uk.ac.qub.qubcoin.api.ApiLayer;
import uk.ac.qub.qubcoin.api.ApiStatus;
import uk.ac.qub.qubcoin.logging.Logging;
import uk.ac.qub.qubcoin.models.Module;
import uk.ac.qub.qubcoin.models.UserType;
import uk.ac.qub.qubcoin.notifications.Notification;
import uk.ac.qub.qubcoin.recyclerviewadapters.ViewModulesRecyclerViewAdapter;
import uk.ac.qub.qubcoin.utilities.HideKeyboard;
import uk.ac.qub.qubcoin.utilities.ProgressSpinner;
import uk.ac.qub.qubcoin.validation.Validation;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static uk.ac.qub.qubcoin.validation.Validation.isEmailValid;
import static uk.ac.qub.qubcoin.validation.Validation.isQUBCoinValueValid;

public class StudentMainActivity extends AppCompatActivity {

    private static final String TAG = StudentMainActivity.class.getName();
    private ViewModulesRecyclerViewAdapter viewModulesRecyclerViewAdapter;
    private RecyclerView recyclerView;
    private View noModulesLayer;
    private EditText editTextUserTo;
    private EditText editTextTransferValue;
    private TextView textViewBalance;
    private ProgressSpinner progressSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);
        // initialize variables on page load

        textViewBalance = (TextView) findViewById(R.id.textViewBalanceResult);
        editTextUserTo = (EditText) findViewById(R.id.editTextUserTo);
        editTextTransferValue = (EditText) findViewById(R.id.editTextAmount);

        progressSpinner = new ProgressSpinner(this);

        AndroidNetworking.initialize(getApplicationContext());
        updateWelcomeString();
        getBalanceOf();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewStudentModule);
        new FirebaseDbHelper().readModules(new FirebaseDbHelper.ModuleStatus() {
            @Override
            public void dataIsLoaded(List<Module> modules, List<String> moduleIds) {
                if(modules.size() > 0) {
                    viewModulesRecyclerViewAdapter =
                            new ViewModulesRecyclerViewAdapter(StudentMainActivity.this, modules, moduleIds, UserType.STUDENT);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(StudentMainActivity.this);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setAdapter(viewModulesRecyclerViewAdapter);
                } else {
                    noModulesLayer = findViewById(R.id.nothingToDisplayLayer);
                    noModulesLayer.setVisibility(VISIBLE);
                    recyclerView.setVisibility(GONE);
                }
            }
        });
    }

    public void getBalanceOf(View view) {
        getBalanceOf();
    }

    private void getBalanceOf() {
        ApiLayer.getBalanceOf(getCurrentUserEmail(), new ApiStatus() {
            @Override
            public void success(JSONObject data) {
                try {
                    String balance = data.getString("balance");
                    textViewBalance.setText(balance);
                } catch (JSONException e) {
                    Logging.error(TAG, e.getMessage());
                    Notification.toast(StudentMainActivity.this, "Error getting QUBCoin Balance from server");
                }
            }

            @Override
            public void fail(JSONObject data) {
                Notification.toast(StudentMainActivity.this, "Error getting QUBCoin Balance from server");
            }

            @Override
            public void error(String message) {
                Notification.toast(StudentMainActivity.this, "Error getting QUBCoin Balance from server");
            }
        });
    }

    private void updateBalanceOf() {
        // update the balance for the user after a 2.5 delay from transfer
        Logging.debug(TAG, "Refresh balance after 2.5s delay");
        try {
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getBalanceOf();
                }
            }, 2500);
        } catch (Exception e) {
            Logging.error(TAG, "Error when refreshing balance of for user: " + e.getMessage());
        }
    }

    public void onClickTransfer(View view) {
        // password prompt for security
        if(validateTransfer()) {
            passwordPrompt();
        } else {
            Notification.toast(this, "Invalid Transfer details - check and try again");
        }
    }

    private boolean validateTransfer() {
        String userTo = editTextUserTo.getText().toString();
        String value = editTextTransferValue.getText().toString();

        // Reset errors.
        editTextUserTo.setError(null);
        editTextTransferValue.setError(null);

        boolean cancel = false;
        View focusView = null;

        // Check for a valid username, if the user entered one.
        if (TextUtils.isEmpty(userTo) || !isEmailValid(userTo)) {
            editTextUserTo.setError(getString(R.string.error_email_invalid));
            focusView = editTextUserTo;
            cancel = true;
        }

        // Check for a valid qr reason, if the user entered one.
        if (TextUtils.isEmpty(value) || !isQUBCoinValueValid(value)) {
            editTextTransferValue.setError(getString(R.string.error_invalid_qubcoin_value));
            focusView = editTextTransferValue;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt transfer and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        return !cancel;
    }

    private void passwordPrompt() {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.password_dialog, null);
        final EditText passwordText = (EditText) promptsView.findViewById(R.id.passwordPromptText);
        AlertDialog.Builder joinModuleDialog = new AlertDialog.Builder(this);
        joinModuleDialog.setView(promptsView);
        joinModuleDialog.setTitle("Are you sure you want to transfer QUBCoin?");
        joinModuleDialog.setMessage("Enter Password to confirm:");
        joinModuleDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setPositiveButton("Transfer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String password = (passwordText.getText()).toString();
                if (Validation.isPasswordValid(password)) {
                    // show progress spinner
                    progressSpinner.showProgress(true);

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    AuthCredential auth = EmailAuthProvider.getCredential(getCurrentUserEmail(), password);
                    user.reauthenticate(auth).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                transfer();
                            } else {
                                String message = "Password incorrect - try again";
                                progressSpinner.showProgress(false);
                                Logging.info(TAG, message);
                                Notification.toast(StudentMainActivity.this, message);
                                dialogInterface.dismiss();
                            }
                        }
                    });
                } else {
                    String message = "Invalid Password - try again";
                    Logging.info(TAG, message);
                    Notification.toast(StudentMainActivity.this, message);
                    dialogInterface.dismiss();
                }
            }
        });

        joinModuleDialog.show();
    }


    private void transfer() {
        String userFrom = getCurrentUserEmail();
        String userTo = editTextUserTo.getText().toString();
        String amount = editTextTransferValue.getText().toString();

        ApiLayer.transfer(userFrom, userTo, amount, new ApiStatus() {
            @Override
            public void success(JSONObject data) {
                progressSpinner.showProgress(false);
                String message = amount + " QUBCoin successfully transferred to " + userTo;
                Notification.toast(StudentMainActivity.this, message);
                Logging.info(TAG, message);
                updateBalanceOf();
                clearTransferForm();
            }

            @Override
            public void fail(JSONObject data) {
                progressSpinner.showProgress(false);
                String message = "Transfer Unsuccessful - please check username / value and try again";
                if(data.has("message")) {
                    try {
                        message = data.getString("message");
                    } catch (JSONException e) {
                        Logging.error(TAG, e.getMessage());
                    }
                }
                Notification.toast(StudentMainActivity.this, message);
                Logging.info(TAG, message);
            }

            @Override
            public void error(String message) {
                progressSpinner.showProgress(false);
                Notification.toast(StudentMainActivity.this, message);
                Logging.error(TAG, message);
            }
        });
    }

    private void clearTransferForm() {
        editTextTransferValue.setText("");
        editTextUserTo.setText("");
        HideKeyboard.hideKeyboard(this, this.getCurrentFocus());
    }

    private String getCurrentUserEmail() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser.getEmail();
    }

    private void updateWelcomeString() {
        final TextView textView = (TextView) findViewById(R.id.textViewUserWelcomeEmail);
        String welcomeString = getCurrentUserEmail();
        textView.setText(welcomeString);
    }

    public void scanQrCode(View view) {
        // open camera activity on top of student main page
        Intent myIntent = new Intent(StudentMainActivity.this, CameraActivity.class);
        StudentMainActivity.this.startActivity(myIntent);
    }

    public void joinModule(View view) {
        Intent intent = new Intent(StudentMainActivity.this, JoinModuleActivity.class);
        startActivity(intent);
    }

    public void signOut(View view) {
        // sign out user and open login screen
        Logging.debug(TAG, "Student user signing out");
        FirebaseAuth.getInstance().signOut();
        Intent myIntent = new Intent(StudentMainActivity.this, LoginActivity.class);
        StudentMainActivity.this.startActivity(myIntent);
        finish();
    }

}
