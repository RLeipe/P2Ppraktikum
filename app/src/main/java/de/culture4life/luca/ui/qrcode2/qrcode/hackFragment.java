package de.culture4life.luca.ui.qrcode2.qrcode;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import de.culture4life.luca.BuildConfig;
import de.culture4life.luca.R;
import de.culture4life.luca.ui.BaseFragment;
import de.culture4life.luca.ui.ViewError;
import de.culture4life.luca.ui.dialog.BaseDialogFragment;
import de.culture4life.luca.ui.registration.RegistrationActivity;
import de.culture4life.luca.ui.registration.RegistrationTextInputLayout;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import timber.log.Timber;

public class hackFragment extends BaseFragment<hackViewModel> {


    private View loadingView;
    private MaterialButton createMeetingButton;

    private RegistrationTextInputLayout firstNameLayout;
    private RegistrationTextInputLayout lastNameLayout;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);


        firstNameLayout = view.findViewById(R.id.firstNameLayout);
        lastNameLayout = view.findViewById(R.id.lastNameLayout);

        loadingView = view.findViewById(R.id.loadingLayout);


        createMeetingButton = view.findViewById(R.id.createMeetingButton);

        return view;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_hack;
    }

    @Override
    protected Class<hackViewModel> getViewModelClass() {
        return hackViewModel.class;
    }

    @Override
    protected Completable initializeViews() {
        return super.initializeViews()
                .andThen(Completable.fromAction(() -> {

                    createMeetingButton.setOnClickListener(v -> readInput());


                    observe(viewModel.getIsLoading(), loading -> loadingView.setVisibility(loading ? View.VISIBLE : View.GONE));

                    observe(viewModel.getPrivateMeetingUrl(), privateMeetingUrl -> {
                        if (privateMeetingUrl != null) {
                            showJoinPrivateMeetingDialog(privateMeetingUrl);
                        }
                    });
                }));
    }

    private void readInput() {
        EditText editText = firstNameLayout.getEditText();
        editText.post(() -> editText.setSelection(editText.getText().length()));
        viewModel.doHack(editText.getText());
    }

    @Override
    public void onResume() {
        super.onResume();
        hideKeyboard();


    }







}
