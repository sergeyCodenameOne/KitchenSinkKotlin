/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package kotlin.reflect.jvm.internal.impl.builtins

import kotlin.reflect.jvm.internal.impl.descriptors.ModuleDescriptor
import kotlin.reflect.jvm.internal.impl.name.FqName
import kotlin.reflect.jvm.internal.impl.serialization.builtins.BuiltInsProtoBuf
import kotlin.reflect.jvm.internal.impl.serialization.deserialization.DeserializedPackageFragment
import kotlin.reflect.jvm.internal.impl.serialization.deserialization.NameResolverImpl
import kotlin.reflect.jvm.internal.impl.serialization.deserialization.descriptors.DeserializedPackageMemberScope
import kotlin.reflect.jvm.internal.impl.storage.StorageManager
import java.io.InputStream

class BuiltInsPackageFragment(
        fqName: FqName,
        storageManager: StorageManager,
        module: ModuleDescriptor,
        loadResource: (path: String) -> InputStream?
) : DeserializedPackageFragment(fqName, storageManager, module, loadResource) {
    private val proto = loadResourceSure(BuiltInSerializerProtocol.getBuiltInsFilePath(fqName)).use { stream ->
        val version = BuiltInsBinaryVersion.readFrom(stream)

        if (!version.isCompatible()) {
            // TODO: report a proper diagnostic
            throw UnsupportedOperationException(
                    "Kotlin built-in definition format version is not supported: " +
                    "expected ${BuiltInsBinaryVersion.INSTANCE}, actual $version. " +
                    "Please update Kotlin"
            )
        }

        BuiltInsProtoBuf.BuiltIns.parseFrom(stream, BuiltInSerializerProtocol.extensionRegistry)
    }

    private val nameResolver = NameResolverImpl(proto.strings, proto.qualifiedNames)

    override val classDataFinder = BuiltInsClassDataFinder(proto, nameResolver)

    override fun computeMemberScope() =
            DeserializedPackageMemberScope(
                    this, proto.`package`, nameResolver, containerSource = null, components = components,
                    classNames = { classDataFinder.allClassIds.filter { classId -> !classId.isNestedClass }.map { it.shortClassName } }
            )
}
