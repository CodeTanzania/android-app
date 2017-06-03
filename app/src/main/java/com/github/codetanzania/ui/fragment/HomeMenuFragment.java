package com.github.codetanzania.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.codetanzania.CivilianFeedback;
import com.github.codetanzania.adapter.HomeMenuItemAdapter;
import com.github.codetanzania.ui.HomeMenu;

import tz.co.codetanzania.R;

public class HomeMenuFragment extends Fragment {

    private HomeMenu.OnClickListener mListener;

    @Override
    public void onAttach(Context mContext) {
        super.onAttach(mContext);
        mListener = (HomeMenu.OnClickListener) mContext;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_home_menu, group, false);
    }

    @Override
    public void onViewCreated(View fragView, Bundle savedInstanceStete) {
        HomeMenu homeMenu = ((CivilianFeedback)getActivity().getApplication()).getHomeMenu();
        bind(homeMenu, fragView);
    }

    private void bind(HomeMenu homeMenu, View fragView) {
        RecyclerView rvHomeMenuItems = (RecyclerView) fragView.findViewById(R.id.rv_MenuItems);
        HomeMenuItemAdapter adapter = new HomeMenuItemAdapter(getActivity(), homeMenu, mListener);
        StaggeredGridLayoutManager lytManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        rvHomeMenuItems.setLayoutManager(lytManager);
        rvHomeMenuItems.setAdapter(adapter);
        rvHomeMenuItems.setHasFixedSize(true);
    }
}