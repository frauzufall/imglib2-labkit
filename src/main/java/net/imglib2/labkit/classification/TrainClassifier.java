package net.imglib2.labkit.classification;

import java.util.Collections;
import java.util.function.Supplier;

import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.SegmentationComponent;
import net.imglib2.labkit.labeling.Labeling;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.models.SegmentationModel;

public class TrainClassifier
{

	SegmentationModel model;

	public TrainClassifier( Extensible extensible, SegmentationModel model )
	{
		this.model = model;
		extensible.addAction("Train Classifier", "trainClassifier", this::trainClassifier, "ctrl shift T");
	}

	private void trainClassifier()
	{
		try
		{
			model.segmenter().train(Collections.singletonList(model.image()), Collections.singletonList(model.labeling()));
		}
		catch ( final Exception e1 )
		{
			System.out.println("Training was interrupted by exception:");
			e1.printStackTrace();
		}
	}

}
