/*
 * Copyright (C) 2024 Jacob Wysko
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
package org.wysko.midis2jam2.midi

import kotlin.math.max

/** The max value a MIDI note can have. */
const val MIDI_MAX_NOTE: Int = 127

/**
 * A [MidiNoteOnEvent] or a [MidiNoteOffEvent].
 *
 * @param note the note value
 */
open class MidiNoteEvent protected constructor(override val time: Long, channel: Int, open val note: Int) :
    MidiChannelSpecificEvent(time, channel)

/**
 * Determines the maximum number of notes playing at any given time (polyphony).
 */
fun List<MidiNoteEvent>.maxPolyphony(): Int {
    var currentPolyphony = 0
    var maxPolyphony = 0
    forEach {
        if (it is MidiNoteOnEvent) currentPolyphony++ else currentPolyphony--
        maxPolyphony = max(currentPolyphony, maxPolyphony)
    }
    return maxPolyphony
}

/**
 * Filters the list of [MidiNoteEvent]s by the specified notes.
 *
 * @param notes the notes to filter by
 * @return a new mutable list containing the filtered [MidiNoteEvent]s
 */
fun <T : MidiNoteEvent> List<T>.byNote(vararg notes: Int): List<T> = filter { notes.contains(it.note) }.toMutableList()
