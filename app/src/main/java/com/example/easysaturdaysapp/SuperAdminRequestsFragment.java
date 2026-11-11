package com.example.easysaturdaysapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class SuperAdminRequestsFragment extends Fragment {
    RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_super_requests, container, false);
        recyclerView = view.findViewById(R.id.requestsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadRequests();
        return view;
    }

    private void loadRequests() {
        ApiClient.getRetrofitInstance().create(ApiService.class).getAdminRequests().enqueue(new Callback<List<AdminRequest>>() {
            @Override
            public void onResponse(Call<List<AdminRequest>> call, Response<List<AdminRequest>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    // Filter to only show Pending requests
                    List<AdminRequest> pending = response.body();

                    // FIX: Safely check if status is not null before comparing
                    pending.removeIf(r -> r.status == null || !r.status.equalsIgnoreCase("Pending"));

                    recyclerView.setAdapter(new RequestAdapter(pending));
                }
            }
            @Override public void onFailure(Call<List<AdminRequest>> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load requests", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.VH> {
        List<AdminRequest> list;
        RequestAdapter(List<AdminRequest> list) { this.list = list; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_super_request, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            AdminRequest r = list.get(position);

            // FIX: Use the requestedByUserId since we removed the nested User object
            holder.tvRequester.setText("Requested by User ID: " + r.requestedByUserId);

            if(r.actionType != null && r.actionType.equals("CreateAdmin")) {
                holder.tvAction.setText("Action: Create new Admin (" + r.newAdminEmail + ")");
            } else {
                holder.tvAction.setText("Action: Delete Admin (ID: " + r.targetAdminId + ")");
            }

            holder.btnApprove.setOnClickListener(v -> processRequest(r.id, true));
            holder.btnReject.setOnClickListener(v -> processRequest(r.id, false));
        }

        @Override public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvRequester, tvAction;
            Button btnApprove, btnReject;
            VH(View v) {
                super(v);
                tvRequester = v.findViewById(R.id.tvRequester);
                tvAction = v.findViewById(R.id.tvAction);
                btnApprove = v.findViewById(R.id.btnApprove);
                btnReject = v.findViewById(R.id.btnReject);
            }
        }
    }

    private void processRequest(int requestId, boolean isApproved) {
        ProcessRequest req = new ProcessRequest();
        req.requestId = requestId;
        req.isApproved = isApproved;

        ApiClient.getRetrofitInstance().create(ApiService.class).processAdminRequest(req).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if(response.isSuccessful()) {
                    Toast.makeText(getContext(), isApproved ? "Approved!" : "Rejected!", Toast.LENGTH_SHORT).show();
                    loadRequests(); // Refresh the list
                } else {
                    Toast.makeText(getContext(), "Failed to process request", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}