package com.example.credcheck.ui.verify;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.credcheck.R;
import com.example.credcheck.util.AuthManager;
import com.example.credcheck.ui.main.MainActivity;
import com.example.credcheck.util.PresentationDefinitionProvider;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class VerifyFragment extends Fragment {

    private ImageView qrImage;
    private Button generateQrButton;
    TextView attributeLabel;
    private RadioGroup attributeRadioGroup;

    private final Handler pollHandler = new Handler();
    private String transactionId = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_verify, container, false);

        qrImage = root.findViewById(R.id.qrImage);
        generateQrButton = root.findViewById(R.id.generateQrButton);

        attributeLabel = root.findViewById(R.id.attributeLabel);

        attributeRadioGroup = root.findViewById(R.id.attributeRadioGroup);
        setupAttributeRadioButtons();

        generateQrButton.setOnClickListener(v -> generateQr());

        return root;
    }

    private void generateQr() {
        AuthManager.getFreshAccessToken(requireContext(), (accessToken, authState) -> {
            if (accessToken == null) {
                Log.e("VerifyFragment", "Access token is null, cannot proceed.");
                return;
            }

            String accountType = "restaurant";
            try {
                String[] parts = accessToken.split("\\.");
                if (parts.length >= 2) {
                    String payloadBase64 = parts[1];
                    byte[] decoded = android.util.Base64.decode(payloadBase64, android.util.Base64.URL_SAFE | android.util.Base64.NO_WRAP | android.util.Base64.NO_PADDING);
                    String payloadJson = new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
                    JSONObject payloadObj = new JSONObject(payloadJson);
                    accountType = payloadObj.optString("account_type", "restaurant");
                }
            } catch (Exception e) {
                Log.e("VerifyFragment", "Failed to extract account_type", e);
            }

            String nonce = UUID.randomUUID().toString();

            String payload = null;
            try {
                payload = new JSONObject()
                        .put("type", "vp_token")
                        .put("nonce", nonce)
                        .put("request_uri_method", "get")
                        .toString();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            String finalPayload = payload;

            int selectedId = attributeRadioGroup.getCheckedRadioButtonId();
            String selectedAttribute = "birthdate";

            if (selectedId != -1) {
                RadioButton selectedRadio = attributeRadioGroup.findViewById(selectedId);
                selectedAttribute = selectedRadio.getText().toString();
            }

            String finalAttribute = selectedAttribute;

            new Thread(() -> {
                try {

                    URL url = new URL("https://backend.credcheck.site/ui/presentations/" + finalAttribute);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                    conn.setDoOutput(true);

                    OutputStream os = conn.getOutputStream();
                    os.write(finalPayload.getBytes());
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
                        String encodedRequestUri = "https%3A%2F%2Fbackend.credcheck.site%2Fwallet%2Frequest.jwt%2F" + requestId;
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
        });
    }

    private void startPollingTransactionStatus() {
        pollHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (transactionId == null) return;

                AuthManager.getFreshAccessToken(requireContext(), (accessToken, authState) -> {
                    if (accessToken == null) {
                        Log.e("VerifyFragment", "Access token null during polling.");
                        pollHandler.postDelayed(this, 2000);
                        return;
                    }

                    new Thread(() -> {
                        try {
                            URL url = new URL("https://backend.credcheck.site/ui/presentations/" + transactionId + "/status");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

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
                });
            }
        }, 2000);
    }

    private void setupAttributeRadioButtons() {
        AuthManager.getFreshAccessToken(requireContext(), (accessToken, authState) -> {
            if (accessToken == null) return;

            String accountType = "restaurant";
            try {
                String[] parts = accessToken.split("\\.");
                if (parts.length >= 2) {
                    String payloadBase64 = parts[1];
                    byte[] decoded = android.util.Base64.decode(payloadBase64, android.util.Base64.URL_SAFE | android.util.Base64.NO_WRAP | android.util.Base64.NO_PADDING);
                    String payloadJson = new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
                    JSONObject payloadObj = new JSONObject(payloadJson);
                    accountType = payloadObj.optString("account_type", "restaurant");
                }
            } catch (Exception e) {
                Log.e("VerifyFragment", "Failed to extract account_type", e);
            }

            String finalAccountTyoe = accountType;

            new Thread(() -> {
                try {
                    URL url = new URL("https://backend.credcheck.site/ui/feature-options/" + finalAccountTyoe);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Authorization", "Bearer " + accessToken);

                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }

                        org.json.JSONArray attributes = new org.json.JSONArray(result.toString());

                        requireActivity().runOnUiThread(() -> {
                            attributeRadioGroup.removeAllViews();

                            for (int i = 0; i < attributes.length(); i++) {
                                try {
                                    String attr = attributes.getString(i);
                                    RadioButton rb = new RadioButton(requireContext());
                                    rb.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#888888")));
                                    rb.setText(attr);
                                    rb.setId(View.generateViewId());

                                    if (i == 0) {
                                        rb.setChecked(true);
                                    }

                                    rb.setOnClickListener(v -> generateQr());
                                    attributeRadioGroup.addView(rb);
                                } catch (JSONException e) {
                                    Log.e("VerifyFragment", "Error parsing attribute", e);
                                }
                            }

                            if (attributes.length() > 1) {
                                attributeLabel.setVisibility(View.VISIBLE);
                                attributeRadioGroup.setVisibility(View.VISIBLE);
                            }
                            generateQr();
                        });
                    }
                } catch (Exception e) {
                    Log.e("VerifyFragment", "Failed to fetch attributes", e);
                }
            }).start();
        });
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
        var prefs = requireContext().getSharedPreferences("history", Context.MODE_PRIVATE);
        prefs.edit().putString(txId, status).apply();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        pollHandler.removeCallbacksAndMessages(null);
    }
}
