package org.wysko.midis2jam2.instrument.organ;

import com.jme3.math.Quaternion;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import org.wysko.midis2jam2.Midis2jam2;
import org.wysko.midis2jam2.instrument.SustainedInstrument;
import org.wysko.midis2jam2.midi.MidiChannelSpecificEvent;
import org.wysko.midis2jam2.midi.MidiEvent;
import org.wysko.midis2jam2.midi.MidiNoteOffEvent;
import org.wysko.midis2jam2.midi.MidiNoteOnEvent;
import org.wysko.midis2jam2.particle.SteamPuffer;

import java.util.ArrayList;
import java.util.List;

import static org.wysko.midis2jam2.Midis2jam2.rad;

public class Harmonica extends SustainedInstrument {
	private final Node highestLevel = new Node();
	SteamPuffer[] puffers = new SteamPuffer[12];
	Node[] pufferNodes = new Node[12];
	Node harmonicaNode = new Node();
	Spatial harmonica;
	List<MidiChannelSpecificEvent> events;
	boolean[] activities = new boolean[12];
	
	public Harmonica(Midis2jam2 context, List<MidiChannelSpecificEvent> eventList) {
		super(context, eventList);
		
		
		harmonica = context.loadModel("Harmonica.obj", "Harmonica.bmp", Midis2jam2.MatType.UNSHADED, 0.9f);
		harmonicaNode.attachChild(harmonica);
		
		for (int i = 0; i < 12; i++) {
			pufferNodes[i] = new Node();
			puffers[i] = new SteamPuffer(context, SteamPuffer.SteamPuffType.HARMONICA, 0.75);
			puffers[i].steamPuffNode.setLocalRotation(new Quaternion().fromAngles(0, rad(-90), 0));
			puffers[i].steamPuffNode.setLocalTranslation(0, 0, 7.2f);
			pufferNodes[i].attachChild(puffers[i].steamPuffNode);
			harmonicaNode.attachChild(pufferNodes[i]);
			pufferNodes[i].setLocalRotation(new Quaternion().fromAngles(0, rad(5 * (i - 5.5)), 0));
		}
		
		// Position harmonica
		instrumentNode.setLocalTranslation(74, 32, -38);
		instrumentNode.setLocalRotation(new Quaternion().fromAngles(0, rad(-90), 0));
	}
	
	@Override
	public void tick(double time, float delta) {
		/* Collect note periods to execute */
		setIdleVisibilityByPeriods(time);
		List<MidiEvent> eventsToPerform = new ArrayList<>();
		if (!events.isEmpty()) {
			if (!(events.get(0) instanceof MidiNoteOnEvent) && !(events.get(0) instanceof MidiNoteOffEvent)) {
				events.remove(0);
			}
			while (!events.isEmpty() && ((events.get(0) instanceof MidiNoteOnEvent && context.file.eventInSeconds(events.get(0)) <= time) ||
					(events.get(0) instanceof MidiNoteOffEvent && context.file.eventInSeconds(events.get(0)) - time <= 0.05))) {
				eventsToPerform.add(events.remove(0));
			}
		}
		
		for (MidiEvent event : eventsToPerform) {
			if (event instanceof MidiNoteOnEvent) {
				MidiNoteOnEvent noteOn = (MidiNoteOnEvent) event;
				int i = (noteOn.note + 3) % 12;
				activities[i] = true;
			} else if (event instanceof MidiNoteOffEvent) {
				MidiNoteOffEvent noteOff = (MidiNoteOffEvent) event;
				int i = (noteOff.note + 3) % 12;
				activities[i] = false;
			}
		}
		
		for (int i = 0; i < puffers.length; i++) {
			puffers[i].tick(time, delta, activities[i]);
		}

//		int mySpot =
//				context.instruments.stream().filter(i -> i instanceof Harmonica && i.visible).collect(Collectors.toList()).indexOf(this);
//		harmonicaNode.setLocalTranslation(0, mySpot * 10, 0);
	}
	
	@Override
	protected void moveForMultiChannel() {
		offsetNode.setLocalTranslation(0, 10 * indexForMoving(), 0);
	}
}
