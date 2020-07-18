package com.artemis.media.camera.filter;

import android.content.Context;

import com.artemis.media.camera.R;
import com.artemis.media.filter.colour.LookupFilter;
import com.artemis.media.filter.effect.GhostGroupFilter;
import com.artemis.media.filter.filter.GroupFilter;

/**
 * Created by xrealm on 2020/7/16.
 */
public class ImagePreProcessingFilter extends GroupFilter {

    private LookupFilter lookupFilter;
    private GhostGroupFilter filter;

    public ImagePreProcessingFilter(Context context) {
        lookupFilter = new LookupFilter(context, R.mipmap.abaose_lut);
        filter = new GhostGroupFilter();

        filter.addTarget(this);
        registerInitialFilter(filter);
        registerTerminalFilter(filter);

    }
}
