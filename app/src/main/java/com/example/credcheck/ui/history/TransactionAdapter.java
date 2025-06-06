package com.example.credcheck.ui.history;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.credcheck.R;
import com.example.credcheck.model.EventModel;
import com.example.credcheck.model.TransactionModel;

import java.util.List;

public class TransactionAdapter extends ArrayAdapter<TransactionModel> {

    private final LayoutInflater inflater;

    public TransactionAdapter(Context context, List<TransactionModel> transactions) {
        super(context, 0, transactions);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TransactionModel transaction = getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.transaction_item, parent, false);
        }

        TextView transactionIdView = convertView.findViewById(R.id.transactionId);
        TextView lastUpdatedView = convertView.findViewById(R.id.lastUpdated);
        TextView statusView = convertView.findViewById(R.id.status);
        LinearLayout eventsContainer = convertView.findViewById(R.id.eventsContainer);

        transactionIdView.setText(transaction.getTransactionId());
        lastUpdatedView.setText("Last updated: " + transaction.getLastUpdated());
        statusView.setText("Status: " + transaction.getStatus());

        convertView.setOnClickListener(v -> {
            transaction.setExpanded(!transaction.isExpanded());
            notifyDataSetChanged();
        });

        eventsContainer.removeAllViews();
        if (transaction.isExpanded()) {
            eventsContainer.setVisibility(View.VISIBLE);
            for (EventModel event : transaction.getEvents()) {
                View eventView = inflater.inflate(R.layout.event_item, eventsContainer, false);
                TextView eventTitle = eventView.findViewById(R.id.eventTitle);
                TextView eventDetails = eventView.findViewById(R.id.eventDetails);

                eventTitle.setText(event.getEvent() + " - " + event.getActor());

                eventView.setOnClickListener(view -> {
                    event.setExpanded(!event.isExpanded());
                    notifyDataSetChanged();
                });

                if (event.isExpanded()) {
                    eventDetails.setVisibility(View.VISIBLE);
                    eventDetails.setText("Timestamp: " + event.getTimestamp() + "\n" + event.getDetails());
                } else {
                    eventDetails.setVisibility(View.GONE);
                }

                eventsContainer.addView(eventView);
            }
        } else {
            eventsContainer.setVisibility(View.GONE);
        }

        return convertView;
    }
}
