package org.wysko.midis2jam2.instrument;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import org.jetbrains.annotations.NotNull;
import org.wysko.midis2jam2.Midis2jam2;
import org.wysko.midis2jam2.instrument.monophonic.MonophonicClone;
import org.wysko.midis2jam2.instrument.monophonic.MonophonicInstrument;
import org.wysko.midis2jam2.midi.MidiChannelSpecificEvent;
import org.wysko.midis2jam2.midi.MidiNoteEvent;
import org.wysko.midis2jam2.midi.MidiNoteOffEvent;
import org.wysko.midis2jam2.midi.MidiNoteOnEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An <i>Instrument</i> is any visual representation of a MIDI instrument. midis2jam2 displays separate instruments
 * for each channel, and also creates new instruments when the program of a channel changes (i.e., the MIDI
 * instrument of the channel changes).
 * <p>
 * Classes that implement Instrument are responsible for handling {@link #tick}, which updates the
 * current animation and note handling for every call.
 *
 * @see MonophonicInstrument
 * @see MonophonicClone
 */
public abstract class Instrument {
	/**
	 * Since these classes are effectively static, we need reference to the main class.
	 */
	protected final Midis2jam2 context;
	/**
	 * When true, this instrument should be displayed on the screen. Otherwise, it should not. The positions of
	 * instruments rely on this variable (if bass guitar 1 hides after a while, bass guitar 2 should step in to fill
	 * its spot).
	 */
	public boolean visible = false;
	/**
	 * This node should contain all animation and geometry. It shall be only used for general positioning.
	 */
	public Node highestLevel = new Node();
	
	/**
	 * Instantiates a new Instrument.
	 *
	 * @param context the context to the main class
	 */
	protected Instrument(Midis2jam2 context) {
		this.context = context;
	}
	
	/**
	 * Filters a list of MIDI channel specific events and returns only the {@link MidiNoteEvent}s.
	 *
	 * @param events the event list
	 * @return only the MidiNoteEvents
	 * @see MidiNoteEvent
	 */
	@NotNull
	protected static List<MidiNoteEvent> scrapeMidiNoteEvents(List<MidiChannelSpecificEvent> events) {
		return events.stream().filter(e -> e instanceof MidiNoteEvent).map(e -> ((MidiNoteEvent) e)).collect(Collectors.toList());
	}
	
	/**
	 * Updates the animation and other necessary frame-dependant calculations.
	 *
	 * @param time  the current time since the beginning of the MIDI file, expressed in seconds
	 * @param delta the amount of time since the last call this method, expressed in seconds
	 */
	public abstract void tick(double time, float delta);
	
	/**
	 * A MIDI file is a sequence of {@link MidiNoteOnEvent}s and {@link MidiNoteOffEvent}s. This method searches the
	 * files and connects corresponding events together. This is effectively calculating the "blocks" you would see
	 * in a piano roll editor.
	 *
	 * @param noteEvents the note events to calculate NotePeriods from
	 */
	protected List<NotePeriod> calculateNotePeriods(List<MidiNoteEvent> noteEvents) {
		List<NotePeriod> notePeriods = new ArrayList<>();
		for (int i = 0, noteEventsSize = noteEvents.size(); i < noteEventsSize; i++) {
			MidiNoteEvent noteEvent = noteEvents.get(i);
			if (noteEvent instanceof MidiNoteOnEvent) {
				for (int j = i + 1; j < noteEventsSize; j++) {
					MidiNoteEvent check = noteEvents.get(j);
					if (check instanceof MidiNoteOffEvent && check.note == noteEvent.note) {
						// We found a block
						notePeriods.add(new NotePeriod(check.note, context.file.eventInSeconds(noteEvent),
								context.file.eventInSeconds(check), ((MidiNoteOnEvent) noteEvent)
								, ((MidiNoteOffEvent) check)));
						break;
					}
				}
			}
		}
		for (int i = notePeriods.size() - 2; i >= 0; i--) {
			final NotePeriod a = notePeriods.get(i + 1);
			final NotePeriod b = notePeriods.get(i);
			if (a.startTick() == b.startTick() &&
					a.endTick() == b.endTick() &&
					a.midiNote == b.midiNote) {
				notePeriods.remove(i + 1);
			}
		}
		return notePeriods;
	}
	
	/**
	 * Determines whether this instrument should be visible at the time, and sets the visibility accordingly.
	 * <p>
	 * The instrument should be visible if:
	 * <ul>
	 *     <li>There is at least 1 second between now and the start of any note period,</li>
	 *     <li>There is at least 4 seconds between now and the end of any note period, or</li>
	 *     <li>Any note period is currently playing</li>
	 * </ul>
	 *
	 * @param notePeriods the note periods to check from
	 * @param time        the current time
	 * @param node        the node to hide
	 */
	protected void setIdleVisibilityByPeriods(List<? extends NotePeriod> notePeriods, double time, Node node) {
		boolean show = false;
		for (NotePeriod notePeriod : notePeriods) {
			// Within 1 second of a note on,
			// within 4 seconds of a note off,
			// or during a note, be visible
			if (notePeriod.isPlayingAt(time)
					|| Math.abs(time - notePeriod.startTime) < 1
					|| (Math.abs(time - notePeriod.endTime) < 4 && time > notePeriod.endTime)) {
				visible = true;
				show = true;
				break;
			} else {
				visible = false;
			}
		}
		node.setCullHint(show ? Spatial.CullHint.Dynamic : Spatial.CullHint.Always);
	}
	
	/**
	 * Determines whether this instrument should be visible at the time, and sets the visibility accordingly.
	 * <p>
	 * The instrument should be visible if:
	 * <ul>
	 *     <li>There is at least 1 second between now and any strike, when the strike comes later, or,</li>
	 *     <li>There is at least 4 seconds between now and any strike, when the strike has elapsed</li>
	 * </ul>
	 *
	 * @param strikes the note on events to check from
	 * @param time    the current time
	 * @param node    the node to hide
	 */
	protected void setIdleVisibilityByStrikes(List<MidiNoteOnEvent> strikes, double time, Node node) {
		boolean show = false;
		for (MidiNoteOnEvent strike : strikes) {
			double x = time - context.file.eventInSeconds(strike);
			if (x < 4 && x > -1) {
				visible = true;
				show = true;
				break;
			} else {
				visible = false;
			}
		}
		node.setCullHint(show ? Spatial.CullHint.Dynamic : Spatial.CullHint.Always);
	}
	
	/**
	 * Returns the index of this instrument in the list of other instruments of this type that are visible.
	 *
	 * @return the index of this instrument in the list of other instruments of this type that are visible
	 */
	protected int getIndexOfThis() {
		return context.instruments.stream()
				.filter(e -> this.getClass().isInstance(e) && e.visible)
				.collect(Collectors.toList()).indexOf(this);
	}
}
