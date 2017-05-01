package net.gegy1000.earth.server.world.gen;

public interface HeightProvider {
    int provideHeight(int x, int z);

    static HeightProvider offset(HeightProvider provider, int offset) {
        return (x, z) -> provider.provideHeight(x, z) + offset;
    }
}
