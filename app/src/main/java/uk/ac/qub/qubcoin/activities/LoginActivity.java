package uk.ac.qub.qubcoin.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import uk.ac.qub.qubcoin.notifications.Notification;
import uk.ac.qub.qubcoin.R;
import uk.ac.qub.qubcoin.logging.Logging;
import uk.ac.qub.qubcoin.models.User;
import uk.ac.qub.qubcoin.models.UserType;
import uk.ac.qub.qubcoin.utilities.ProgressSpinner;

import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.HashMap;

import static uk.ac.qub.qubcoin.validation.Validation.isEmailValid;
import static uk.ac.qub.qubcoin.validation.Validation.isPasswordValid;
import static uk.ac.qub.qubcoin.utilities.HideKeyboard.hideKeyboard;

/**
 * A login screen that offers login via student or staff number/password.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getName();
    private static final String USER_DB_TABLE = "users";
    private static final String USER_TYPE_DB_KEY = "userType";
    private static final String DEFAULT_QUBCOIN_VALUE = "0.0";
    private static final String USER_TYPE_STAFF = "staff";

    // UI references.
    private AutoCompleteTextView mUserIdView;
    private EditText mPasswordView;
    private ProgressSpinner progressSpinnerDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // needed for debugging - always log user out on startup
        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();

        onStart();

        // Set up the login form.
        mUserIdView = (AutoCompleteTextView) findViewById(R.id.userId);

        // Set up listener on password to login on enter key press
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    onClickSignIn(getCurrentFocus());
                    return true;
                }
                return false;
            }
        });

        progressSpinnerDialog = new ProgressSpinner(this);
    }

    public void onClickSignIn(View view) {
        String userId = mUserIdView.getText().toString();
        String password = mPasswordView.getText().toString();
        if (validateForm(userId, password)) {
            hideKeyboard(this, view);
            signIn(userId, password);
        }
    }

    public void onClickRegister(View view) {
        String userId = mUserIdView.getText().toString();
        String password = mPasswordView.getText().toString();
        if (validateForm(userId, password)) {
            hideKeyboard(this, view);
            createAccount(userId, password);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in and update UI accordingly.
        updateUI(mAuth.getCurrentUser(), null);
    }

    private void updateUI(FirebaseUser user, UserType userType) {
        if (user != null) {
            if (userType != null) {
                switch(userType) {
                    case STUDENT:
                        launchStudentPortal();
                        break;
                    case STAFF:
                        launchStaffPortal();
                        break;
                }
            } else {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference(USER_DB_TABLE).child(user.getUid());
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Logging.debug(TAG, snapshot.toString());
                        if (snapshot.child(USER_TYPE_DB_KEY).getValue().toString().equals(USER_TYPE_STAFF)) {
                            Logging.info(TAG, "User is a member of staff - launch portal");
                            launchStaffPortal();
                        } else {
                            Logging.info(TAG, "User is a student - launch portal");
                            launchStudentPortal();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Logging.error(TAG, "Error validating user info" + error.toString());
                    }
                });
            }
        }
    }

    /**
     * TODO: extract to utility function
     * TODO: read database and validate the user's information
     */
    private void validateUserInfoDb(FirebaseUser firebaseUser) {
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            String userUid = firebaseUser.getUid();
            DatabaseReference myRef = database.getReference(USER_DB_TABLE);
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Logging.debug(TAG, snapshot.toString());
                    if (!snapshot.hasChild(userUid)) {
                        Logging.info(TAG, "User does not exist in the DB: Add to firebase.");
                        // here we want to prompt for user type via dialog
                        getUserTypeAndInitUserDB();
                    } else {
                        updateUI(getCurrentUser(), null);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Logging.error(TAG, "Error validating user info" + error.toString());
                }
            });
        } catch (DatabaseException e) {
            Logging.error(TAG, "Error validating user info in db");
        }

    }

    /**
     * TODO: extract as utility function
     * expand exception handling and ensure function is unit testable
     */
    private void initUserDbInfo(String userUid, String email, UserType userType) {
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference(USER_DB_TABLE).child(userUid);
            HashMap<String, Boolean> moduleCodes = new HashMap<String, Boolean>();
            myRef.setValue(new User(email, userType, moduleCodes));
            updateUI(getCurrentUser(), userType);
        } catch (DatabaseException e) {
            Logging.error(TAG, "Error adding user to database: " + e.getMessage());
            Notification.toast(LoginActivity.this, "User could not be added to database. Please try again.");
            progressSpinnerDialog.showProgress(false);
        }
    }

    /**
     * Account setup
     */
    private void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // Sign in success, update UI with the signed-in user's information
                        Logging.debug(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        validateUserInfoDb(user);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Logging.error(TAG, "Error creating account: " + e.getCause());
                        if (e instanceof FirebaseAuthInvalidUserException) {
                            String errorCode = ((FirebaseAuthInvalidUserException) e).getErrorCode();
                            if (errorCode.equals("ERROR_USER_DISABLED")) {
                                Notification.toast(LoginActivity.this, "This user account has been disabled");
                            } else {
                                Notification.toast(LoginActivity.this, e.getLocalizedMessage());
                            }
                        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Notification.toast(LoginActivity.this, "Password does not meet minimum requirements");
                        } else if (e instanceof FirebaseAuthUserCollisionException) {
                            Notification.toast(LoginActivity.this, "A user already exists with this email - please login");
                        }
                        progressSpinnerDialog.showProgress(false);
                    }
                });
    }

    /**
     * Central sign in function
     */
    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Logging.debug(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            validateUserInfoDb(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Logging.warn(TAG, "signInWithEmail:failure" + task.getException());
                            progressSpinnerDialog.showProgress(false);
                            Notification.toast(LoginActivity.this, "Email/Password incorrect.\nClick Register to create a new account");
                        }
                    }
                });
    }

    /**
     * Todo extract this method to global scope
     */
    public FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    private void launchStudentPortal() {
        Intent myIntent = new Intent(LoginActivity.this, StudentMainActivity.class);
        // close current activity
        finish();
        // launch student portal
        LoginActivity.this.startActivity(myIntent);
    }

    private void launchStaffPortal() {
        Intent myIntent = new Intent(LoginActivity.this, StaffMainActivity.class);
        // close current activity
        finish();
        // launch staff portal
        LoginActivity.this.startActivity(myIntent);
    }

    private void getUserTypeAndInitUserDB() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseUser user = getCurrentUser();
                switch (which) {
                    case DialogInterface.BUTTON_NEGATIVE:
                        // Student button clicked
                        initUserDbInfo(user.getUid(), user.getEmail(), UserType.STUDENT);
                        break;
                    case DialogInterface.BUTTON_POSITIVE:
                        // Staff button clicked
                        initUserDbInfo(user.getUid(), user.getEmail(), UserType.STAFF);
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you staff or student?")
                .setPositiveButton("Staff", dialogClickListener)
                .setNegativeButton("Student", dialogClickListener)
                .show();
    }


    private Boolean validateForm(String userId, String password) {
        // Reset errors.
        mUserIdView.setError(null);
        mPasswordView.setError(null);

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid staff/student number.
        if (TextUtils.isEmpty(userId)) {
            mUserIdView.setError(getString(R.string.error_field_required));
            focusView = mUserIdView;
            cancel = true;
        } else if (!isEmailValid(userId)) {
            mUserIdView.setError(getString(R.string.error_invalid_user_id));
            focusView = mUserIdView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            progressSpinnerDialog.showProgress(true);
        }
        return !cancel;
    }
}
