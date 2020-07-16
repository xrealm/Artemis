package com.artemis.media.filter.effect;

import com.artemis.media.filter.filter.GroupFilter;

/**
 * Created by xrealm on 2020/7/16.
 */
public class GhostGroupFilter extends GroupFilter {

    WobbleFilter wobbleFilter;
    SolarizedFilter solarizedFilter;
    VignettedFilter vignettedFilter;

    public GhostGroupFilter() {

        wobbleFilter = new WobbleFilter();
        solarizedFilter = new SolarizedFilter();
        vignettedFilter = new VignettedFilter();

        wobbleFilter.setStrength(0.01f);
        wobbleFilter.setSize(1f);
        solarizedFilter.setBrightness(0.4f);
        solarizedFilter.setPower(1f);
        solarizedFilter.setColorize(0.2f);
        vignettedFilter.setDarkness(1f);
        vignettedFilter.setAmount(1.3f);

        wobbleFilter.addTarget(solarizedFilter);
        solarizedFilter.addTarget(vignettedFilter);
        vignettedFilter.addTarget(this);

        registerInitialFilter(wobbleFilter);
        registerFilter(solarizedFilter);
        registerTerminalFilter(vignettedFilter);
    }
}
