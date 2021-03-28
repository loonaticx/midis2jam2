package org.wysko.midis2jam2.instrument.brass;

import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.wysko.midis2jam2.Midis2jam2;
import org.wysko.midis2jam2.instrument.Axis;
import org.wysko.midis2jam2.instrument.Clone;
import org.wysko.midis2jam2.instrument.MonophonicInstrument;
import org.wysko.midis2jam2.midi.MidiChannelSpecificEvent;

import java.util.List;

import static org.wysko.midis2jam2.Midis2jam2.rad;

public class Trombone extends MonophonicInstrument {
	
	/**
	 * Instantiates a new sustained instrument.
	 *
	 * @param context   the context to the main class
	 * @param eventList
	 */
	public Trombone(@NotNull Midis2jam2 context,
	                @NotNull List<MidiChannelSpecificEvent> eventList) throws ReflectiveOperationException {
		super(context, eventList, TromboneClone.class);
	}
	
	@Override
	protected void moveForMultiChannel() {
		offsetNode.setLocalTranslation(0, 10 * indexForMoving(), 0);
	}
	
	public class TromboneClone extends Clone {
		
		
		private final Spatial slide;
		
		public TromboneClone() {
			super(Trombone.this, 0.1f, Axis.X);
			
			Spatial body = context.loadModel("Trombone.fbx", "HornSkin.bmp", Midis2jam2.MatType.REFLECTIVE, 0.9f);
			modelNode.attachChild(body);
			
			Material material = context.reflectiveMaterial("Assets/HornSkinGrey.bmp");
			((Node) body).getChild(1).setMaterial(material);
			
			slide = context.loadModel("TromboneSlide.obj", "HornSkin.bmp", Midis2jam2.MatType.REFLECTIVE, 0.9f);
			modelNode.attachChild(slide);
			modelNode.setLocalRotation(new Quaternion().fromAngles(rad(-10), 0, 0));
			highestLevel.setLocalTranslation(0, 65, -200);
		}
		
		private void moveToPosition(@Range(from = 1, to = 7) int position) {
			slide.setLocalTranslation(slidePosition(position));
		}
		
		private Vector3f slidePosition(@Range(from = 1, to = 7) int position) {
			return new Vector3f(0, 0, 3.33f * position - 1);
		}
		
		@Override
		protected void tick(double time, float delta) {
			super.tick(time, delta);
			if (isPlaying()) {
				if (currentNotePeriod != null) {
					moveToPosition((currentNotePeriod.midiNote % 7) + 1);
				}
			} else {
//				if (!notePeriods.isEmpty() && time - notePeriods.get(0).startTime < 1) {
//					Vector3f slidePosition = slidePosition(notePeriods.get(0).midiNote % 7);
//					Vector3f localTranslation = slide.getLocalTranslation();
//					if (localTranslation.z - slidePosition.z > 0.01) {
//						slide.move(0, 0, -10 * delta);
//					} else if (localTranslation.z - slidePosition.z < 0.01) {
//						slide.move(0, 0, 10 * delta);
//					}
//				}
			}
		}
		
		@Override
		protected void moveForPolyphony() {
			offsetNode.setLocalRotation(new Quaternion().fromAngles(0, rad(30 + indexForMoving() * -3), 0));
			offsetNode.setLocalTranslation(0, indexForMoving(), 0);
			
		}
	}
}
