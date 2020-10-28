package com.troop.freedcam.cameraui.fragment;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.troop.freedcam.cameraui.R;
import com.troop.freedcam.cameraui.databinding.CameraUiFragmentBinding;
import com.troop.freedcam.cameraui.viewmodels.CameraUiViewModel;

public class CameraUiFragment extends Fragment {

    private CameraUiViewModel mViewModel;
    private CameraUiFragmentBinding cameraUiFragmentBinding;

    public static CameraUiFragment newInstance() {
        return new CameraUiFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        cameraUiFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.camera_ui_fragment, container, false);
        cameraUiFragmentBinding.setCameraUiViewModel(mViewModel);
        return cameraUiFragmentBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(CameraUiViewModel.class);
        // TODO: Use the ViewModel
    }

}