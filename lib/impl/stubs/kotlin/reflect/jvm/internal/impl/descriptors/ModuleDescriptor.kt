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

package kotlin.reflect.jvm.internal.impl.descriptors

import kotlin.reflect.jvm.internal.impl.builtins.KotlinBuiltIns
import kotlin.reflect.jvm.internal.impl.name.FqName
import kotlin.reflect.jvm.internal.impl.name.Name
import kotlin.reflect.jvm.internal.impl.resolve.ImportPath
import kotlin.reflect.jvm.internal.impl.types.TypeSubstitutor

interface ModuleDescriptor : DeclarationDescriptor {
    override fun getContainingDeclaration(): DeclarationDescriptor? = null

    val builtIns: KotlinBuiltIns

    fun shouldSeeInternalsOf(targetModule: ModuleDescriptor): Boolean

    override fun substitute(substitutor: TypeSubstitutor): ModuleDescriptor {
        return this
    }

    override fun <R, D> accept(visitor: DeclarationDescriptorVisitor<R, D>, data: D): R {
        return visitor.visitModuleDeclaration(this, data)
    }

    fun getPackage(fqName: FqName): PackageViewDescriptor

    fun getSubPackagesOf(fqName: FqName, nameFilter: (Name) -> Boolean): Collection<FqName>

    val defaultImports: List<ImportPath>

    fun <T> getCapability(capability: Capability<T>): T?

    class Capability<T>(val name: String)
}
