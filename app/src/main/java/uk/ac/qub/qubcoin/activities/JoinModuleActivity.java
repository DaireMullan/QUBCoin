package uk.ac.qub.qubcoin.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import uk.ac.qub.qubcoin.R;
import uk.ac.qub.qubcoin.logging.Logging;
import uk.ac.qub.qubcoin.models.Module;
import uk.ac.qub.qubcoin.recyclerviewadapters.JoinModuleRecyclerViewAdapter;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class JoinModuleActivity extends AppCompatActivity {

    private static final String TAG = JoinModuleActivity.class.getName();
    private FirebaseDatabase database;
    private JoinModuleRecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView recyclerView;
    private List<Module> modules;
    private List<String> moduleIds;
    private View backgroundLayer;
    private View mainLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_module);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewJoinModule);
        mainLayer = (ConstraintLayout) findViewById(R.id.join_module_main_layer);
        backgroundLayer = (LinearLayout) findViewById(R.id.join_background_layer);

        database = FirebaseDatabase.getInstance();
        loadAllModules();
    }

    private void loadAllModules() {
        DatabaseReference dbRef = database.getReference("/modules");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // get modules and module ids out of snapshot
                modules = new ArrayList<Module>();
                moduleIds = new ArrayList<>();
                for (DataSnapshot module : snapshot.getChildren()) {
                    Module m = module.getValue(Module.class);
                    modules.add(m);
                    moduleIds.add(module.getKey());
                }
                if (moduleIds.size() <= 0) {
                    showBackground(true);
                } else {
                    showBackground(false);
                    recyclerViewAdapter = new JoinModuleRecyclerViewAdapter(modules, moduleIds);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(JoinModuleActivity.this);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setAdapter(recyclerViewAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Logging.error(TAG, "Could not load modules from firebase: " + error.getMessage());
                showBackground(true);
            }
        });
    }

    private void showBackground(Boolean show) {
        if (show) {
            backgroundLayer.setVisibility(VISIBLE);
            mainLayer.setVisibility(GONE);
        } else {
            backgroundLayer.setVisibility(GONE);
            mainLayer.setVisibility(VISIBLE);
        }
    }

    public void onClickCancel(View view) {
        finish();
    }
}