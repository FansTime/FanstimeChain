/*
 * This file is part of the fti library.
 *
 * The fti library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The fti library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the fti library. If not, see <http://www.gnu.org/licenses/>.
 */package com.fanstime.fti.vm;

/**
 * Created by Tony Hunt on 07.07.2018.
 *
 * Factory used to create {@link VMHook} objects
 */
public interface VMHookFactory {
    /**
     * Creates {@link VMHook}
     */
    VMHook create();
}
