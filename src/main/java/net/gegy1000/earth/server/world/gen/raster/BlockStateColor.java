package net.gegy1000.earth.server.world.gen.raster;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import java.awt.Color;

public class BlockStateColor extends Color {
    private int value;

    public BlockStateColor() {
        super(0);
    }

    public void set(IBlockState state) {
        this.value = Block.getStateId(state) | 0xFF000000;
    }

    @Override
    public int hashCode() {
        return this.value;
    }

    @Override
    public int getRGB() {
        return this.value;
    }
}
