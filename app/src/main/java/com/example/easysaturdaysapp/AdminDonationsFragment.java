package com.example.easysaturdaysapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDonationsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_donations, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.adminDonationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ApiClient.getRetrofitInstance().create(ApiService.class).getAllDonations().enqueue(new Callback<List<AdminDonation>>() {
            @Override public void onResponse(Call<List<AdminDonation>> call, Response<List<AdminDonation>> response) {
                if(response.isSuccessful()) recyclerView.setAdapter(new DonationAdapter(response.body()));
            }
            @Override public void onFailure(Call<List<AdminDonation>> call, Throwable t) {}
        });
        return view;
    }

    class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.VH> {
        List<AdminDonation> list;
        DonationAdapter(List<AdminDonation> list) { this.list = list; }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false));
        }
        @Override public void onBindViewHolder(@NonNull VH holder, int position) {
            AdminDonation d = list.get(position);
            holder.text1.setText(d.donorName + " donated R" + (int)d.amount);
            holder.text2.setText("To: " + d.campaignTitle + " | " + d.date);
        }
        @Override public int getItemCount() { return list.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView text1, text2;
            VH(View v) { super(v); text1 = v.findViewById(android.R.id.text1); text2 = v.findViewById(android.R.id.text2); }
        }
    }
}