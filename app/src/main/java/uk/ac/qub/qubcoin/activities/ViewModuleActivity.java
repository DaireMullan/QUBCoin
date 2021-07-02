package uk.ac.qub.qubcoin.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import uk.ac.qub.qubcoin.notifications.Notification;
import uk.ac.qub.qubcoin.R;
import uk.ac.qub.qubcoin.logging.Logging;
import uk.ac.qub.qubcoin.models.QrDbObject;
import uk.ac.qub.qubcoin.recyclerviewadapters.QrCodeRecyclerViewAdapter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.LinearLayout;

import java.util.HashMap;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ViewModuleActivity extends AppCompatActivity {

    private static final String TAG = ViewModuleActivity.class.getName();
    private static final String QR_CODE_INTENT_NAME = "QR_CODE_IDS";
    private static final int STORAGE_PERMISSION = 1;
    private HashMap<String, QrDbObject> qrCodes;
    private RecyclerView recyclerView;
    private LinearLayout noQrCodeLayer;
    private QrCodeRecyclerViewAdapter recyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_module);
        qrCodes = (HashMap<String, QrDbObject>) getIntent().getSerializableExtra(QR_CODE_INTENT_NAME);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewStudentModule);

        if (qrCodes.size() == 0) {
            noQrCodeLayer = (LinearLayout) findViewById(R.id.nothingToDisplayLayer);
            noQrCodeLayer.setVisibility(VISIBLE);
            recyclerView.setVisibility(GONE);
        } else {
            if (isStoragePermission()) {
                setupActivity();
            } else {
                requestStoragePermission();
            }
        }
    }

    private void setupActivity() {
        recyclerViewAdapter = new QrCodeRecyclerViewAdapter(qrCodes);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ViewModuleActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private Boolean isStoragePermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                STORAGE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION) {
            if (isStoragePermission()) {
                setupActivity();
            } else {
                String errorString = "QUBCoin needs permission to write QR codes to device";
                Notification.toast(ViewModuleActivity.this, errorString);
                Logging.error(TAG, errorString);
                ViewModuleActivity.this.finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}