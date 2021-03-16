package org.wysko.midis2jam2.instrument.monophonic.reed.sax;

import com.jme3.math.Vector3f;
import org.wysko.midis2jam2.Midis2jam2;
import org.wysko.midis2jam2.instrument.monophonic.MonophonicInstrument;
import org.wysko.midis2jam2.midi.MidiFile;

public abstract class Saxophone extends MonophonicInstrument {
	public static final int KEY_COUNT = 20;
	protected static final Vector3f MULTI_SAX_OFFSET = new Vector3f(0, 40, 0);
	
	/**
	 * Constructs a saxophone.
	 *
	 * @param context context to midis2jam2
	 * @param file    context to the midi file
	 */
	public Saxophone(Midis2jam2 context, MidiFile file) {
		super(context);
		
	}
}