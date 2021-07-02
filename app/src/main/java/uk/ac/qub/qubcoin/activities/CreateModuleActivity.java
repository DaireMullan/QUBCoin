package uk.ac.qub.qubcoin.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import uk.ac.qub.qubcoin.R;
import uk.ac.qub.qubcoin.logging.Logging;
import uk.ac.qub.qubcoin.models.Module;
import uk.ac.qub.qubcoin.notifications.Notification;
import uk.ac.qub.qubcoin.utilities.ProgressSpinner;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import static uk.ac.qub.qubcoin.validation.Validation.isCourseCodeValid;
import static uk.ac.qub.qubcoin.validation.Validation.isModuleCodeValid;
import static uk.ac.qub.qubcoin.validation.Validation.isModuleNameValid;
import static uk.ac.qub.qubcoin.utilities.HideKeyboard.hideKeyboard;

public class CreateModuleActivity extends AppCompatActivity {

    private final static String TAG = CreateModuleActivity.class.getName();
    private EditText moduleCodeView;
    private EditText courseCodeView;
    private EditText moduleNameView;
    private ProgressSpinner progressSpinnerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_module);

        moduleCodeView = (EditText) findViewById(R.id.module_code);
        courseCodeView = (EditText) findViewById(R.id.course_code);
        moduleNameView = (EditText) findViewById(R.id.module_name);
        // set the course code text to be all caps and a max of 4 chars long
        courseCodeView.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(4)});

        progressSpinnerDialog = new ProgressSpinner(this);
    }

    public void onClickCancel(View view) {
        hideKeyboard(this, view);
        progressSpinnerDialog.showProgress(false);
        finish();
    }

    public void onClickCreateModule(View view) {
        String moduleCode = moduleCodeView.getText().toString().trim();
        String moduleName = moduleNameView.getText().toString().trim();
        String courseCode = courseCodeView.getText().toString().trim();
        if(validateForm(courseCode, moduleCode, moduleName)) {
            hideKeyboard(this, view);
            createModule(courseCode, moduleCode, moduleName);
        } else {
            progressSpinnerDialog.showProgress(false);
        }
    }

    private void createModule(String courseCode, String moduleCode, String moduleName) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        String fullModuleCode = Module.combineModuleCodes(courseCode, moduleCode);
        Module module = new Module(fullModuleCode, moduleName, new HashMap<>());
        DatabaseReference dbRef = db.getReference("modules");
        dbRef.push().setValue(module).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                successfulCreation("Successfully created " + fullModuleCode);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String errorMessage = "Error creating module: " + e.getMessage();
                Logging.error(TAG, errorMessage);
                Notification.toast(CreateModuleActivity.this, errorMessage);
                progressSpinnerDialog.showProgress(false);
            }
        });
    }

    private void successfulCreation(String toastMessage) {
        Notification.toast(this, toastMessage);
        progressSpinnerDialog.showProgress(false);
        finish();
    }

    private Boolean validateForm(String courseCode, String moduleCode, String moduleName) {
        // Reset errors.
        moduleCodeView.setError(null);
        boolean cancel = false;
        View focusView = null;

        // Check for a valid module code, if the user entered one.
        if (TextUtils.isEmpty(moduleCode) || !isModuleCodeValid(moduleCode)) {
            moduleCodeView.setError(getString(R.string.error_invalid_module_code));
            focusView = moduleCodeView;
            cancel = true;
        }

        // Check for a valid module name, if the user entered one.
        if (TextUtils.isEmpty(moduleName) || !isModuleNameValid(moduleName)) {
            moduleNameView.setError(getString(R.string.error_invalid_module_name));
            focusView = moduleNameView;
            cancel = true;
        }

        // Check for a valid course code, if the user entered one.
        if (TextUtils.isEmpty(courseCode) || !isCourseCodeValid(courseCode)) {
            courseCodeView.setError(getString(R.string.error_invalid_course_code));
            focusView = courseCodeView;
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
