package com.detectorcaidas.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.detectorcaidas.R;


public class Fragment_inicio extends Fragment {
    public static final String ARG_FRAG_INICIO = "Fragment_inicio";

    private ImageView imageView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View baseView = inflater.inflate(R.layout.layout_fragment_inicio, container, false);

        imageView = baseView.findViewById(R.id.image_fragInicio);
        int imageToLoad = getArguments() != null ? getArguments().getInt(ARG_FRAG_INICIO):0;
        imageView.setImageResource(imageToLoad);


        return baseView;
    }

}
