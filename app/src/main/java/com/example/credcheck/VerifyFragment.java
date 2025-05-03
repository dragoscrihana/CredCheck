package com.example.credcheck;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

public class VerifyFragment extends Fragment {

    private ImageView qrImage;
    private Button generateQrButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_verify, container, false);

        qrImage = root.findViewById(R.id.qrImage);
        generateQrButton = root.findViewById(R.id.generateQrButton);

        generateQr(); // Generate QR immediately

        generateQrButton.setOnClickListener(v -> generateQr());

        return root;
    }

    private void generateQr() {
        String content = "https://credcheck.app/request?id=" + System.currentTimeMillis();

        QRCodeWriter writer = new QRCodeWriter();
        try {
            int size = 800;
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            var bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            qrImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
