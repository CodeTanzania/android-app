package com.github.codetanzania.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.codetanzania.model.ServiceRequest;
import com.github.codetanzania.util.LookAndFeelUtils;
import com.github.codetanzania.util.ServiceRequestsUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import tz.co.codetanzania.R;

public class ServiceRequestsListAdapter extends
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
    public ServiceRequestsListAdapter(
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
            View view = inflater.inflate(R.layout.cardview_issue_list_item, parent, false);
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
            ServiceRequest request = this.mServiceRequests.get(position);

            ServiceRequestViewHolder srViewHolder = (ServiceRequestViewHolder) holder;
            srViewHolder.tvTitle.setText(request.service.name);
            srViewHolder.tvDescription.setText(request.description);
            System.out.println("Icon res: "+request.service.getIcon());
            srViewHolder.tvServiceTypeIcon.setImageResource(request.service.getIcon());

            Date lastActionDate;
            if (request.resolvedAt != null) {
                lastActionDate = request.resolvedAt;
                srViewHolder.tvStatus.setText(mContext.getString(R.string.text_status_closed));
                srViewHolder.tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
            } else {
                lastActionDate = request.updatedAt;
                srViewHolder.tvStatus.setText(mContext.getString(R.string.text_status_pending));
                srViewHolder.tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.colorWarning));
            }
            srViewHolder.tvServiceReqResolvedAt.setText(LookAndFeelUtils.formatDate(lastActionDate));

            srViewHolder.bind(request, srViewHolder.crdTicketItem);
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

        ImageView tvServiceTypeIcon;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvServiceReqResolvedAt;
        TextView tvStatus;
        View     crdTicketItem;

        private OnItemClickListener<ServiceRequest> mClickListener;

        ServiceRequestViewHolder(View itemView, OnItemClickListener<ServiceRequest> mClickListener) {
            super(itemView);

            this.mClickListener = mClickListener;

            tvServiceTypeIcon = (ImageView) itemView.findViewById(R.id.tv_serviceTypeIcon);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_serviceReqTitle);
            tvServiceReqResolvedAt = (TextView) itemView.findViewById(R.id.tv_serviceReqDate);
            tvDescription = (TextView) itemView.findViewById(R.id.tv_serviceReqDescription);
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