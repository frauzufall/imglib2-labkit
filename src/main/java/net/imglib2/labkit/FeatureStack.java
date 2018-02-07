package net.imglib2.labkit;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.util.stream.IntStream;

/**
 * @author Matthias Arzt
 */
// TODO : Refactor FeatureStack, what it is actually used for, can it be remove / replaced by something more appropriate
public class FeatureStack {

	private RandomAccessibleInterval<?> original;

	private final double scaling;

	private final CellGrid grid;

	private final RandomAccessibleInterval<?> preparedOriginal;

	public FeatureStack(RandomAccessibleInterval<?> original, double scaling, boolean isTimeSeries) {
		this.original = original;
		this.scaling = scaling;
		this.grid = initGrid(original, isTimeSeries);
		this.preparedOriginal = prepareOriginal(original);
	}

	private static CellGrid initGrid(Interval interval, boolean isTimeSeries) {
		int[] cellDimension = initCellDimension(interval.numDimensions(), isTimeSeries);
		return new CellGrid(Intervals.dimensionsAsLongArray(interval), cellDimension);
	}

	private static int[] initCellDimension(int n, boolean isTimeSeries) {
		return isTimeSeries ? RevampUtils.extend(initCellDimension(n - 1), 1) :
				initCellDimension(n);
	}

	private static int[] initCellDimension(int n) {
		int size = cellLength(n);
		return IntStream.range(0, n).map(x -> size).toArray();
	}

	private static int cellLength(int n) {
		switch (n) {
			case 2: return 128;
			case 3: return 32;
			default: return (int) Math.round(Math.pow(128. * 128., 1. / n) + 0.5);
		}
	}

	private RandomAccessibleInterval<?> prepareOriginal(RandomAccessibleInterval<?> original) {
		Object voxel = original.randomAccess().get();
		if(voxel instanceof RealType && !(voxel instanceof FloatType))
			return LabkitUtils.toFloat(RevampUtils.uncheckedCast(original));
		return original;
	}

	public static Img<FloatType> cachedFeatureBlock(FeatureCalculator feature, RandomAccessibleInterval<?> image) {
		return cachedFeatureBlock(feature, Views.extendBorder(image), initGrid(image, false));
	}

	public static Img<FloatType> cachedFeatureBlock(FeatureCalculator feature, RandomAccessible<?> extendedOriginal, CellGrid grid) {
		int count = feature.count();
		if(count <= 0)
			throw new IllegalArgumentException();
		long[] dimensions = LabkitUtils.extend(grid.getImgDimensions(), count);
		int[] cellDimensions = LabkitUtils.extend(new int[grid.numDimensions()], count);
		grid.cellDimensions(cellDimensions);
		final DiskCachedCellImgOptions featureOpts = DiskCachedCellImgOptions.options().cellDimensions( cellDimensions ).dirtyAccesses( false );
		final DiskCachedCellImgFactory< FloatType > featureFactory = new DiskCachedCellImgFactory<>( featureOpts );
		CellLoader<FloatType> loader = target -> feature.apply(extendedOriginal, RevampUtils.slices(target));
		return featureFactory.create(dimensions, new FloatType(), loader);
	}

	public double scaling() {
		return scaling;
	}

	public Interval interval() {
		return new FinalInterval(original);
	}

	public CellGrid grid() {
		return grid;
	}

	public RandomAccessibleInterval<?> compatibleOriginal() {
		return preparedOriginal;
	}
}