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

package kotlin.reflect.jvm.internal.impl.descriptors.impl;

import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import kotlin.reflect.jvm.internal.impl.descriptors.*;
import kotlin.reflect.jvm.internal.impl.descriptors.annotations.Annotations;
import kotlin.reflect.jvm.internal.impl.name.Name;
import kotlin.reflect.jvm.internal.impl.resolve.DescriptorUtils;
import kotlin.reflect.jvm.internal.impl.storage.LockBasedStorageManager;
import kotlin.reflect.jvm.internal.impl.types.KotlinType;
import kotlin.reflect.jvm.internal.impl.types.Variance;

import java.util.ArrayList;
import java.util.List;

import static kotlin.reflect.jvm.internal.impl.resolve.descriptorUtil.DescriptorUtilsKt.getBuiltIns;

public class TypeParameterDescriptorImpl extends AbstractTypeParameterDescriptor {
    @Nullable
    private final Function1<KotlinType, Void> reportCycleError;

    public static TypeParameterDescriptor createWithDefaultBound(
            @NotNull DeclarationDescriptor containingDeclaration,
            @NotNull Annotations annotations,
            boolean reified,
            @NotNull Variance variance,
            @NotNull Name name,
            int index
    ) {
        TypeParameterDescriptorImpl typeParameterDescriptor =
                createForFurtherModification(containingDeclaration, annotations, reified, variance, name, index, SourceElement.NO_SOURCE);
        typeParameterDescriptor.addUpperBound(getBuiltIns(containingDeclaration).getDefaultBound());
        typeParameterDescriptor.setInitialized();
        return typeParameterDescriptor;
    }

    public static TypeParameterDescriptorImpl createForFurtherModification(
            @NotNull DeclarationDescriptor containingDeclaration,
            @NotNull Annotations annotations,
            boolean reified,
            @NotNull Variance variance,
            @NotNull Name name,
            int index,
            @NotNull SourceElement source
    ) {
        return createForFurtherModification(containingDeclaration, annotations, reified, variance, name, index, source,
                                            /* reportSupertypeLoopError = */ null, SupertypeLoopChecker.EMPTY.INSTANCE);
    }

    public static TypeParameterDescriptorImpl createForFurtherModification(
            @NotNull DeclarationDescriptor containingDeclaration,
            @NotNull Annotations annotations,
            boolean reified,
            @NotNull Variance variance,
            @NotNull Name name,
            int index,
            @NotNull SourceElement source,
            @Nullable Function1<KotlinType, Void> reportCycleError,
            @NotNull SupertypeLoopChecker supertypeLoopsResolver
    ) {
        return new TypeParameterDescriptorImpl(containingDeclaration, annotations, reified, variance, name, index, source, reportCycleError,
                                               supertypeLoopsResolver);
    }

    private final List<KotlinType> upperBounds = new ArrayList<KotlinType>(1);
    private boolean initialized = false;

    private TypeParameterDescriptorImpl(
            @NotNull DeclarationDescriptor containingDeclaration,
            @NotNull Annotations annotations,
            boolean reified,
            @NotNull Variance variance,
            @NotNull Name name,
            int index,
            @NotNull SourceElement source,
            @Nullable Function1<KotlinType, Void> reportCycleError,
            @NotNull SupertypeLoopChecker supertypeLoopsChecker
    ) {
        super(LockBasedStorageManager.NO_LOCKS, containingDeclaration, annotations, name, variance, reified, index, source,
              supertypeLoopsChecker);
        this.reportCycleError = reportCycleError;
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Type parameter descriptor is not initialized: " + nameForAssertions());
        }
    }

    private void checkUninitialized() {
        if (initialized) {
            throw new IllegalStateException("Type parameter descriptor is already initialized: " + nameForAssertions());
        }
    }

    private String nameForAssertions() {
        return getName() + " declared in " + DescriptorUtils.getFqName(getContainingDeclaration());
    }

    public void setInitialized() {
        checkUninitialized();
        initialized = true;
    }

    public void addUpperBound(@NotNull KotlinType bound) {
        checkUninitialized();
        doAddUpperBound(bound);
    }

    private void doAddUpperBound(KotlinType bound) {
        if (bound.isError()) return;
        upperBounds.add(bound); // TODO : Duplicates?
    }

    public void addDefaultUpperBound() {
        checkUninitialized();

        if (upperBounds.isEmpty()) {
            doAddUpperBound(getBuiltIns(getContainingDeclaration()).getDefaultBound());
        }
    }

    @Override
    protected void reportSupertypeLoopError(@NotNull KotlinType type) {
        if (reportCycleError == null) return;
        reportCycleError.invoke(type);
    }

    @NotNull
    @Override
    protected List<KotlinType> resolveUpperBounds() {
        checkInitialized();
        return upperBounds;
    }
}
