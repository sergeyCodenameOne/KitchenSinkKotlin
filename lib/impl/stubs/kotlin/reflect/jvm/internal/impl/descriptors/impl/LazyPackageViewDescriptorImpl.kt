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

package kotlin.reflect.jvm.internal.impl.descriptors.impl

import kotlin.reflect.jvm.internal.impl.descriptors.DeclarationDescriptor
import kotlin.reflect.jvm.internal.impl.descriptors.DeclarationDescriptorVisitor
import kotlin.reflect.jvm.internal.impl.descriptors.PackageFragmentDescriptor
import kotlin.reflect.jvm.internal.impl.descriptors.PackageViewDescriptor
import kotlin.reflect.jvm.internal.impl.descriptors.annotations.Annotations
import kotlin.reflect.jvm.internal.impl.name.FqName
import kotlin.reflect.jvm.internal.impl.resolve.scopes.ChainedMemberScope
import kotlin.reflect.jvm.internal.impl.resolve.scopes.LazyScopeAdapter
import kotlin.reflect.jvm.internal.impl.resolve.scopes.MemberScope
import kotlin.reflect.jvm.internal.impl.storage.StorageManager
import kotlin.reflect.jvm.internal.impl.storage.getValue
import kotlin.reflect.jvm.internal.impl.types.TypeSubstitutor

class LazyPackageViewDescriptorImpl(
        override val module: ModuleDescriptorImpl,
        override val fqName: FqName,
        storageManager: StorageManager
) : DeclarationDescriptorImpl(Annotations.EMPTY, fqName.shortNameOrSpecial()), PackageViewDescriptor {

    override val fragments: List<PackageFragmentDescriptor> by storageManager.createLazyValue {
        module.packageFragmentProvider.getPackageFragments(fqName)
    }

    override val memberScope: MemberScope = LazyScopeAdapter(storageManager.createLazyValue {
        if (fragments.isEmpty()) {
            MemberScope.Empty
        }
        else {
            // Packages from SubpackagesScope are got via getContributedDescriptors(DescriptorKindFilter.PACKAGES, MemberScope.ALL_NAME_FILTER)
            val scopes = fragments.map { it.getMemberScope() } + SubpackagesScope(module, fqName)
            ChainedMemberScope("package view scope for $fqName in ${module.name}", scopes)
        }
    })

    override fun getContainingDeclaration(): PackageViewDescriptor? {
        return if (fqName.isRoot) null else module.getPackage(fqName.parent())
    }

    override fun equals(other: Any?): Boolean {
        val that = other as? PackageViewDescriptor ?: return false
        return this.fqName == that.fqName && this.module == that.module
    }

    override fun hashCode(): Int {
        var result = module.hashCode()
        result = 31 * result + fqName.hashCode()
        return result
    }

    override fun substitute(substitutor: TypeSubstitutor): DeclarationDescriptor? = this

    override fun <R, D> accept(visitor: DeclarationDescriptorVisitor<R, D>, data: D): R = visitor.visitPackageViewDescriptor(this, data)
}
