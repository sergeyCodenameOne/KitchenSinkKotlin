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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import kotlin.reflect.jvm.internal.impl.descriptors.*;
import kotlin.reflect.jvm.internal.impl.descriptors.annotations.Annotations;
import kotlin.reflect.jvm.internal.impl.name.Name;
import kotlin.reflect.jvm.internal.impl.types.TypeSubstitutor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class PropertyAccessorDescriptorImpl extends DeclarationDescriptorNonRootImpl implements PropertyAccessorDescriptor {
    private final boolean isDefault;
    private final boolean isExternal;
    private final Modality modality;
    private final PropertyDescriptor correspondingProperty;
    private final Kind kind;
    private Visibility visibility;
    @Nullable
    private FunctionDescriptor initialSignatureDescriptor = null;

    public PropertyAccessorDescriptorImpl(
            @NotNull Modality modality,
            @NotNull Visibility visibility,
            @NotNull PropertyDescriptor correspondingProperty,
            @NotNull Annotations annotations,
            @NotNull Name name,
            boolean isDefault,
            boolean isExternal,
            Kind kind,
            @NotNull SourceElement source
    ) {
        super(correspondingProperty.getContainingDeclaration(), annotations, name, source);
        this.modality = modality;
        this.visibility = visibility;
        this.correspondingProperty = correspondingProperty;
        this.isDefault = isDefault;
        this.isExternal = isExternal;
        this.kind = kind;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @NotNull
    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public boolean isOperator() {
        return false;
    }

    @Override
    public boolean isInfix() {
        return false;
    }

    @Override
    public boolean isExternal() {
        return isExternal;
    }

    @Override
    public boolean isInline() {
        return false;
    }

    @Override
    public boolean isTailrec() {
        return false;
    }

    @NotNull
    @Override
    public FunctionDescriptor substitute(@NotNull TypeSubstitutor substitutor) {
        throw new UnsupportedOperationException(); // TODO
    }

    @NotNull
    @Override
    public List<TypeParameterDescriptor> getTypeParameters() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasStableParameterNames() {
        return false;
    }

    @Override
    public boolean hasSynthesizedParameterNames() {
        return false;
    }

    @NotNull
    @Override
    public Modality getModality() {
        return modality;
    }

    @NotNull
    @Override
    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    @Override
    @NotNull
    public PropertyDescriptor getCorrespondingProperty() {
        return correspondingProperty;
    }

    @Nullable
    @Override
    public ReceiverParameterDescriptor getExtensionReceiverParameter() {
        return getCorrespondingProperty().getExtensionReceiverParameter();
    }

    @Nullable
    @Override
    public ReceiverParameterDescriptor getDispatchReceiverParameter() {
        return getCorrespondingProperty().getDispatchReceiverParameter();
    }

    @NotNull
    @Override
    public CopyBuilder<? extends FunctionDescriptor> newCopyBuilder() {
        throw new UnsupportedOperationException("Accessors must be copied by the corresponding property");
    }

    @NotNull
    @Override
    public PropertyAccessorDescriptor copy(
            DeclarationDescriptor newOwner,
            Modality modality,
            Visibility visibility,
            Kind kind,
            boolean copyOverrides
    ) {
        throw new UnsupportedOperationException("Accessors must be copied by the corresponding property");
    }

    @NotNull
    protected Collection<PropertyAccessorDescriptor> getOverriddenDescriptors(boolean isGetter) {
        Collection<PropertyAccessorDescriptor> result = new ArrayList<PropertyAccessorDescriptor>(0);
        for (PropertyDescriptor overriddenProperty : getCorrespondingProperty().getOverriddenDescriptors()) {
            PropertyAccessorDescriptor accessorDescriptor = isGetter ? overriddenProperty.getGetter() : overriddenProperty.getSetter();
            if (accessorDescriptor != null) {
                result.add(accessorDescriptor);
            }
        }
        return result;
    }

    @Override
    public void setOverriddenDescriptors(@NotNull Collection<? extends CallableMemberDescriptor> overriddenDescriptors) {
        assert overriddenDescriptors.isEmpty() : "Overridden accessors should be empty";
    }

    @NotNull
    @Override
    public abstract PropertyAccessorDescriptor getOriginal();

    @Override
    @Nullable
    public FunctionDescriptor getInitialSignatureDescriptor() {
        return initialSignatureDescriptor;
    }

    public void setInitialSignatureDescriptor(@Nullable FunctionDescriptor initialSignatureDescriptor) {
        this.initialSignatureDescriptor = initialSignatureDescriptor;
    }

    @Override
    public boolean isHiddenToOvercomeSignatureClash() {
        return false;
    }
}
