package com.artemis.media.camera.filter;

import com.artemis.cv.FrameInfo;
import com.artemis.media.filter.filter.BasicFilter;
import com.artemis.media.filter.filter.GroupFilter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by xrealm on 2020/7/19.
 */
public class BasicFaceFilter extends GroupFilter implements IFaceFilter {

    private List<BasicFilter> mFilters = new CopyOnWriteArrayList<>();

    protected void insertFilter(BasicFilter dstFilter, BasicFilter filter) {
        int size = mFilters.size();
        int dstIndex = -1;
        for (int i = 0; i < size; i++) {
            if (mFilters.get(i) == dstFilter) {
                BasicFilter nextFilter = null;
                if (i < size - 1) {
                    nextFilter = mFilters.get(i + 1);
                }
                if (nextFilter != null) {
                    dstFilter.removeTarget(nextFilter);
                    dstFilter.addTarget(filter);
                    filter.addTarget(nextFilter);
                    registerFilter(filter);
                } else {
                    dstFilter.removeTarget(this);
                    removeTerminalFilter(dstFilter);
                    dstFilter.addTarget(filter);
                    filter.addTarget(this);
                    registerFilter(dstFilter);
                    registerTerminalFilter(filter);
                }
                dstIndex = i;
                break;
            }
        }
        if (dstIndex > 0) {
            mFilters.add(dstIndex + 1, filter);
        }
    }

    protected boolean removeDstFilter(BasicFilter dstFilter) {
        int size = mFilters.size();
        int dstIndex = -1;
        for (int i = 0; i < size; i++) {
            BasicFilter filter = mFilters.get(i);
            if (dstFilter == filter) {
                BasicFilter preFilter = null;
                BasicFilter nextFilter = null;
                if (i > 0) {
                    preFilter = mFilters.get(i - 1);
                }
                if (i < size - 1) {
                    nextFilter = mFilters.get(i + 1);
                }
                if (preFilter != null && nextFilter == null) {
                    preFilter.removeTarget(dstFilter);
                    removeTerminalFilter(dstFilter);
                    registerTerminalFilter(preFilter);
                }
                dstIndex = i;
                break;
            }
        }
        if (dstIndex < 0) {
            return false;
        }
        mFilters.remove(dstFilter);
        return true;
    }

    protected void addTerminalFilter(BasicFilter filter) {
        if (mFilters.size() == 0 && getTerminalFilters().size() == 0) {
            filter.addTarget(this);
            registerInitialFilter(filter);
            registerTerminalFilter(filter);
            mFilters.add(filter);
            return;
        }
        BasicFilter terminalFilter = getTerminalFilters().get(0);
        if (mFilters.get(mFilters.size() - 1) == terminalFilter) {
            terminalFilter.removeTarget(this);
            removeTerminalFilter(terminalFilter);
            terminalFilter.addTarget(filter);
            filter.addTarget(this);
            registerFilter(terminalFilter);
            registerTerminalFilter(filter);
            mFilters.add(filter);
        }
    }

    protected boolean resetFilter(BasicFilter oldFilter, BasicFilter newFilter) {
        if (oldFilter == newFilter) {
            return false;
        }
        int relinkIndex = -1;
        int size = mFilters.size();
        for (int i = 0; i < size; i++) {
            BasicFilter filter = mFilters.get(i);

            if (filter == oldFilter) {
                BasicFilter preFilter = null;
                BasicFilter nextFilter = null;
                if (i > 0) {
                    preFilter = mFilters.get(i - 1);
                }
                if (i < size - 1) {
                    nextFilter = mFilters.get(i + 1);
                }
                if (preFilter == null && nextFilter == null) {
                    oldFilter.removeTarget(this);
                    removeInitialFilter(oldFilter);
                    removeTerminalFilter(oldFilter);
                    newFilter.addTarget(this);
                    registerInitialFilter(newFilter);
                    registerTerminalFilter(newFilter);
                } else if (preFilter == null) {
                    oldFilter.removeTarget(nextFilter);
                    removeInitialFilter(oldFilter);
                    newFilter.addTarget(nextFilter);
                    registerInitialFilter(newFilter);
                } else if (nextFilter == null) {
                    preFilter.removeTarget(oldFilter);
                    oldFilter.removeTarget(this);
                    removeTerminalFilter(oldFilter);
                    preFilter.addTarget(newFilter);
                    newFilter.addTarget(this);
                    registerTerminalFilter(newFilter);
                } else {
                    preFilter.removeTarget(oldFilter);
                    oldFilter.removeTarget(nextFilter);
                    removeFilter(oldFilter);
                    preFilter.addTarget(newFilter);
                    newFilter.addTarget(nextFilter);
                    registerFilter(newFilter);
                }
                relinkIndex = i;
                break;
            }
        }
        if (relinkIndex < 0) {
            return false;
        }

        mFilters.remove(oldFilter);
        mFilters.add(relinkIndex, newFilter);
        return true;
    }

    protected void constructGroupFilter(List<BasicFilter> inputFilters) {
        int size = inputFilters.size();
        if (size > 0) {
            BasicFilter filterBegin = inputFilters.get(0);
            BasicFilter filterEnd = inputFilters.get(size - 1);
            registerInitialFilter(filterBegin);
            BasicFilter curFilter = null;
            for (int i = 0; i < size; i++) {
                BasicFilter basicFilter = inputFilters.get(i);
                basicFilter.clearTarget();
                if (curFilter != null) {
                    curFilter.addTarget(basicFilter);
                }
                if (i > 0 && i < size - 1) {
                    registerFilter(basicFilter);
                }
                curFilter = basicFilter;
            }
            filterEnd.addTarget(this);
            registerTerminalFilter(filterEnd);
            mFilters.addAll(inputFilters);
        }
    }

    protected void destructGroupFilter() {
        int size = mFilters.size();
        if (size > 0) {
            BasicFilter filterBegin = mFilters.get(0);
            BasicFilter filterEnd = mFilters.get(size - 1);
            for (int i = size - 1; i >= 0; i--) {
                BasicFilter basicFilter = mFilters.get(i);
                basicFilter.clearTarget();
                removeFilter(basicFilter);
            }
            filterEnd.clearTarget();
            removeInitialFilter(filterBegin);
            removeTerminalFilter(filterEnd);
            mFilters.clear();
        }
    }

    @Override
    public void setFrameInfo(FrameInfo frameInfo) {
        int size = getFilters().size();
        if (size > 0) {
            for (BasicFilter filter : getFilters()) {
                if (filter instanceof IFaceFilter) {
                    ((IFaceFilter) filter).setFrameInfo(frameInfo);
                }
            }
        }
    }

    private List<BasicFilter> getFilters() {
        return mFilters;
    }
}
