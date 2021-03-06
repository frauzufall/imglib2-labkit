package net.imglib2.labkit.control.brush.neighborhood;

import net.imglib2.RandomAccess;
import net.imglib2.algorithm.neighborhood.HyperSphereNeighborhood;
import net.imglib2.algorithm.neighborhood.Neighborhood;

import java.util.stream.DoubleStream;

public class NeighborhoodFactories
{

	public static < T > NeighborhoodFactory hyperSphere()
	{
		return new NeighborhoodFactory() {
			@Override
			public <T> Neighborhood<T> create(RandomAccess<T> access, long[] position, long size) {
				return HyperSphereNeighborhood.<T>factory().create(position, size, access);
			}
		};
	}

	public static NeighborhoodFactory hyperEllipsoid(final double[] radius) {
		return (new NeighborhoodFactory() {
			@Override
			public <T> Neighborhood<T> create(RandomAccess<T> access, long[] position, long size) {
				return new HyperEllipsoidNeighborhood<>(position, scale(radius, size), access);
			}
		});
	}

	private static double[] scale(double[] radius, long size) {
		return DoubleStream.of(radius).map(x -> x * size).toArray();
	}
}
