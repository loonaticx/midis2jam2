package org.wysko.midis2jam2.instrument.guitar;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import org.wysko.midis2jam2.Midis2jam2;
import org.wysko.midis2jam2.midi.MidiChannelSpecificEvent;

import java.util.HashMap;
import java.util.List;

import static org.wysko.midis2jam2.Midis2jam2.rad;

/**
 * Adam Neely would be proud.
 *
 * @see FrettedInstrument
 */
public class BassGuitar extends FrettedInstrument {
	
	private final static Vector3f BASE_POSITION = new Vector3f(51.5863f, 54.5902f, -16.5817f);
	
	public BassGuitar(Midis2jam2 context, List<MidiChannelSpecificEvent> events) {
		super(context,
				new FrettingEngine(4, 22, new int[] {28, 33, 38, 43}, 28, 65),
				events,
				new FrettedInstrumentPositioning(19.5F, -26.57f, new Vector3f[] {
						new Vector3f(1, 1, 1),
						new Vector3f(1, 1, 1),
						new Vector3f(1, 1, 1),
						new Vector3f(1, 1, 1)
					
				},
						new float[] {-0.85f, -0.31f, 0.20f, 0.70f},
						new float[] {-1.86f, -0.85f, 0.34f, 1.37f},
						new FretHeightByTable(new HashMap<Integer, Float>() {{
							put(0, 0.0f);
							put(1, 0.05f);
							put(2, 0.1f);
							put(3, 0.15f);
							put(4, 0.20f);
							put(5, 0.24f);
							put(6, 0.285f);
							put(7, 0.325f);
							put(8, 0.364f);
							put(9, 0.4f);
							put(10, 0.43f);
							put(11, 0.464f);
							put(12, 0.494f);
							put(13, 0.523f);
							put(14, 0.55f);
							put(15, 0.575f);
							put(16, 0.6f);
							put(17, 0.62f);
							put(18, 0.643f);
							put(19, 0.663f);
							put(20, 0.68f);
							put(21, 0.698f);
							put(22, 0.716f);
						}})),
				4,
				context.loadModel("Bass.obj", "BassSkin.bmp", Midis2jam2.MatType.UNSHADED, 0.9f)
		);
		
		
		for (int i = 0; i < 4; i++) {
			Spatial string;
			
			string = context.loadModel("BassString.obj", "BassSkin.bmp", Midis2jam2.MatType.UNSHADED, 0.9f);
			upperStrings[i] = string;
			instrumentNode.attachChild(upperStrings[i]);
		}
		
		// Position each string
		final float forward = 0.125f;
		upperStrings[0].setLocalTranslation(positioning.topX[0], positioning.topY, forward);
		upperStrings[0].setLocalRotation(new Quaternion().fromAngles(0, 0, rad(-1.24)));
		
		upperStrings[1].setLocalTranslation(positioning.topX[1], positioning.topY, forward);
		upperStrings[1].setLocalRotation(new Quaternion().fromAngles(0, 0, rad(-0.673)));
		
		upperStrings[2].setLocalTranslation(positioning.topX[2], positioning.topY, forward);
		upperStrings[2].setLocalRotation(new Quaternion().fromAngles(0, 0, rad(0.17)));
		
		upperStrings[3].setLocalTranslation(positioning.topX[3], positioning.topY, forward);
		upperStrings[3].setLocalRotation(new Quaternion().fromAngles(0, 0, rad(0.824)));
		
		// Lower strings
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 5; j++) {
				lowerStrings[i][j] = context.loadModel("BassStringBottom" + j + ".obj", "BassSkin.bmp",
						Midis2jam2.MatType.UNSHADED, 0.9f);
				instrumentNode.attachChild(lowerStrings[i][j]);
			}
		}
		
		// Position lower strings
		for (int i = 0; i < 5; i++) {
			lowerStrings[0][i].setLocalTranslation(positioning.bottomX[0], positioning.bottomY, forward);
			lowerStrings[0][i].setLocalRotation(new Quaternion().fromAngles(0, 0, rad(-1.24)));
		}
		for (int i = 0; i < 5; i++) {
			lowerStrings[1][i].setLocalTranslation(positioning.bottomX[1], positioning.bottomY, forward);
			lowerStrings[1][i].setLocalRotation(new Quaternion().fromAngles(0, 0, rad(-0.673)));
		}
		for (int i = 0; i < 5; i++) {
			lowerStrings[2][i].setLocalTranslation(positioning.bottomX[2], positioning.bottomY, forward);
			lowerStrings[2][i].setLocalRotation(new Quaternion().fromAngles(0, 0, rad(0.17)));
		}
		
		for (int i = 0; i < 5; i++) {
			lowerStrings[3][i].setLocalTranslation(positioning.bottomX[3], positioning.bottomY, forward);
			lowerStrings[3][i].setLocalRotation(new Quaternion().fromAngles(0, 0, rad(0.824)));
		}
		
		// Hide all wobbly strings
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 5; j++) {
				lowerStrings[i][j].setCullHint(Spatial.CullHint.Always);
			}
		}
		
		// Initialize note fingers
		for (int i = 0; i < 4; i++) {
			noteFingers[i] = context.loadModel("BassNoteFinger.obj", "BassSkin.bmp", Midis2jam2.MatType.UNSHADED, 0.9f);
			instrumentNode.attachChild(noteFingers[i]);
			noteFingers[i].setCullHint(Spatial.CullHint.Always);
		}
		
		// Position guitar
		highestLevel = new Node();
		highestLevel.setLocalTranslation(BASE_POSITION);
		highestLevel.setLocalRotation(new Quaternion().fromAngles(rad(-3.21), rad(-43.5), rad(-29.1)));
		highestLevel.attachChild(instrumentNode);
		context.getRootNode().attachChild(highestLevel);
	}
	
	
	@Override
	public void tick(double time, float delta) {
		setIdleVisibilityByPeriods(finalNotePeriods, time, highestLevel);
		
		final int indexThis = getIndexOfThis();
		Vector3f add = new Vector3f(BASE_POSITION).add(new Vector3f(indexThis * 7, indexThis * -2.43f, 0));
		highestLevel.setLocalTranslation(add);
		
		handleStrings(time, delta);
	}
}
