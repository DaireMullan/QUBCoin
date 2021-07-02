package uk.ac.qub.qubcoin.recyclerviewadapters;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import uk.ac.qub.qubcoin.notifications.Notification;
import uk.ac.qub.qubcoin.R;
import uk.ac.qub.qubcoin.exceptions.QrCodeException;
import uk.ac.qub.qubcoin.logging.Logging;
import uk.ac.qub.qubcoin.models.QrDbObject;
import uk.ac.qub.qubcoin.qrcode.QrCode;

import static uk.ac.qub.qubcoin.qrcode.QrCode.getContentValues;
import static uk.ac.qub.qubcoin.qrcode.QrCode.getQrFilename;
import static uk.ac.qub.qubcoin.qrcode.QrCode.getTimestamp;
import static uk.ac.qub.qubcoin.qrcode.QrCode.writeBitmapToFs;

public class QrCodeRecyclerViewAdapter extends RecyclerView.Adapter<QrCodeRecyclerViewAdapter.QrCodeViewHolder> {

    private final static String TAG = QrCodeRecyclerViewAdapter.class.getName();
    private final List<QrDbObject> qrDbObjectList;
    private final List<String> qrCodeIds;

    public QrCodeRecyclerViewAdapter(HashMap<String, QrDbObject> qrCodes) {
        this.qrCodeIds = new ArrayList<>(qrCodes.keySet());
        this.qrDbObjectList = new ArrayList<QrDbObject>();
        for (int i = 0; i < qrCodeIds.size(); i++) {
            this.qrDbObjectList.add(qrCodes.get(this.qrCodeIds.get(i)));
        }
    }

    @Override
    public QrCodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_adapter_layout, parent, false);
        return new QrCodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(QrCodeViewHolder holder, final int position) {

        final QrDbObject qrDbObject = qrDbObjectList.get(position);
        holder.title.setText(qrDbObject.getDescription());
        holder.subtitle.setText(qrDbObject.getValue() + " QUBCoin");
        holder.secondarySubtitle.setText(qrDbObject.getTimestamp());
        holder.qrCodeId = qrCodeIds.get(position);
    }

    @Override
    public int getItemCount() {
        return qrDbObjectList.size();
    }

    public class QrCodeViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView subtitle;
        private final TextView secondarySubtitle;
        private final CardView cardView;
        private String qrCodeId;

        public QrCodeViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            cardView = itemView.findViewById(R.id.cardView);
            secondarySubtitle = itemView.findViewById(R.id.secondarySubtitle);
            secondarySubtitle.setVisibility(View.VISIBLE);
            itemView.findViewById(R.id.actionButtonContainer).setVisibility(View.GONE);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder qrCodeDialog = new AlertDialog.Builder(itemView.getContext());
                    LayoutInflater factory = LayoutInflater.from(itemView.getContext());
                    view = factory.inflate(R.layout.qr_code_view, null);
                    ImageView imageView = (ImageView) view.findViewById(R.id.qr_image_view);
                    Bitmap bitmap = null;
                    try {
                        bitmap = QrCode.getBitmap(qrCodeId);
                    } catch (QrCodeException e) {
                        Logging.error(TAG, e.getMessage());
                        Notification.toast(itemView.getContext(), e.getMessage());
                    }
                    imageView.setImageBitmap(bitmap);
                    qrCodeDialog.setView(view);
                    Bitmap finalBitmap = bitmap;

                    qrCodeDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).setPositiveButton("Download", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                downloadQr(finalBitmap);
                                String successMessage = "Your QR Code is available in your device's gallery!";
                                Notification.toast(itemView.getContext(), successMessage);
                            } catch (QrCodeException e) {
                                Notification.toast(itemView.getContext(), e.getMessage());
                            }
                        }
                    });
                    qrCodeDialog.show();
                }
            });
        }

        private void downloadQr(Bitmap bitmap) throws QrCodeException {
            String timestamp = getTimestamp();
            String filename = getQrFilename(timestamp);
            File folder = new File(Environment.getExternalStorageDirectory(), "/QUBCoin");
            writeBitmapToFs(folder, filename, bitmap);
            File filePath = new File(folder.getAbsolutePath(), filename);
            exportQrFileToAndroidGallery(getContentValues(filename, filePath.getAbsolutePath(), timestamp));
        }

        private void exportQrFileToAndroidGallery(ContentValues values) throws QrCodeException {
            try {
                itemView.getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } catch (Exception e) {
                String errorString = "Error when exporting QR code to Android gallery ";
                Logging.error(TAG, errorString + e.getMessage());
                throw new QrCodeException(errorString);
            }
        }
    }
}