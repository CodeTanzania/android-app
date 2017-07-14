package com.github.codetanzania.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.codetanzania.model.ServiceRequest;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import tz.co.codetanzania.R;

public class ServiceRequestsAdapter extends
        ClickAwareRecyclerViewAdapter {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    /* A list of open311Service requests by the civilian */
    private final List<ServiceRequest> mServiceRequests;

    /* Title of the issues */
    private final String mTitle;

    /* context allows us to access to resources as in ordinary first class citizen components */
    private Context mContext;

    /* constructor */
    public ServiceRequestsAdapter(
            Context mContext,
            String title,
            List<ServiceRequest> serviceRequests,
            OnItemClickListener<ServiceRequest> onItemClickListener) {
        super(onItemClickListener);
        this.mContext = mContext;
        this.mTitle = title;
        this.mServiceRequests = serviceRequests;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        /* inflate view and return view holder */
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (viewType == TYPE_ITEM) {
            View view = inflater.inflate(R.layout.issue_ticket, parent, false);
            return new ServiceRequestViewHolder(view, mClickListener);
        } else if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.issue_ticket_groups_title, parent, false);
            return new ServiceHeaderViewHolder(view);
        }

        throw new UnsupportedOperationException("Invalid view type");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        /* bind data to the views */
        if (getItemViewType(position) == TYPE_HEADER) {
            ((ServiceHeaderViewHolder)holder).tvHeader.setText(mTitle);
        } else {
            ServiceRequest serviceRequest = this
                    .mServiceRequests.get(position);

            ServiceRequestViewHolder serviceRequestViewHolder = (ServiceRequestViewHolder) holder;
            serviceRequestViewHolder.tvServiceReqTitle.setText(serviceRequest.service.name);
            serviceRequestViewHolder.tvServiceReqTicket.setText(serviceRequest.description);
            // serviceRequestViewHolder.tvServiceReqCode.setText(serviceRequest.service.name.substring(0, 2).toUpperCase());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            String lastActionDateStr;
            if (serviceRequest.resolvedAt != null) {
                lastActionDateStr = sdf.format(serviceRequest.resolvedAt);
                serviceRequestViewHolder.tvStatus.setText(mContext.getString(R.string.text_status_closed));
                serviceRequestViewHolder.tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
            } else {
                lastActionDateStr = sdf.format(serviceRequest.updatedAt);
                serviceRequestViewHolder.tvStatus.setText(mContext.getString(R.string.text_status_pending));
                serviceRequestViewHolder.tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.colorWarning));
            }
            serviceRequestViewHolder.tvServiceReqResolvedAt.setText(lastActionDateStr);

            /*((ServiceRequestViewHolder)holder).tvStatus.setCompoundDrawables(
                    null, null, ContextCompat.getDrawable(mContext, R.drawable.ic_warning_24dp), null);*/

//          Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.bg_circular_lbl);
//          drawable.setColorFilter(Color.parseColor(serviceRequest.service.color), PorterDuff.Mode.MULTIPLY);
//          serviceRequestViewHolder.tvServiceReqCode.setBackground(drawable);

            serviceRequestViewHolder.bind(serviceRequest, serviceRequestViewHolder.crdTicketItem);
        }
    }

    @Override
    public int getItemViewType(int pos) {
        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        /* Size of the requests */
        return mServiceRequests.size();
    }

    private static class ServiceRequestViewHolder extends RecyclerView.ViewHolder {

        TextView tvServiceReqCode;
        TextView tvServiceReqTitle;
        TextView tvServiceReqTicket;
        TextView tvServiceReqResolvedAt;
        TextView tvStatus;
        View     crdTicketItem;

        private OnItemClickListener<ServiceRequest> mClickListener;

        ServiceRequestViewHolder(View itemView, OnItemClickListener<ServiceRequest> mClickListener) {
            super(itemView);

            this.mClickListener = mClickListener;

            tvServiceReqCode = (TextView) itemView.findViewById(R.id.tv_serviceReqCode);
            tvServiceReqTitle = (TextView) itemView.findViewById(R.id.tv_serviceReqTitle);
            tvServiceReqResolvedAt = (TextView) itemView.findViewById(R.id.tv_serviceReqResolvedAt);
            tvServiceReqTicket = (TextView) itemView.findViewById(R.id.tv_serviceReqTicket);
            tvStatus = (TextView) itemView.findViewById(R.id.tv_Status);
            // vwStatusView = itemView.findViewById(R.id.vw_serviceReqStatus);
            crdTicketItem = itemView.findViewById(R.id.crd_TicketItem);
        }

        void bind(final ServiceRequest request, View view) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.onItemClick(request);
                }
            });
        }
    }

    private static class ServiceHeaderViewHolder extends RecyclerView.ViewHolder {

        TextView tvHeader;

        ServiceHeaderViewHolder (View itemView) {
            super(itemView);
            tvHeader = (TextView) itemView.findViewById(R.id.tv_serviceReqHeaderName);
        }
    }

}