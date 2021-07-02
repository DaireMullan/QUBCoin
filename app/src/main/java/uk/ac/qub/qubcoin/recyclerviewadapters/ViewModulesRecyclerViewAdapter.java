package uk.ac.qub.qubcoin.recyclerviewadapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import androidx.cardview.widget.CardView;
import uk.ac.qub.qubcoin.R;
import uk.ac.qub.qubcoin.activities.ItemClickListener;
import uk.ac.qub.qubcoin.logging.Logging;
import uk.ac.qub.qubcoin.models.Module;
import androidx.recyclerview.widget.RecyclerView;
import uk.ac.qub.qubcoin.models.UserType;

public class ViewModulesRecyclerViewAdapter extends RecyclerView.Adapter<ViewModulesRecyclerViewAdapter.ModuleViewHolder> {

    private final static String TAG = ViewModulesRecyclerViewAdapter.class.getName();
    private List<Module> moduleList;
    private List<String> moduleIds;
    private ItemClickListener listener;
    private UserType userType;

    public ViewModulesRecyclerViewAdapter(Context context, List<Module> moduleList, List<String> moduleIds, UserType userType) {
        this.moduleList = moduleList;
        this.moduleIds = moduleIds;
        this.userType = userType;
        if(userType == UserType.STAFF) {
            this.listener = (ItemClickListener) context;
        }
    }

    @Override
    public ModuleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_adapter_layout, parent, false);
        return new ModuleViewHolder(view, userType);
    }

    @Override
    public void onBindViewHolder(ModuleViewHolder holder, final int position) {
        final Module module = moduleList.get(position);
        holder.title.setText(module.getCode());
        holder.subtitle.setText(module.getName());
        if (this.userType == UserType.STAFF) {
            holder.viewQrCodeButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.startQrCodeActivity(module);
                    } else {
                        Logging.error(TAG, "Error loading QR codes; null listener");
                    }
                }
            });

            holder.createQrCodeButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.startCreateQrCodeActivity(moduleIds.get(position));
                    } else {
                        Logging.error(TAG, "Error loading module id; null listener");
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return moduleList.size();
    }

    public class ModuleViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView subtitle;
        private CardView cardView;
        private Button createQrCodeButton;
        private Button viewQrCodeButton;
        private View buttonContainer;

        public ModuleViewHolder(View itemView, UserType userType) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            cardView = itemView.findViewById(R.id.cardView);
            createQrCodeButton = itemView.findViewById(R.id.buttonCreateQrCode);
            viewQrCodeButton = itemView.findViewById(R.id.buttonViewQrCodes);
            if (userType == UserType.STUDENT) {
                buttonContainer = itemView.findViewById(R.id.actionButtonContainer);
                buttonContainer.setVisibility(View.GONE);
            }
        }
    }
}