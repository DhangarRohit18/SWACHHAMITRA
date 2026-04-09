package com.example.civsetu.surveys;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.civsetu.databinding.ActivitySurveyBinding;
import com.example.civsetu.databinding.ItemSurveyBinding;
import com.example.civsetu.model.Survey;
import java.util.ArrayList;
import java.util.List;

public class SurveyActivity extends AppCompatActivity {
    private ActivitySurveyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySurveyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.rvSurveys.setLayoutManager(new LinearLayoutManager(this));
        // Mock data for demo if repository isn't fully wired for surveys yet
        List<Survey> mockSurveys = new ArrayList<>();
        // Add mock surveys
        binding.rvSurveys.setAdapter(new SurveyAdapter(mockSurveys));
    }

    static class SurveyAdapter extends RecyclerView.Adapter<SurveyAdapter.ViewHolder> {
        private List<Survey> surveys;

        SurveyAdapter(List<Survey> surveys) {
            this.surveys = surveys;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemSurveyBinding binding = ItemSurveyBinding.inflate(LayoutInflater.from(parent.getContext()), parent,
                    false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Survey survey = surveys.get(position);
            holder.binding.tvTitle.setText(survey.getTitle());
            holder.binding.tvDescription.setText(survey.getDescription());
        }

        @Override
        public int getItemCount() {
            return surveys.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ItemSurveyBinding binding;

            ViewHolder(ItemSurveyBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
