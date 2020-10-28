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

package kotlin.reflect.jvm.internal.impl.resolve.scopes

import kotlin.reflect.jvm.internal.impl.descriptors.ClassDescriptor
import kotlin.reflect.jvm.internal.impl.descriptors.ClassKind
import kotlin.reflect.jvm.internal.impl.descriptors.SimpleFunctionDescriptor
import kotlin.reflect.jvm.internal.impl.incremental.components.LookupLocation
import kotlin.reflect.jvm.internal.impl.name.Name
import kotlin.reflect.jvm.internal.impl.resolve.DescriptorFactory.createEnumValueOfMethod
import kotlin.reflect.jvm.internal.impl.resolve.DescriptorFactory.createEnumValuesMethod
import kotlin.reflect.jvm.internal.impl.storage.StorageManager
import kotlin.reflect.jvm.internal.impl.storage.getValue
import kotlin.reflect.jvm.internal.impl.utils.Printer
import java.util.*

// We don't need to track lookups here since this scope used only for introduce special Enum class members
class StaticScopeForKotlinEnum(
        storageManager: StorageManager,
        private val containingClass: ClassDescriptor
) : MemberScopeImpl() {
    init {
        assert(containingClass.kind == ClassKind.ENUM_CLASS) { "Class should be an enum: $containingClass" }
    }

    override fun getContributedClassifier(name: Name, location: LookupLocation) = null // TODO

    private val functions: List<SimpleFunctionDescriptor> by storageManager.createLazyValue {
        listOf(createEnumValueOfMethod(containingClass), createEnumValuesMethod(containingClass))
    }

    override fun getContributedDescriptors(kindFilter: DescriptorKindFilter, nameFilter: (Name) -> Boolean) = functions

    override fun getContributedFunctions(name: Name, location: LookupLocation) =
            functions.filterTo(ArrayList<SimpleFunctionDescriptor>(1)) { it.name == name }

    override fun printScopeStructure(p: Printer) {
        p.println("Static scope for $containingClass")
    }
}
