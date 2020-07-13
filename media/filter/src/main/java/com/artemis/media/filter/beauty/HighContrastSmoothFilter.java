package com.artemis.media.filter.beauty;

import com.artemis.media.filter.filter.GroupFilter;
import com.artemis.media.filter.filter.NothingFilter;
import com.artemis.media.filter.input.GLTextureOutputRenderer;
import com.artemis.media.filter.processing.BoxBlur2Filter;
import com.artemis.media.filter.processing.HighContrastFilter;

/**
 * Created by xrealm on 2020/7/10.
 */
public class HighContrastSmoothFilter extends GroupFilter {

    private NothingFilter nothingFilter;
    private BoxBlur2Filter firstBoxBlurFilter;
    private BoxBlur2Filter secondBoxBlurFilter;
    private HighContrastFilter highContrastFilter;
    private HCSmoothFilter smoothFilter;

    public HighContrastSmoothFilter() {
        nothingFilter = new NothingFilter();
        firstBoxBlurFilter = new BoxBlur2Filter();
        secondBoxBlurFilter = new BoxBlur2Filter();
        highContrastFilter = new HighContrastFilter();
        smoothFilter = new HCSmoothFilter();

        nothingFilter.addTarget(firstBoxBlurFilter);
        nothingFilter.addTarget(highContrastFilter);
        firstBoxBlurFilter.addTarget(highContrastFilter);
        highContrastFilter.registerFilterLocation(nothingFilter);
        highContrastFilter.registerFilterLocation(firstBoxBlurFilter);

        highContrastFilter.addTarget(secondBoxBlurFilter);

        nothingFilter.addTarget(smoothFilter);
        firstBoxBlurFilter.addTarget(smoothFilter);
        secondBoxBlurFilter.addTarget(smoothFilter);
        smoothFilter.registerFilterLocation(nothingFilter);
        smoothFilter.registerFilterLocation(firstBoxBlurFilter);
        smoothFilter.registerFilterLocation(secondBoxBlurFilter);
        smoothFilter.addTarget(this);

        registerInitialFilter(nothingFilter);
        registerFilter(firstBoxBlurFilter);
        registerFilter(highContrastFilter);
        registerFilter(secondBoxBlurFilter);
        registerTerminalFilter(smoothFilter);
    }

    public void setSmoothLevel(float level) {
        smoothFilter.setSmoothLevel(level);
    }

    @Override
    public void newTextureReady(int texture, GLTextureOutputRenderer source, boolean newData) {
        if (!getTerminalFilters().contains(source)) {
            int width = Math.min(source.getWidth() / 2, 360);
            int height = Math.min(source.getHeight() / 2, 480);
            firstBoxBlurFilter.setRenderSize(width, height);
            highContrastFilter.setRenderSize(width, height);
            secondBoxBlurFilter.setRenderSize(source.getWidth(), source.getHeight());

        }
        super.newTextureReady(texture, source, newData);
    }
}
