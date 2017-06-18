package com.github.codetanzania.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;

import java.util.List;

import tz.co.codetanzania.R;

public class IssueMultimediaAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // private static final String TAG = "AttCardViewAdapter";

    private static final int MAP_VIEW_TYPE = 0;
    private static final int PICTURE_VIEW_TYPE = 1;

    private final Context mContext;
    private final List<String> mAttachments;
    private final double mGeoPoint[];

    public IssueMultimediaAdapter(
            @NonNull Context context, @NonNull List<String> attachments, double[] geoPoint) {
        this.mContext = context;
        this.mAttachments = attachments;
        this.mAttachments.add(0, null);
        this.mGeoPoint = geoPoint;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == MAP_VIEW_TYPE) {
            return new MapViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.card_view_static_osm, parent, false));
        } else {
            return new PictureViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.card_view_issue_image, parent, false));
        }
    }

    @Override public int getItemViewType(int position) {
        return position == 0 ? MAP_VIEW_TYPE : PICTURE_VIEW_TYPE;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == 0) {
            // setup map view
            setupMapView((MapViewHolder) holder);
        } else {
            // setup picture view
            setupPictureView((PictureViewHolder) holder, mAttachments.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mAttachments.size();
    }

    private void setupMapView(MapViewHolder mapViewHolder) {
        mapViewHolder.render(mGeoPoint[0], mGeoPoint[1]);
    }

    private void setupPictureView(PictureViewHolder pictureViewHolder, String source) {
        if (!TextUtils.isEmpty(source)) {
            pictureViewHolder.render(source);
        }
    }

    private static class MapViewHolder extends RecyclerView.ViewHolder {

        MapView mMapView;

        MapViewHolder(View itemView) {
            super(itemView);
            mMapView = (MapView) itemView.findViewById(R.id.map);
        }

        void render(double latitude, double longitude) {
            mMapView.setTileSource(TileSourceFactory.MAPNIK);
            mMapView.setBuiltInZoomControls(true);
            mMapView.setMultiTouchControls(true);

            // set zoom level and location
            IMapController mapController = mMapView.getController();
            mapController.setZoom(21);
            GeoPoint startPoint = new GeoPoint(latitude, longitude);
            mapController.setCenter(startPoint);

            // add compass
            CompassOverlay compassOverlay =
                    new CompassOverlay(mMapView.getContext(),
                    new InternalCompassOrientationProvider(mMapView.getContext()), mMapView);
            compassOverlay.enableCompass();
            mMapView.getOverlays().add(compassOverlay);

            // add marker
            Marker mMarker = new Marker(mMapView);
            mMarker.setPosition(startPoint);
            mMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mMarker.setTitle("Reported location");
            mMapView.getOverlays().add(mMarker);
            mMarker.setIcon(ContextCompat.getDrawable(
                    mMapView.getContext(), R.drawable.ic_location_on_white_24dp));
            mMapView.getOverlays().add(mMarker);
        }
    }

    private static class PictureViewHolder extends RecyclerView.ViewHolder {

        ImageView mImageView;

        PictureViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.imgView_Picture);
        }

        void render(String source) {
            Picasso.with(mImageView.getContext())
                    .load(source)
                    .into(mImageView);
        }
    }
}
