package com.example.credcheck.ui.verify;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;

import com.example.credcheck.util.PresentationDefinitionProvider;
import com.example.credcheck.R;
import com.example.credcheck.ui.main.MainActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class VerifyFragment extends Fragment {

    private ImageView qrImage;
    private Button generateQrButton;

    private final Handler pollHandler = new Handler();
    private String transactionId = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_verify, container, false);

        qrImage = root.findViewById(R.id.qrImage);
        generateQrButton = root.findViewById(R.id.generateQrButton);

        generateQr();
        generateQrButton.setOnClickListener(v -> generateQr());

        return root;
    }

    private void generateQr() {
        SharedPreferences prefs = requireContext().getSharedPreferences("credcheck_prefs", Context.MODE_PRIVATE);
        String accountType = prefs.getString("account_type", "restaurant");
        String payload = PresentationDefinitionProvider.getPresentationDefinition(accountType);

        new Thread(() -> {
            try {
                URL url = new URL("https://glowing-gradually-midge.ngrok-free.app/ui/presentations");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(payload.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    JSONObject responseJson = new JSONObject(result.toString());
                    String requestUri = responseJson.getString("requestUri");
                    transactionId = responseJson.getString("transactionId");
                    String requestId = requestUri.substring(requestUri.lastIndexOf("/") + 1);
                    String encodedRequestUri = "https%3A%2F%2Fglowing-gradually-midge.ngrok-free.app%2Fwallet%2Frequest.jwt%2F" + requestId;
                    String qrContent = "eudi-openid4vp://?client_id=Verifier&request_uri=" + encodedRequestUri;

                    Bitmap qrBitmap = generateQrBitmap(qrContent);
                    requireActivity().runOnUiThread(() -> qrImage.setImageBitmap(qrBitmap));

                    startPollingTransactionStatus();
                } else {
                    Log.e("VerifyFragment", "Server returned: " + responseCode);
                }
            } catch (Exception e) {
                Log.e("VerifyFragment", "QR generation failed", e);
            }
        }).start();
    }

    private void startPollingTransactionStatus() {
        pollHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (transactionId == null) return;

                new Thread(() -> {
                    try {
                        URL url = new URL("https://glowing-gradually-midge.ngrok-free.app/ui/presentations/" + transactionId + "/status");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");

                        int code = conn.getResponseCode();
                        if (code == HttpURLConnection.HTTP_OK) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            String status = reader.readLine();

                            if ("PENDING".equalsIgnoreCase(status.trim())) {
                                pollHandler.postDelayed(this, 2000);
                            } else {
                                Log.d("VerifyFragment", "Final status: " + status);
                                handleFinalStatus(status.trim());
                            }
                        } else {
                            Log.e("VerifyFragment", "Polling failed with HTTP " + code);
                            pollHandler.postDelayed(this, 2000);
                        }
                    } catch (Exception e) {
                        Log.e("VerifyFragment", "Polling error", e);
                        pollHandler.postDelayed(this, 2000);
                    }
                }).start();
            }
        }, 2000);
    }

    private void handleFinalStatus(String status) {
        requireActivity().runOnUiThread(() -> {
            saveTransactionStatus(transactionId, status);
            if ("ACCEPTED".equalsIgnoreCase(status)) {
                ((MainActivity) getActivity()).showOverlay(true);
                Log.d("VerifyFragment", "Overlay triggered with status: " + status);
            } else if ("DENIED".equalsIgnoreCase(status)) {
                ((MainActivity) getActivity()).showOverlay(false);
                Log.d("VerifyFragment", "Overlay triggered with status: " + status);
            }
        });
    }

    private Bitmap generateQrBitmap(String content) {
        try {
            int size = 800;
            QRCodeWriter writer = new QRCodeWriter();
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            var bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveTransactionStatus(String txId, String status) {
        SharedPreferences prefs = requireContext().getSharedPreferences("history", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(txId, status);
        editor.apply();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        pollHandler.removeCallbacksAndMessages(null);
    }
}
