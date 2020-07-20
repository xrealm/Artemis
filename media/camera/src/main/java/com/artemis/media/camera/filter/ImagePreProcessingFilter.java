package com.artemis.media.camera.filter;

import android.content.Context;

import com.artemis.media.camera.R;
import com.artemis.media.filter.beauty.HighContrastSmoothFilter;
import com.artemis.media.filter.colour.LookupFilter;
import com.artemis.media.filter.filter.BasicFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xrealm on 2020/7/16.
 */
public class ImagePreProcessingFilter extends BasicFaceFilter {

    private LookupFilter lookupFilter;
    private HighContrastSmoothFilter smoothFilter;
    private FacePointVaoFilter facePointFilter;

    public ImagePreProcessingFilter(Context context) {
        lookupFilter = new LookupFilter(context, R.mipmap.abaose_lut);
        lookupFilter.setIntensity(0.5f);
        smoothFilter = new HighContrastSmoothFilter();
        smoothFilter.setSmoothLevel(0.3f);

        facePointFilter = new FacePointVaoFilter();

        List<BasicFilter> filterList = new ArrayList<>();
        filterList.add(lookupFilter);
        filterList.add(smoothFilter);
        filterList.add(facePointFilter);

        constructGroupFilter(filterList);

    }
}
