package com.crossfitarmyjym.app.ui.progress;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.crossfitarmyjym.app.R;
import com.crossfitarmyjym.app.data.model.PersonalRecord;
import com.crossfitarmyjym.app.data.model.Result;

import java.util.List;

public class AthleteProgressDialog {

    private final Context context;
    private final AlertDialog dialog;
    private final TextView bestsView;
    private final TextView historyView;
    private final TextView wodResultsView;

    public AthleteProgressDialog(@NonNull Context context, @NonNull String title) {
        this.context = context;

        ScrollView scrollView = new ScrollView(context);
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(18);
        content.setPadding(padding, padding / 2, padding, 0);
        scrollView.addView(content);

        bestsView = addProgressSection(content, context.getString(R.string.client_progress_bests));
        historyView = addProgressSection(content, context.getString(R.string.client_progress_pr_history));
        wodResultsView = addProgressSection(content, context.getString(R.string.client_progress_wod_results));

        dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(scrollView)
                .setPositiveButton(android.R.string.ok, null)
                .create();
        setLoading();
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public boolean isShowing() {
        return dialog.isShowing();
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        dialog.setOnDismissListener(listener);
    }

    public void setLoading() {
        bestsView.setText(R.string.loading);
        historyView.setText(R.string.loading);
        wodResultsView.setText(R.string.loading);
    }

    public void setBests(List<PersonalRecord> records) {
        bestsView.setText(ProgressFormatter.personalRecords(context, records));
    }

    public void setHistory(List<PersonalRecord> records) {
        historyView.setText(ProgressFormatter.personalRecords(context, records));
    }

    public void setWodResults(List<Result> results) {
        wodResultsView.setText(ProgressFormatter.wodResults(context, results));
    }

    private TextView addProgressSection(@NonNull LinearLayout parent, @NonNull String title) {
        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextColor(ContextCompat.getColor(context, R.color.army_navy));
        titleView.setTextSize(17);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setPadding(0, dp(12), 0, 0);
        parent.addView(titleView);

        View divider = new View(context);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(dp(48), dp(3));
        dividerParams.topMargin = dp(7);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(ContextCompat.getColor(context, R.color.army_red));
        parent.addView(divider);

        TextView bodyView = new TextView(context);
        bodyView.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
        bodyView.setTextSize(14);
        bodyView.setLineSpacing(4, 1);
        LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        bodyParams.topMargin = dp(12);
        bodyView.setLayoutParams(bodyParams);
        parent.addView(bodyView);
        return bodyView;
    }

    private int dp(int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }
}
