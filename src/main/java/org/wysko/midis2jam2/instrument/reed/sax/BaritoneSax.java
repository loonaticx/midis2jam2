package org.wysko.midis2jam2.instrument.reed.sax;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import org.wysko.midis2jam2.Midis2jam2;
import org.wysko.midis2jam2.instrument.Clone;
import org.wysko.midis2jam2.midi.MidiChannelSpecificEvent;

import java.util.HashMap;
import java.util.List;

import static org.wysko.midis2jam2.Midis2jam2.rad;

/**
 * The baritone sax.
 */
public class BaritoneSax extends Saxophone {
	
	private final static float STRETCH_FACTOR = 0.65f;
	
	/**
	 * Defines which keys need to be pressed given the corresponding MIDI note.
	 */
	private final static HashMap<Integer, Integer[]> KEY_MAPPING = new HashMap<Integer, Integer[]>() {{
		
		/*
		0 - Palm F
		1 - Palm E
		2 - Palm D
		3 - B
		4 - Bis
		5 - A/C
		6 - G
		7 - G#
		8 - Low B
		9 - Low C#
		10 - Low Bb
		11 - Side E
		12 - Side C
		13 - Side Bb
		14 - F
		15 - E
		16 - Side F#
		17 - D
		18 - Eb
		19 - Low C
		 */
		
		put(68, new Integer[] {2, 1, 0, 11}); // 15va F
		put(67, new Integer[] {2, 1, 11}); // 15va E
		put(66, new Integer[] {2, 1}); // 15va Eb
		put(65, new Integer[] {2}); // 15va D
		put(64, new Integer[] {}); // C#
		put(63, new Integer[] {5}); // C
		put(62, new Integer[] {3}); // B
		put(61, new Integer[] {3, 5, 13}); // Bb
		put(60, new Integer[] {3, 5}); // A
		put(59, new Integer[] {3, 5, 6, 7}); // G#
		put(58, new Integer[] {3, 5, 6}); // G
		put(57, new Integer[] {3, 5, 6, 15}); // F#
		put(56, new Integer[] {3, 5, 6, 14}); // F
		put(55, new Integer[] {3, 5, 6, 14, 15}); // E
		put(54, new Integer[] {3, 5, 6, 14, 15, 17, 18}); // Eb
		put(53, new Integer[] {3, 5, 6, 14, 15, 17}); // D
		put(52, new Integer[] {}); // C#
		put(51, new Integer[] {5}); // C
		put(50, new Integer[] {3}); // B
		put(49, new Integer[] {3, 5, 13}); // Bb
		put(48, new Integer[] {3, 5}); // A
		put(47, new Integer[] {3, 5, 6, 7}); // G#
		put(46, new Integer[] {3, 5, 6}); // G
		put(45, new Integer[] {3, 5, 6, 15}); // F#
		put(44, new Integer[] {3, 5, 6, 14}); // F
		put(43, new Integer[] {3, 5, 6, 14, 15}); // E
		put(42, new Integer[] {3, 5, 6, 14, 15, 17, 18}); // Eb
		put(41, new Integer[] {3, 5, 6, 14, 15, 17}); // D
		put(40, new Integer[] {3, 5, 6, 14, 15, 17, 9, 19}); // C#
		put(39, new Integer[] {3, 5, 6, 14, 15, 17, 19}); // C
		put(38, new Integer[] {3, 5, 6, 14, 15, 17, 19, 8}); // B
		put(37, new Integer[] {3, 5, 6, 14, 15, 17, 19, 10}); // Bb
	}};
	
	/**
	 * Constructs a baritone sax.
	 *
	 * @param context context to midis2jam2
	 * @param events  all events that pertain to this instance of a baritone sax
	 */
	public BaritoneSax(Midis2jam2 context,
	                   List<MidiChannelSpecificEvent> events)
			throws ReflectiveOperationException {
		
		super(context, events, BaritoneSaxClone.class);
		groupOfPolyphony.move(25, 48.5f, -15);
		groupOfPolyphony.rotate(rad(10), rad(30), 0);
		groupOfPolyphony.scale(1.5f);
	}
	
	/**
	 * Implements {@link Clone}, as baritone sax clones.
	 */
	public class BaritoneSaxClone extends SaxophoneClone {
		public BaritoneSaxClone() {
			super(BaritoneSax.this, STRETCH_FACTOR, KEY_MAPPING);
			
			Material shinyHornSkin = context.reflectiveMaterial("Assets/HornSkin.bmp");
			Material black = new Material(context.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
			black.setColor("Color", ColorRGBA.Black);
			
			Material shinyHornSkin2 = context.reflectiveMaterial("Assets/HornSkinGrey.bmp");
			
			this.body = context.getAssetManager().loadModel("Assets/BaritoneSaxBody.fbx");
			this.bell.attachChild(context.getAssetManager().loadModel("Assets/BaritoneSaxHorn.obj"));
			
			Node bodyNode = ((Node) body);
			
			bodyNode.getChild(0).setMaterial(shinyHornSkin);
			bodyNode.getChild(1).setMaterial(shinyHornSkin2);
			bodyNode.getChild(2).setMaterial(black);
			bell.setMaterial(shinyHornSkin);
			
			modelNode.attachChild(body);
			modelNode.attachChild(bell);
			bell.move(0, -10, 0); // Move bell down to body
		}
	}
}