package uk.ac.qub.qubcoin.recyclerviewadapters;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import uk.ac.qub.qubcoin.R;
import uk.ac.qub.qubcoin.activities.JoinModuleActivity;
import uk.ac.qub.qubcoin.logging.Logging;
import uk.ac.qub.qubcoin.models.Module;
import uk.ac.qub.qubcoin.notifications.Notification;

public class JoinModuleRecyclerViewAdapter extends RecyclerView.Adapter<JoinModuleRecyclerViewAdapter.ModuleViewHolder> {

    private final static String TAG = JoinModuleActivity.class.getName();
    private final List<Module> modules;
    private final List<String> moduleIds;

    public JoinModuleRecyclerViewAdapter(List<Module> modules, List<String> moduleIds) {
        this.modules = modules;
        this.moduleIds = moduleIds;
    }

    @Override
    public JoinModuleRecyclerViewAdapter.ModuleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_adapter_layout, parent, false);
        return new JoinModuleRecyclerViewAdapter.ModuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(JoinModuleRecyclerViewAdapter.ModuleViewHolder holder, final int position) {
        final Module module = modules.get(position);
        holder.title.setText(module.getCode());
        holder.subtitle.setText(module.getName());
        holder.moduleId = moduleIds.get(position);
    }

    @Override
    public int getItemCount() {
        return modules.size();
    }

    public class ModuleViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView subtitle;
        private String moduleId;

        public ModuleViewHolder(View itemView) {
            super(itemView);
            this.title = itemView.findViewById(R.id.title);
            this.subtitle = itemView.findViewById(R.id.subtitle);
            CardView cardView = itemView.findViewById(R.id.cardView);
            itemView.findViewById(R.id.actionButtonContainer).setVisibility(View.GONE);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchDialog();
                }
            });
        }

        private void launchDialog() {
            AlertDialog.Builder joinModuleDialog = new AlertDialog.Builder(itemView.getContext());
            joinModuleDialog.setMessage("Are you sure you want to join this module?");
            joinModuleDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).setPositiveButton("Join Module", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    addModuleToUser(moduleId);
                    String successMsg = "Successfully added user to module.";
                    Logging.debug(TAG, successMsg);
                    Notification.toast(itemView.getContext(), successMsg);
                }
            });
            joinModuleDialog.show();
        }

    }

    private static void addModuleToUser(String moduleId) {
        String userUid = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = db.getReference("users").child(userUid);
        dbRef.child("modules").child(moduleId).setValue(true);
    }
}
