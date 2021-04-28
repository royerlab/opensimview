package asdk.demo;

import static java.lang.Math.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import asdk.AlpaoDeformableMirror;
import asdk.TriggerMode;

public class AlpaoDeformableMirrorDemo
{

	@Test
	public void demo() throws IOException, InterruptedException
	{
		String lSerialName = "BIL118\0";

		AlpaoDeformableMirror lAlpaoDeformableMirror = new AlpaoDeformableMirror(lSerialName);
		lAlpaoDeformableMirror.setDebugPrintout(true);

		assertTrue(lAlpaoDeformableMirror.open());

		int lNumberOfActuators = lAlpaoDeformableMirror.getNumberOfActuators();
		assertEquals(97, lNumberOfActuators);

		assertTrue(lAlpaoDeformableMirror.setInputTriggerMode(TriggerMode.Disabled));

		for (int j = 0; j < 100; j++)
		{
			double[] lMirrorShape = new double[lNumberOfActuators];
			for (int i = 0; i < lNumberOfActuators; i++)
				lMirrorShape[i] = 0.1 * (2 * random() - 1);

			assertTrue(lAlpaoDeformableMirror.sendRawMirrorShapeVector(lMirrorShape));
			Thread.sleep(100);
		}

		int lNumberOfMirrorShapes = 100;
		double[] lMultipleMirrorShapes = new double[lNumberOfActuators * lNumberOfMirrorShapes];

		for (int i = 0; i < lMultipleMirrorShapes.length; i++)
			lMultipleMirrorShapes[i] = 0.01 * (2 * random() - 1);

		/*
		assertTrue(lAlpaoDeformableMirror.sendMirrorShapeSequenceAsynchronously(	lMultipleMirrorShapes,
																																							lNumberOfMirrorShapes,
																																							1));

		// assertTrue(lAlpaoDeformableMirror.setInputTriggerMode(TriggerMode.RisingEdge));

		assertTrue(lAlpaoDeformableMirror.sendMirrorShapeSequenceAsynchronously(	lMultipleMirrorShapes,
																																							lNumberOfMirrorShapes,
																																							1));
																																							/**/

		lAlpaoDeformableMirror.close();

	}
}
