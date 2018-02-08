package net.imglib2.labkit.models;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.ARGBType;

public class BitmapModel
{

	private final LabelingModel model;

	public BitmapModel(LabelingModel model) {
		this.model = model;
	}

	public String label() {
		return model.selectedLabel().get();
	}

	public ARGBType color() {
		return model.colorMapProvider().colorMap().getColor( label() );
	}

	public RandomAccessibleInterval<BitType> bitmap() {
		return model.labeling().get().regions().get( model.selectedLabel().get() );
	}

	public void fireBitmapChanged() {
		model.dataChangedNotifier().forEach( Runnable::run );
	}
}
