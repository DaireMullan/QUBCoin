package uk.ac.qub.qubcoin.qrcode;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.provider.MediaStore;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.ac.qub.qubcoin.exceptions.QrCodeException;
import uk.ac.qub.qubcoin.logging.Logging;

public class QrCode {

    private static final String TAG = QrCode.class.getName();
    private static final int DIMENSIONS = 1000;

    public static Bitmap getBitmap(String qrCodeId) throws QrCodeException {
        if (!isQrContentsValid(qrCodeId)) {
            throw new QrCodeException("Cannot create QR Code image with empty contents");
        }
        Bitmap bitmap = Bitmap.createBitmap(DIMENSIONS, DIMENSIONS, Bitmap.Config.ARGB_8888);
        MultiFormatWriter codeWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = codeWriter.encode(qrCodeId, BarcodeFormat.QR_CODE, DIMENSIONS, DIMENSIONS);
            for (int x = 0; x < DIMENSIONS; x++) {
                for (int y = 0; y < DIMENSIONS; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
        } catch (WriterException e) {
            String errorString = "Could not create QR code image ";
            Logging.error(TAG, errorString + e.getMessage());
            throw new QrCodeException(errorString);
        }
        return bitmap;
    }

    public static void writeBitmapToFs(File folder, String filename, Bitmap bitmap) throws QrCodeException {
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File filePath = new File(folder, filename);
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            String errorString = "Error writing QR code to file ";
            Logging.error(TAG, errorString + e.getMessage());
            throw new QrCodeException(errorString);
        }
    }

    public static ContentValues getContentValues(String filename, String filePath, String timestamp) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.DATE_ADDED, timestamp);
        values.put(MediaStore.MediaColumns.DATA, filePath);
        return values;
    }

    public static Boolean isQrContentsValid(String qrCodeId) {
        return qrCodeId.trim().length() > 0;
    }

    public static String getQrFilename(String timestamp) {
        return "QUBCoin_QR_" + timestamp + ".png";
    }

    public static String getTimestamp() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
        return simpleDateFormat.format(new Date());
    }
}
