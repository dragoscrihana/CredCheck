package com.example.credcheck;

import android.os.Bundle;
import android.view.*;
import android.widget.ListView;
import androidx.fragment.app.Fragment;

import com.example.credcheck.model.EventModel;
import com.example.credcheck.model.TransactionModel;

import java.util.*;

public class HistoryFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        ListView listView = root.findViewById(R.id.historyListView);

        List<TransactionModel> transactions = getDummyData();
        TransactionAdapter adapter = new TransactionAdapter(requireContext(), transactions);
        listView.setAdapter(adapter);

        return root;
    }

    private List<TransactionModel> getDummyData() {
        List<EventModel> events = new ArrayList<>();
        events.add(new EventModel("Transaction initialized", "Verifier", 1746291795075L, "request_uri: /wallet/request.jwt/..."));
        events.add(new EventModel("Request object retrieved", "Wallet", 1746291811098L, "jwt: eyJra..."));
        events.add(new EventModel("Wallet response posted", "Wallet", 1746291816994L, "vp_token: eyJhb..."));

        List<TransactionModel> transactions = new ArrayList<>();
        transactions.add(new TransactionModel("GJ8HUnJR4T...", "2025-05-03 14:56", events));
        transactions.add(new TransactionModel("GJ8HUnJR4T...", "2025-05-03 14:56", events));
        transactions.add(new TransactionModel("GJ8HUnJR4T...", "2025-05-03 14:56", events));
        transactions.add(new TransactionModel("GJ8HUnJR4T...", "2025-05-03 14:56", events));
        transactions.add(new TransactionModel("GJ8HUnJR4T...", "2025-05-03 14:56", events));
        transactions.add(new TransactionModel("GJ8HUnJR4T...", "2025-05-03 14:56", events));
        transactions.add(new TransactionModel("GJ8HUnJR4T...", "2025-05-03 14:56", events));
        transactions.add(new TransactionModel("GJ8HUnJR4T...", "2025-05-03 14:56", events));
        transactions.add(new TransactionModel("GJ8HUnJR4T...", "2025-05-03 14:56", events));
        transactions.add(new TransactionModel("GJ8HUnJR4T...", "2025-05-03 14:56", events));
        transactions.add(new TransactionModel("GJ8HUnJR4T...", "2025-05-03 14:56", events));
        transactions.add(new TransactionModel("GJ8HUnJR4T...", "2025-05-03 14:56", events));
        transactions.add(new TransactionModel("GJ8HUnJR4T...", "2025-05-03 14:56", events));
        transactions.add(new TransactionModel("GJ8HUnJR4T...", "2025-05-03 14:56", events));
        transactions.add(new TransactionModel("GJ8HUnJR4T...", "2025-05-03 14:56", events));
        transactions.add(new TransactionModel("GJ8HUnJR4T...", "2025-05-03 14:56", events));
        transactions.add(new TransactionModel("GJ8HUnJR4T...", "2025-05-03 14:56", events));
        return transactions;
    }
}
