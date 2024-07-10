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

package org.wysko.midis2jam2.world.lyric

import org.wysko.kmidi.midi.event.MetaEvent
import org.wysko.midis2jam2.Midis2jam2
import kotlin.time.Duration

/**
 * A line of lyrics.
 */
internal typealias LyricLine = List<MetaEvent.Lyric>


context(Midis2jam2)
internal val LyricLine.startTime: Duration
    get() = sequence.getTimeOf(minBy { it.tick })

context(Midis2jam2)
internal val LyricLine.endTime: Duration
    get() = sequence.getTimeOf(maxBy { it.tick })
