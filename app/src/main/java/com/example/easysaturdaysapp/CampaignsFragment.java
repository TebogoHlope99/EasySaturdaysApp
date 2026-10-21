package com.example.easysaturdaysapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CampaignsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_campaigns, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.campaignsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ApiService api = ApiClient.getRetrofitInstance().create(ApiService.class);
        api.getCampaigns().enqueue(new Callback<List<Campaign>>() {
            @Override
            public void onResponse(Call<List<Campaign>> call, Response<List<Campaign>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    recyclerView.setAdapter(new CampaignAdapter(response.body()));
                }
            }
            @Override
            public void onFailure(Call<List<Campaign>> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load campaigns", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    // Adapter copied from CampaignsActivity
    class CampaignAdapter extends RecyclerView.Adapter<CampaignAdapter.VH> {
        List<Campaign> list;
        CampaignAdapter(List<Campaign> list) { this.list = list; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_campaign, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Campaign c = list.get(position);
            holder.title.setText(c.title);
            holder.desc.setText(c.description);
            holder.stats.setText("Raised: R" + (int)c.raisedAmount + " / Goal: R" + (int)c.goalAmount);
            int percent = 0;
            if (c.goalAmount > 0) percent = (int) Math.min(100, (c.raisedAmount / c.goalAmount) * 100);
            holder.progress.setProgress(percent);
        }

        @Override
        public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView title, desc, stats;
            ProgressBar progress;
            VH(View v) {
                super(v);
                title = v.findViewById(R.id.tvTitle);
                desc = v.findViewById(R.id.tvDescription);
                stats = v.findViewById(R.id.tvStats);
                progress = v.findViewById(R.id.progressBar);
            }
        }
    }
}