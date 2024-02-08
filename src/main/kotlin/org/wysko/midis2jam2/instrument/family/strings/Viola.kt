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
package org.wysko.midis2jam2.instrument.family.strings

import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import org.wysko.midis2jam2.Midis2jam2
import org.wysko.midis2jam2.midi.MidiChannelSpecificEvent
import org.wysko.midis2jam2.util.Utils.rad
import org.wysko.midis2jam2.world.modelD

/** The Viola. */
class Viola(context: Midis2jam2, events: List<MidiChannelSpecificEvent>) : StringFamilyInstrument(
    context,
    events,
    true,
    180.0,
    Vector3f(1f, 1f, 1f),
    intArrayOf(48, 55, 62, 69),
    context.modelD("Violin.obj", "ViolaSkin.bmp")
) {
    override fun adjustForMultipleInstances(delta: Float) {
        root.setLocalTranslation(20f * updateInstrumentIndex(delta), 0f, 0f)
    }

    init {
        placement.setLocalTranslation(-2f, 27f, -15f)
        placement.attachChild(geometry)
        geometry.setLocalScale(1f)
        geometry.localRotation = Quaternion().fromAngles(rad(-130.0), rad(-174.0), rad(-28.1))
    }
}
