package com.github.codetanzania.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.codetanzania.Constants;
import com.github.codetanzania.adapter.Open311ServiceAdapter;
import com.github.codetanzania.event.ClickListener;
import com.github.codetanzania.event.RecyclerViewTouchListener;
import com.github.codetanzania.api.model.Open311Service;

import java.util.List;

import tz.co.codetanzania.R;

public class ServiceSelectorFragment extends Fragment {

    private static final String TAG = "ServiceSelectionFrag";

    private static final String SERVICE_ID = "_idOpen311";

    public interface OnSelectOpen311Service {
        void onOpen311ServiceSelected(Open311Service open311Service);
    }

    private Open311ServiceAdapter mAdapter;

    private OnSelectOpen311Service mOnSelectService;

    private int mSelectedServiceIndex;

    public static ServiceSelectorFragment getNewInstance(Bundle args) {
        ServiceSelectorFragment frag = new ServiceSelectorFragment();
        frag.setArguments(args);
        return frag;
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            // restore the fragment state
            // [1] restore the item which was previously selected.
            mSelectedServiceIndex = savedInstanceState.getInt(SERVICE_ID, 0);
        } else {
            mSelectedServiceIndex = 0;
        }
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // check if user has selected service
        mSelectedServiceIndex = mAdapter.getSelectedItemIndex();
        if (mSelectedServiceIndex != -1) {
            outState.putInt(SERVICE_ID, mSelectedServiceIndex);
        }
    }

    @Override public View onCreateView(
            LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_service_selection, group, false);
    }

    @Override public void onViewCreated(View fragView, Bundle savedInstanceState) {
        // bind data to the view
        bindView(fragView);
    }

    @Override public void onAttach(Context ctx) {
        super.onAttach(ctx);
        try {
            mOnSelectService = (OnSelectOpen311Service) ctx;
        } catch (ClassCastException e) {
            throw new ClassCastException(ctx.toString() + " must implement OnSelectService");
        }
    }

    @Override public void onDetach() {
        super.onDetach();
        mOnSelectService = null;
    }

    private void bindView(View fragView) {
        List<Open311Service> mOpen311ServicesList = getArguments().getParcelableArrayList(Constants.Const.SERVICE_LIST);
        // recycler view
        RecyclerView mRecyclerView = (RecyclerView) fragView.findViewById(R.id.rv_Services);
        // adapter
        mAdapter = new Open311ServiceAdapter(getActivity(), mOpen311ServicesList);
        // layout manager
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        // setup recycler view
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter.toggleSelectedItemIndex(mSelectedServiceIndex);

        RecyclerViewTouchListener tListener = new RecyclerViewTouchListener(getActivity(), mRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                // select item at a given position
                mAdapter.toggleSelectedItemIndex(position);
            }

            @Override
            public void onLongClick(View view, int position) {
                // long press allows user to capture picture and fill in information using dialogs
            }
        });

        // add decorations --- API 23+ add support for default dividers
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        // respond to touch event. Basically, we're using
        // gestures to advance cause of action
        mRecyclerView.addOnItemTouchListener(tListener);

        // attach next event listener to button
        attachNextEvent(fragView.findViewById(R.id.btn_OpenIssue));
    }

    private void attachNextEvent(View button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdapter.getSelectedItem() == null) {
                    Toast.makeText(getActivity(), "Please, Select Service", Toast.LENGTH_SHORT).show();
                } else {
                    mOnSelectService.onOpen311ServiceSelected(mAdapter.getSelectedItem());
                }
            }
        });
    }
}
