package org.wysko.midis2jam2.instrument.percussive;

import com.jme3.math.FastMath;
import org.jetbrains.annotations.Range;
import org.wysko.midis2jam2.Midis2jam2;
import org.wysko.midis2jam2.instrument.Instrument;
import org.wysko.midis2jam2.midi.MidiNoteOnEvent;

import java.util.List;

public abstract class Drum extends Instrument {
	static final float DRUM_RECOIL_COMEBACK = 22;
	protected final List<MidiNoteOnEvent> hits;
	
	protected Drum(Midis2jam2 context, List<MidiNoteOnEvent> hits) {
		super(context);
		this.hits = hits;
	}
	
	/**
	 * midis2jam2 displays velocity ramping in recoiled instruments. Different functions may be used, but a sqrt
	 * regression looks pretty good. May adjust this in the future.
	 * <a href="https://www.desmos.com/calculator/17rgvqhl84">See a graph.</a>
	 *
	 * @param x the velocity of the note
	 * @return a percentage to multiply by the target recoil
	 */
	@Range(from = 0, to = 1)
	double velocityRecoilDampening(@Range(from = 0, to = 127) int x) {
		return FastMath.sqrt(x) / 11.26942767f;
	}
}
