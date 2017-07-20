package com.github.codetanzania.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.codetanzania.api.model.Open311Service;

import java.util.ArrayList;
import java.util.List;

import tz.co.codetanzania.R;

public class Open311ServiceAdapter extends RecyclerView.Adapter<Open311ServiceAdapter.ViewHolder> {

    private static final String TAG = "Open311ServiceAdapter";

    private final Context mContext;
    private final List<Open311Service> mOpen311Services;
    private final List<Boolean> mCheckList;
    private Open311Service mSelection;

    // reference to the view holder
    private ViewHolder mViewHolder;

    public Open311ServiceAdapter(Context ctx, List<Open311Service> open311Services) {
        this.mContext = ctx;
        // the service
        this.mOpen311Services = open311Services;
        // make an eqi-length list of bools
        this.mCheckList = new ArrayList<>(mOpen311Services.size());
        // fill it with false
        // Collections.fill(this.mCheckList, false);
        for (int i = 0; i < open311Services.size(); ++i) {
            this.mCheckList.add(false);
        }
    }

    public void toggleSelectedItemIndex(int index) {
        // this.mSelection = this.mOpen311Services.get(index);
        // state
        boolean selected = this.mCheckList.get(index);

        this.mSelection = !selected ? mOpen311Services.get(index) : null;

        // toggle item at a given position
        this.mCheckList.set(index, !selected);
        for (int i = 0; i < mCheckList.size(); ++i) {
            // skip handled case
            if (i != index) {
                this.mCheckList.set(i, false);
            }
        }

        notifyDataSetChanged();
    }

    public int getSelectedItemIndex() {
        for (int i = 0; i < mCheckList.size(); ++i) {
            if (mCheckList.get(i)) {
                return i;
            }
        }
        return -1;
    }

    public Open311Service getSelectedItem() {
        return this.mSelection;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mViewHolder = new ViewHolder(
                inflater.inflate(R.layout.service_details_content, parent, false));
        return mViewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Open311Service open311Service = mOpen311Services.get(position);
        boolean selected = mCheckList.get(position);
        holder.bind(open311Service, selected);
    }

    @Override
    public int getItemCount() {
        return this.mOpen311Services.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvOpen311ServiceDesc;
        private TextView tvOpen311ServiceName;
        private TextView tvSelectedService;

        ViewHolder(View itemView) {
            super(itemView);
            tvOpen311ServiceDesc = (TextView) itemView.findViewById(R.id.tv_ServiceDescription);
            tvOpen311ServiceName = (TextView) itemView.findViewById(R.id.tv_ServiceName);
            tvSelectedService = (TextView) itemView.findViewById(R.id.tv_SelectedService);
        }

        void bind(Open311Service mOpen311Open311Service, boolean selected) {
            tvOpen311ServiceName.setText(mOpen311Open311Service.name);
            tvOpen311ServiceDesc.setText(mOpen311Open311Service.description);
            tvSelectedService.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
            Log.d(TAG, "Toggling to " + mOpen311Open311Service.name + " to " + selected);
        }
    }
}
