package com.pingidentity.sdk.pingoneverify.sample;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.pingidentity.sdk.pingoneverify.PingOneVerifyClient;
import com.pingidentity.sdk.pingoneverify.errors.DocumentSubmissionError;
import com.pingidentity.sdk.pingoneverify.listeners.DocumentSubmissionListener;
import com.pingidentity.sdk.pingoneverify.models.DocumentSubmissionResponse;
import com.pingidentity.sdk.pingoneverify.models.DocumentSubmissionStatus;
import com.pingidentity.sdk.pingoneverify.settings.ButtonAppearance;
import com.pingidentity.sdk.pingoneverify.settings.UIAppearanceSettings;

public class MainFragment extends Fragment implements DocumentSubmissionListener {

    public static final String TAG = MainFragment.class.getCanonicalName();

    private Button mBtnVerify;
    private View waitOverlay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBtnVerify = view.findViewById(R.id.btn_verify);
        waitOverlay = view.findViewById(R.id.wait_spinner);
        mBtnVerify.setOnClickListener(mView -> initPingOneClient());
    }

    @Override
    public void onDocumentSubmitted(DocumentSubmissionResponse response) {
        Log.i("onDocumentSubmitted", response.toString());
    }

    @Override
    public void onSubmissionComplete(DocumentSubmissionStatus status) {
        setInProgress(false);
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        CompletedFragment completedFragment = new CompletedFragment();
        fragmentTransaction.replace(R.id.frame_layout, completedFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onSubmissionError(DocumentSubmissionError error) {
        Log.e(TAG, error.getMessage() != null ? error.getMessage() : "Unknown Error");
        showAlert("Document Submission Error", error.getLocalizedMessage());
        setInProgress(false);
    }

    private void initPingOneClient() {
        waitOverlay.setVisibility(View.VISIBLE);

        new PingOneVerifyClient.Builder(false)
                .setRootActivity(getActivity())
                .setListener(this)
//                .setUIAppearance(this.getUiAppearanceSettings())
                .startVerification(new PingOneVerifyClient.Builder.BuilderCallback() {
                    @Override
                    public void onSuccess(PingOneVerifyClient client) {
                        Log.d("initPingOneClient", "success");
                        setInProgress(true);
                        waitOverlay.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        if (errorMessage != null) {
                            showAlert("Client Builder Error", errorMessage);
                        }
                        setInProgress(false);
                        waitOverlay.setVisibility(View.GONE);
                    }
                });
    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .create()
                .show();
    }

    private void setInProgress(boolean inProgress) {
        mBtnVerify.setClickable(!inProgress);
        mBtnVerify.setAlpha(inProgress ? 0.4f : 1f);
    }

}