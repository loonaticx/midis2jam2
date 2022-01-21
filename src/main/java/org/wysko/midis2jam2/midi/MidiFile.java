/*
 * Copyright (C) 2022 Jacob Wysko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.wysko.midis2jam2.midi;

import org.jetbrains.annotations.NotNull;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A special type of music file.
 */
public final class MidiFile {
	
	/**
	 * The tracks of this MIDI file.
	 */
	private MidiTrack[] tracks;
	
	/**
	 * The division, expressed as ticks per quarter-note.
	 */
	private short division;
	
	/**
	 * A list of tempos that occur in this MIDI file.
	 */
	private List<MidiTempoEvent> tempos = new ArrayList<>();
	
	private HashMap<MidiEvent, Double> eventToTime = new HashMap<>();
	
	public MidiFile() {
		// Populated in implementation
	}
	
	public double length;
	
	public String name;
	
	/**
	 * Reads a MIDI file and parses pertinent information.
	 *
	 * @param midiFile the system file of the MIDI file
	 * @return the MIDI file
	 * @throws IOException              an i/o error occurred
	 * @throws InvalidMidiDataException if the MIDI file is bad
	 */
	@SuppressWarnings({"StatementWithEmptyBody", "java:S1160"})
	public static MidiFile readMidiFile(File midiFile) throws IOException, InvalidMidiDataException {
		/* Init vars */
		Sequence sequence = new StandardMidiFileReader().getSequence(midiFile);
		MidiFile file = new MidiFile();
		
		/* Division and track count */
		file.setDivision((short) sequence.getResolution());
		file.setTracks(new MidiTrack[sequence.getTracks().length + 1]);
		
		/* For each track */
		for (int j = 1; j <= sequence.getTracks().length; j++) {
			file.getTracks()[j] = new MidiTrack();
			Track track = sequence.getTracks()[j - 1];
			
			for (int i = 0; i < track.size(); i++) {
				javax.sound.midi.MidiEvent midiEvent = track.get(i);
				if (midiEvent.getMessage() instanceof MetaMessage) {
					MetaMessage message = (MetaMessage) midiEvent.getMessage();
					if (message.getType() == 0x51) { // Tempo
						byte[] data = message.getData();
						int tempo = (((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8)) | (data[2] & 0xff);
						file.getTracks()[j].getEvents().add(new MidiTempoEvent(midiEvent.getTick(), tempo));
					} else if (message.getType() == 0x01 || message.getType() == 0x05) { // Text
						file.getTracks()[j].getEvents().add(new MidiTextEvent(midiEvent.getTick(), new String(message.getData())));
					}
				} else if (midiEvent.getMessage() instanceof ShortMessage) {
					ShortMessage message = (ShortMessage) midiEvent.getMessage();
					int command = message.getCommand();
					if (command == ShortMessage.NOTE_ON) {
						if (message.getData2() == 0) {
							file.getTracks()[j].getEvents().add(midiNoteOffFromData(midiEvent.getTick(), message.getData1(), message.getChannel()));
						} else {
							file.getTracks()[j].getEvents().add(midiNoteOnFromData(midiEvent.getTick(), message.getData1(), message.getData2(), message.getChannel()));
						}
					} else if (command == ShortMessage.NOTE_OFF) {
						file.getTracks()[j].getEvents().add(midiNoteOffFromData(midiEvent.getTick(), message.getData1(), message.getChannel()));
					} else if (command == ShortMessage.PROGRAM_CHANGE) {
						file.getTracks()[j].getEvents().add(programEventFromData(midiEvent.getTick(), message.getData1(), message.getChannel()));
					} else if (command == ShortMessage.PITCH_BEND) {
						int val = message.getData1() + message.getData2() * 128;
						file.getTracks()[j].getEvents().add(new MidiPitchBendEvent(midiEvent.getTick(), message.getChannel(), val));
					} else if (command == ShortMessage.CONTROL_CHANGE) {
						file.getTracks()[j].getEvents().add(new MidiControlEvent(midiEvent.getTick(), message.getChannel(), message.getData1(), message.getData2()));
					} else {
						// Ignore message
					}
				} else {
					// Ignore message
				}
			}
		}
		file.calculateTempoMap();
		for (MidiTrack track : file.getTracks()) {
			if (track != null) {
				for (MidiEvent event : track.getEvents()) {
					file.eventToTime.put(event, file.eventInSeconds(event.getTime()));
				}
			}
		}
		
		/* Calculate length */
		long max = 0;
		for (MidiTrack track : file.getTracks()) {
			if (track != null) {
				for (MidiEvent event : track.getEvents()) {
					if (event.getTime() > max) {
						max = event.getTime();
					}
				}
			}
		}
		file.length = file.eventInSeconds(max);
		file.name = midiFile.getName();
		return file;
	}
	
	/**
	 * Given data, returns a {@link MidiNoteOnEvent}.
	 *
	 * @param tick     the MIDI tick
	 * @param note     the note
	 * @param velocity the velocity
	 * @param channel  the channel
	 * @return a new {@link MidiNoteOnEvent}
	 */
	@NotNull
	private static MidiNoteOnEvent midiNoteOnFromData(long tick, int note, int velocity, int channel) {
		return new MidiNoteOnEvent(tick, channel, note, velocity);
	}
	
	/**
	 * Given data, returns a {@link MidiNoteOffEvent}.
	 *
	 * @param tick    the MIDI tick
	 * @param note    the note
	 * @param channel the channel
	 * @return a new {@link MidiNoteOffEvent}
	 */
	@NotNull
	private static MidiNoteOffEvent midiNoteOffFromData(long tick, int note, int channel) {
		return new MidiNoteOffEvent(tick, channel, note);
	}
	
	/**
	 * Given data, returns a {@link MidiProgramEvent}.
	 *
	 * @param tick    the MIDI tick
	 * @param preset  the preset
	 * @param channel the channel
	 * @return a new {@link MidiProgramEvent}
	 */
	@NotNull
	private static MidiProgramEvent programEventFromData(long tick, int preset, int channel) {
		return new MidiProgramEvent(tick, channel, preset);
	}
	
	/**
	 * @return the first tempo event in the file, expressed in beats per minute
	 */
	public double firstTempoInBpm() {
		MidiTempoEvent event = new MidiTempoEvent(0, 500_000);
		
		/* For each track */
		for (int i = 1; i < getTracks().length; i++) {
			MidiTrack track = getTracks()[i];
			
			event = track.getEvents().stream().filter(MidiTempoEvent.class::isInstance).findFirst().map(MidiTempoEvent.class::cast).orElse(event);
		}
		return 6E7 / event.getNumber();
	}
	
	/**
	 * Calculates the tempo map of this MIDI file.
	 */
	public void calculateTempoMap() {
		List<MidiTempoEvent> tempoEvents = new ArrayList<>();
		
		/* For each MIDI track */
		for (MidiTrack track : getTracks()) {
			/* Skip if null */
			if (track == null) continue;
			
			tempoEvents.addAll(track.getEvents().stream().filter(MidiTempoEvent.class::isInstance).map(MidiTempoEvent.class::cast).collect(Collectors.toList()));
		}
		if (tempoEvents.isEmpty()) {
			tempoEvents.add(new MidiTempoEvent(0, 500_000));
		}
		tempoEvents.sort(Comparator.comparingLong(MidiTempoEvent::getTime));
		
		/* Remove overlapping tempos (fuck you if you have two different tempos at the same time) */
		for (int i = 0, numberOfTempoEvents = tempoEvents.size(); i < numberOfTempoEvents; i++) {
			while (i < tempoEvents.size() - 1 && tempoEvents.get(i).getTime() == tempoEvents.get(i + 1).getTime()) {
				tempoEvents.remove(i);
			}
		}
		this.tempos = tempoEvents;
	}
	
	/**
	 * Given a MIDI tick, returns the tick as expressed in seconds, calculated by the tempo map of this MIDI file. If
	 * the MIDI tick value is negative, the method uses the first tempo and extrapolates backwards.
	 *
	 * @param midiTick the MIDI tick to convert to seconds
	 * @return the tick as expressed in seconds
	 */
	public double midiTickInSeconds(long midiTick) {
		List<MidiTempoEvent> temposToConsider = new ArrayList<>();
		if (midiTick >= 0) {
			for (MidiTempoEvent tempo : getTempos()) {
				if (tempo.getTime() <= midiTick) {
					temposToConsider.add(tempo);
				}
			}
		} else {
			temposToConsider.add(getTempos().get(0));
		}
		if (temposToConsider.isEmpty()) {
			temposToConsider.add(new MidiTempoEvent(0, 500_000));
		}
		if (temposToConsider.size() == 1) {
			return ((double) midiTick / getDivision()) * (60 / (6E7 / temposToConsider.get(0).getNumber()));
		}
		double seconds = 0;
		for (int i = 0; i < temposToConsider.size() - 1; i++) {
			seconds += ((double) (temposToConsider.get(i + 1).getTime() - temposToConsider.get(i).getTime()) / getDivision()) * (60 / (6E7 / temposToConsider.get(i).getNumber()));
		}
		MidiTempoEvent lastTempo = temposToConsider.get(temposToConsider.size() - 1);
		seconds += ((double) (midiTick - lastTempo.getTime()) / getDivision()) * (60 / (6E7 / lastTempo.getNumber()));
		return seconds;
	}
	
	/**
	 * Converts a MIDI event into its time in seconds.
	 *
	 * @param event a {@link MidiEvent}
	 * @return the event's time, expressed in seconds
	 */
	public double eventInSeconds(MidiEvent event) {
		final Double time = eventToTime.get(event);
		if (time != null) {
			return time;
		} else {
			return eventInSeconds(event.getTime());
		}
	}
	
	/**
	 * Converts a MIDI event into its time in seconds.
	 *
	 * @param time the MIDI tick of the event
	 * @return the event's time, expressed in seconds
	 */
	public double eventInSeconds(long time) {
		return midiTickInSeconds(time);
	}
	
	/**
	 * Determines the tempo that is effective just before an event.
	 *
	 * @param event the event
	 * @return the effective tempo before the event
	 */
	@SuppressWarnings("java:S2325")
	public MidiTempoEvent tempoBefore(MidiEvent event) {
		return tempoBefore(event.getTime());
	}
	
	public MidiTempoEvent tempoBefore(long tick) {
		MidiTempoEvent lastTempo = getTempos().get(0);
		if (getTempos().size() > 1) {
			for (MidiTempoEvent tempo : getTempos()) {
				if (tempo.getTime() < tick) {
					lastTempo = tempo;
				} else {
					return lastTempo;
				}
			}
		}
		return lastTempo;
	}
	
	public MidiTempoEvent tempoAt(long tick) {
		MidiTempoEvent lastTempo = getTempos().get(0);
		if (getTempos().size() > 1) {
			for (MidiTempoEvent tempo : getTempos()) {
				if (tempo.getTime() <= tick) {
					lastTempo = tempo;
				} else {
					return lastTempo;
				}
			}
		}
		return lastTempo;
	}
	
	/**
	 * Given a time, determines the current tempo at the time.
	 *
	 * @param time the time
	 * @return the tempo, expressed in MIDI format
	 */
	public int tempoAt(double time) {
		if (getTempos().size() == 1) {
			return tempos.get(0).getNumber();
		}
		for (int i = 1; i < getTempos().size(); i++) {
			if (eventToTime.get(tempos.get(i)) > time) {
				return tempos.get(i - 1).getNumber();
			}
		}
		return 0;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MidiFile midiFile = (MidiFile) o;
		return getDivision() == midiFile.getDivision() && Arrays.equals(getTracks(), midiFile.getTracks()) && Objects.equals(getTempos(), midiFile.getTempos());
	}
	
	@Override
	public int hashCode() {
		int result = Objects.hash(getDivision(), getTempos());
		result = 31 * result + Arrays.hashCode(getTracks());
		return result;
	}
	
	public List<MidiTempoEvent> getTempos() {
		return tempos;
	}
	
	public short getDivision() {
		return division;
	}
	
	public void setDivision(short division) {
		this.division = division;
	}
	
	public MidiTrack[] getTracks() {
		return tracks;
	}
	
	public void setTracks(MidiTrack[] tracks) {
		this.tracks = tracks;
	}
}
