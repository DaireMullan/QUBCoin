package uk.ac.qub.qubcoin;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import uk.ac.qub.qubcoin.logging.Logging;
import uk.ac.qub.qubcoin.models.Module;
import uk.ac.qub.qubcoin.models.User;

public class FirebaseDbHelper {

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private List<Module> modules = new ArrayList<Module>();
    private User user;
    private String userUid;
    private final static String TAG = FirebaseDbHelper.class.getName();

    public interface ModuleStatus {
        void dataIsLoaded(List<Module> modules, List<String> moduleIds);
    }

    public FirebaseDbHelper(){
        database = FirebaseDatabase.getInstance();
        userUid = FirebaseAuth.getInstance().getUid();
        databaseReference = database.getReference("users/" + userUid);
    }

    public void readModules(final ModuleStatus moduleStatus) {
        databaseReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    user = snapshot.getValue(User.class);
                    if (!user.modules.isEmpty()) {
                        databaseReference = database.getReference("modules");
                        databaseReference.addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                modules.clear();
                                List<String> moduleIds = new ArrayList<>();
                                String tempKey;
                                for (DataSnapshot moduleKey : snapshot.getChildren()) {
                                    tempKey = moduleKey.getKey();
                                    if (user.modules.containsKey(tempKey)) {
                                        moduleIds.add(tempKey);
                                        modules.add(moduleKey.getValue(Module.class));
                                    }
                                }
                                moduleStatus.dataIsLoaded(modules, moduleIds);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Logging.error(TAG, error.getMessage());
                            }
                        });
                    } else {
                        moduleStatus.dataIsLoaded(modules, new ArrayList<>());
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Logging.error(TAG, error.getMessage());
                moduleStatus.dataIsLoaded(modules, new ArrayList<>());
            }
        });
    }
}
