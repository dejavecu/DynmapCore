package org.dynmap.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.dynmap.DynmapChunk;
import org.dynmap.DynmapWorld;
import org.dynmap.Log;

public abstract class CoreMapChunkCache implements MapChunkCache {
    protected DynmapWorld dw;
    protected List<DynmapChunk> chunks;
    protected ListIterator<DynmapChunk> iterator;

    protected int x_min, x_max, y_min, y_max, z_min, z_max;
    protected int x_dim;

    protected int chunks_read;    /* Number of chunks actually loaded */
    protected int chunks_attempted;   /* Number of chunks attempted to load */
    protected long total_loadtime;    /* Total time loading chunks, in nanoseconds */

    protected long exceptions;

    protected static final BlockStep unstep[] = { BlockStep.X_MINUS, BlockStep.Y_MINUS, BlockStep.Z_MINUS,
        BlockStep.X_PLUS, BlockStep.Y_PLUS, BlockStep.Z_PLUS
    };

    protected HiddenChunkStyle hidestyle = HiddenChunkStyle.FILL_AIR;
    private List<VisibilityLimit> visible_limits = null;
    private List<VisibilityLimit> hidden_limits = null;

    protected boolean do_generate = false;
    protected boolean do_save = false;
    protected boolean isempty = true;

    public void setVisibilityParameters(DynmapWorld world) {
        if(world.visibility_limits != null) {
            for(MapChunkCache.VisibilityLimit limit: world.visibility_limits) {
                setVisibleRange(limit);
            }
            setHiddenFillStyle(world.hiddenchunkstyle);
            setAutoGenerateVisbileRanges(world.do_autogenerate);
        }
        if(hidden_limits != null) {
            for(MapChunkCache.VisibilityLimit limit: world.hidden_limits) {
                setHiddenRange(limit);
            }
            setHiddenFillStyle(world.hiddenchunkstyle);
        }
    }

    public void setHiddenFillStyle(HiddenChunkStyle style) {
        this.hidestyle = style;
    }
    /**
     * Set autogenerate - must be done after at least one visible range has been set
     */
    public void setAutoGenerateVisbileRanges(DynmapWorld.AutoGenerateOption generateopt) {
        if((generateopt != DynmapWorld.AutoGenerateOption.NONE) && ((visible_limits == null) || (visible_limits.size() == 0))) {
            Log.severe("Cannot setAutoGenerateVisibleRanges() without visible ranges defined");
            return;
        }
        this.do_generate = (generateopt != DynmapWorld.AutoGenerateOption.NONE);
        this.do_save = (generateopt == DynmapWorld.AutoGenerateOption.PERMANENT);
    }
    /**
     * Add visible area limit - can be called more than once 
     * Needs to be set before chunks are loaded
     * Coordinates are block coordinates
     */
    public void setVisibleRange(VisibilityLimit lim) {
        VisibilityLimit limit = new VisibilityLimit();
        if(lim.x0 > lim.x1) {
            limit.x0 = (lim.x1 >> 4); limit.x1 = ((lim.x0+15) >> 4);
        }
        else {
            limit.x0 = (lim.x0 >> 4); limit.x1 = ((lim.x1+15) >> 4);
        }
        if(lim.z0 > lim.z1) {
            limit.z0 = (lim.z1 >> 4); limit.z1 = ((lim.z0+15) >> 4);
        }
        else {
            limit.z0 = (lim.z0 >> 4); limit.z1 = ((lim.z1+15) >> 4);
        }
        if(visible_limits == null)
            visible_limits = new ArrayList<VisibilityLimit>();
        visible_limits.add(limit);
    }
    /**
     * Add hidden area limit - can be called more than once 
     * Needs to be set before chunks are loaded
     * Coordinates are block coordinates
     */
    public void setHiddenRange(VisibilityLimit lim) {
        VisibilityLimit limit = new VisibilityLimit();
        if(lim.x0 > lim.x1) {
            limit.x0 = (lim.x1 >> 4); limit.x1 = ((lim.x0+15) >> 4);
        }
        else {
            limit.x0 = (lim.x0 >> 4); limit.x1 = ((lim.x1+15) >> 4);
        }
        if(lim.z0 > lim.z1) {
            limit.z0 = (lim.z1 >> 4); limit.z1 = ((lim.z0+15) >> 4);
        }
        else {
            limit.z0 = (lim.z0 >> 4); limit.z1 = ((lim.z1+15) >> 4);
        }
        if(hidden_limits == null)
            hidden_limits = new ArrayList<VisibilityLimit>();
        hidden_limits.add(limit);
    }

    protected boolean isChunkVisible(DynmapChunk chunk) {
        boolean result = true;
        if(visible_limits != null) {
            result = false;
            for(VisibilityLimit limit : visible_limits) {
                if((chunk.x >= limit.x0) && (chunk.x <= limit.x1) && (chunk.z >= limit.z0) && (chunk.z <= limit.z1)) {
                    result = true;
                    break;
                }
            }
        }
        if(result && (hidden_limits != null)) {
            for(VisibilityLimit limit : hidden_limits) {
                if((chunk.x >= limit.x0) && (chunk.x <= limit.x1) && (chunk.z >= limit.z0) && (chunk.z <= limit.z1)) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }
    
}