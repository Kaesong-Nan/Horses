package com.forgenz.horses.attribute;

import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import com.voxmc.voxlib.util.VoxTrail;

/**
 * Created by john on 8/7/15.
 */
public class TrailAttribute extends Attribute implements BuffAttribute {
    private VoxTrail voxTrail;
    
    public TrailAttribute(VoxTrail voxTrail) {
        this.voxTrail = voxTrail;
    }
    
    @Override
    public void onAdd(PlayerHorse playerHorse) {
        if(playerHorse == null || voxTrail == null) {
            return;
        }
        voxTrail.addToEntity(playerHorse.getHorse(), Horses.getInstance());
    }
    
    @Override
    public void onRemove(PlayerHorse playerHorse) {
        if(playerHorse == null || voxTrail == null) {
            return;
        }
        voxTrail.remove(playerHorse.getHorse(), Horses.getInstance());
    }
    
    public VoxTrail getVoxTrail() {
        return voxTrail;
    }
    
    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        
        TrailAttribute that = (TrailAttribute) o;
        
        return !(voxTrail != null ? !voxTrail.equals(that.voxTrail) : that.voxTrail != null);
    }
    
    @Override
    public int hashCode() {
        return voxTrail != null ? voxTrail.hashCode() : 0;
    }
}
