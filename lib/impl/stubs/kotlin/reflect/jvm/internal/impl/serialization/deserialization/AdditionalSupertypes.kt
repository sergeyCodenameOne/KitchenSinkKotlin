/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kotlin.reflect.jvm.internal.impl.serialization.deserialization

import kotlin.reflect.jvm.internal.impl.serialization.deserialization.descriptors.DeserializedClassDescriptor
import kotlin.reflect.jvm.internal.impl.types.KotlinType

interface AdditionalSupertypes {
    fun forClass(classDescriptor: DeserializedClassDescriptor): Collection<KotlinType>

    object None : AdditionalSupertypes {
        override fun forClass(classDescriptor: DeserializedClassDescriptor): Collection<KotlinType> = emptyList()
    }
}