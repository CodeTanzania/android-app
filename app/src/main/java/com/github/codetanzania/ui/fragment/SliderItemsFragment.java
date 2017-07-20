package com.github.codetanzania.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.andraskindler.parallaxviewpager.ParallaxViewPager;
import com.github.codetanzania.adapter.SliderItemViewPagerAdapter;
import com.github.codetanzania.api.model.Open311Service;
import com.github.codetanzania.util.Open311ServicesUtil;

import java.util.List;

import me.relex.circleindicator.CircleIndicator;
import tz.co.codetanzania.R;

public class SliderItemsFragment extends Fragment {

    /* bridges communication between the fragment and the attached activity */
    private OnShowOpen311ServicesList onShowOpen311ServicesList;

    /* a list of model items shown by the view pager (slider) */
    private List<Open311Service> mSliderModelItems;

    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        try {
            onShowOpen311ServicesList = (OnShowOpen311ServicesList) ctx;
        } catch (ClassCastException cce) {
            throw new ClassCastException(String.format(
                    "%s must implement %s", ctx.getClass().getName(),
                    OnShowOpen311ServicesList.class.getName()
            ));
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup viewGroup,
            Bundle savedInstanceState) {

        View fragView = inflater.inflate(R.layout.card_view_quick_issue_selector, viewGroup, false);
        return fragView;
    }

    @Override
    public void onViewCreated(View fragView, Bundle savedInstanceState) {
        ParallaxViewPager itemsSlider =
                (ParallaxViewPager) fragView.findViewById(R.id.view_pager_ItemsSlider);
        CircleIndicator   circleIndicator =
                (CircleIndicator) fragView.findViewById(R.id.indicator);
        // link to viewing all the items
        Button btnViewAllCategories = (Button) fragView.findViewById(R.id.btn_OpenOpen311ServiceList);

        // setup view pager
        setupViewPager(itemsSlider, circleIndicator);
        /* setup events */
        bindEvents(btnViewAllCategories);
    }

    private void setupViewPager(ParallaxViewPager slider, CircleIndicator indicator) {
        mSliderModelItems = getItemModels();
        SliderItemViewPagerAdapter adapter = new SliderItemViewPagerAdapter(getChildFragmentManager(), mSliderModelItems);
        slider.setAdapter(adapter);
        indicator.setViewPager(slider);
    }

    private void bindEvents(Button btn) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShowOpen311ServicesList.showList();
            }
        });
    }

    private List<Open311Service> getItemModels() {
        return Open311ServicesUtil.cached(getActivity());
    }

    /* the interface is enables implementation to respond to "Show All Items" event when the button
     * to preview all items is clicked */
    public interface OnShowOpen311ServicesList {
        void showList();
    }

    /**/
}
