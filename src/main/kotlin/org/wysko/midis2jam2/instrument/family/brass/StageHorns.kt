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
package org.wysko.midis2jam2.instrument.family.brass

import com.jme3.math.Vector3f
import org.wysko.midis2jam2.Midis2jam2
import org.wysko.midis2jam2.instrument.DivisiveSustainedInstrument
import org.wysko.midis2jam2.instrument.PitchClassAnimator
import org.wysko.midis2jam2.instrument.RisingPitchClassAnimator
import org.wysko.midis2jam2.midi.MidiChannelSpecificEvent
import org.wysko.midis2jam2.midi.notePeriodsModulus
import org.wysko.midis2jam2.util.loc
import org.wysko.midis2jam2.util.node
import org.wysko.midis2jam2.util.plus
import org.wysko.midis2jam2.util.plusAssign
import org.wysko.midis2jam2.util.rot
import org.wysko.midis2jam2.util.times
import org.wysko.midis2jam2.util.unaryPlus
import org.wysko.midis2jam2.util.v3
import org.wysko.midis2jam2.world.modelR

private val BASE_POSITION = Vector3f(0f, 29.5f, -152.65f)

/**
 * The stage horns are a set of twelve horns that are arranged in an arc at the back of the stage.
 *
 * @param context The context to the main class.
 * @param eventList The list of all events that this instrument should be aware of.
 * @param type The type of stage horns to use.
 */
class StageHorns(context: Midis2jam2, eventList: List<MidiChannelSpecificEvent>, type: StageHornsType) :
    DivisiveSustainedInstrument(context, eventList, false) {

    override val animators: Array<PitchClassAnimator> =
        Array(12) {
            RisingPitchClassAnimator(context, eventList.notePeriodsModulus(context, it)).apply {
                geometry += context.modelR("StageHorn.obj", type.texture)
            }
        }

    init {
        val hornNodes = Array(12) {
            with(geometry) {
                +node {
                    +animators[it].root
                }
            }
        }

        hornNodes.forEachIndexed { i, node ->
            animators[i].root.loc = BASE_POSITION
            node.rot = v3(0f, 16 + i * 1.5, 0f)
        }
    }

    override fun adjustForMultipleInstances(delta: Float) {
        val index = updateInstrumentIndex(delta)
        animators.forEach {
            it.root.loc = BASE_POSITION + v3(0f, 3f, if (index >= 0) -5f else 5f) * index
        }
    }

    /**
     * A type of stage horns.
     */
    enum class StageHornsType(internal val texture: String) {
        /**
         * The default stage horns.
         */
        BrassSection("HornSkin.bmp"),

        /**
         * The stage horns used for "Synth Brass 1".
         */
        SynthBrass1("HornSkinGrey.bmp"),

        /**
         * The stage horns used for "Synth Brass 2".
         */
        SynthBrass2("HornSkinCopper.png"),
    }
}
