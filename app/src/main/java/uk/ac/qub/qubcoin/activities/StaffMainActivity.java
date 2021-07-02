package uk.ac.qub.qubcoin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import uk.ac.qub.qubcoin.FirebaseDbHelper;
import uk.ac.qub.qubcoin.R;
import uk.ac.qub.qubcoin.logging.Logging;
import uk.ac.qub.qubcoin.models.Module;
import uk.ac.qub.qubcoin.models.QrDbObject;
import uk.ac.qub.qubcoin.models.UserType;
import uk.ac.qub.qubcoin.recyclerviewadapters.ViewModulesRecyclerViewAdapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class StaffMainActivity extends AppCompatActivity implements ItemClickListener {

    private static final String TAG = StaffMainActivity.class.getName();
    private List<Module> modules;
    private List<String> moduleCodes;
    private RecyclerView recyclerView;
    private TextView noModulesLayer;
    private ViewModulesRecyclerViewAdapter viewModulesRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logging.debug(TAG, "Instantiate Staff Main Dashboard");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_main);
        // initialize variables on page load
        AndroidNetworking.initialize(getApplicationContext());
        updateWelcomeString();

        modules = new ArrayList<>();
        moduleCodes = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewStudentModule);

        Logging.debug(TAG, "Reading staff user modules from Firebase");
        new FirebaseDbHelper().readModules(new FirebaseDbHelper.ModuleStatus() {
            @Override
            public void dataIsLoaded(List<Module> modules, List<String> moduleIds) {
                if(modules.size() > 0) {
                    viewModulesRecyclerViewAdapter =
                            new ViewModulesRecyclerViewAdapter(StaffMainActivity.this, modules, moduleIds, UserType.STAFF);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(StaffMainActivity.this);
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

    private String getCurrentUserEmail() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser.getEmail();
    }

    private void updateWelcomeString() {
        final TextView textView = (TextView) findViewById(R.id.textViewUserWelcomeEmail);
        String welcomeString = getCurrentUserEmail();
        textView.setText(welcomeString);
    }

    public void signOut(View view) {
        // sign out user and open login screen
        Logging.debug(TAG, "Staff user signing out.");
        FirebaseAuth.getInstance().signOut();
        Intent myIntent = new Intent(StaffMainActivity.this, LoginActivity.class);
        StaffMainActivity.this.startActivity(myIntent);
        finish();
    }

    @Override
    public void startQrCodeActivity(Module module) {
        Intent intent = new Intent(StaffMainActivity.this, ViewModuleActivity.class);
        HashMap<String, QrDbObject> qrCodeIds = module.getQrCodes();
        if(qrCodeIds != null) {
            intent.putExtra("QR_CODE_IDS", qrCodeIds);
        } else {
            intent.putExtra("QR_CODE_IDS", new HashMap<String, QrDbObject>()
            );
        }
        startActivity(intent);
    }

    @Override
    public void startCreateQrCodeActivity(String moduleId) {
        Intent intent = new Intent(StaffMainActivity.this, CreateQrCodeActivity.class);
        if(moduleId != null) {
            intent.putExtra("MODULE_ID", moduleId);
        }
        startActivity(intent);
    }

    public void startCreateModuleActivity(View view) {
        Intent intent = new Intent(StaffMainActivity.this, CreateModuleActivity.class);
        startActivity(intent);
    }

    public void startJoinModuleActivity(View view) {
        Intent intent = new Intent(StaffMainActivity.this, JoinModuleActivity.class);
        startActivity(intent);
    }
}

