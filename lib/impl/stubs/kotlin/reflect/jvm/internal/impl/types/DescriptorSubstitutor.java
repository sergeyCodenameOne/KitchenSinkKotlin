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

package kotlin.reflect.jvm.internal.impl.types;

import org.jetbrains.annotations.Mutable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ReadOnly;
import kotlin.reflect.jvm.internal.impl.descriptors.DeclarationDescriptor;
import kotlin.reflect.jvm.internal.impl.descriptors.SourceElement;
import kotlin.reflect.jvm.internal.impl.descriptors.TypeParameterDescriptor;
import kotlin.reflect.jvm.internal.impl.descriptors.impl.TypeParameterDescriptorImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DescriptorSubstitutor {
    private DescriptorSubstitutor() {
    }

    @NotNull
    public static TypeSubstitutor substituteTypeParameters(
            @ReadOnly @NotNull List<TypeParameterDescriptor> typeParameters,
            @NotNull TypeSubstitution originalSubstitution,
            @NotNull DeclarationDescriptor newContainingDeclaration,
            @NotNull @Mutable List<TypeParameterDescriptor> result
    ) {
        Map<TypeConstructor, TypeProjection> mutableSubstitution = new HashMap<TypeConstructor, TypeProjection>();

        Map<TypeParameterDescriptor, TypeParameterDescriptorImpl> substitutedMap = new HashMap<TypeParameterDescriptor, TypeParameterDescriptorImpl>();
        int index = 0;
        for (TypeParameterDescriptor descriptor : typeParameters) {
            TypeParameterDescriptorImpl substituted = TypeParameterDescriptorImpl.createForFurtherModification(
                    newContainingDeclaration,
                    descriptor.getAnnotations(),
                    descriptor.isReified(),
                    descriptor.getVariance(),
                    descriptor.getName(),
                    index++,
                    SourceElement.NO_SOURCE
            );

            mutableSubstitution.put(descriptor.getTypeConstructor(), new TypeProjectionImpl(substituted.getDefaultType()));

            substitutedMap.put(descriptor, substituted);
            result.add(substituted);
        }

        TypeSubstitutor substitutor = TypeSubstitutor.createChainedSubstitutor(
                originalSubstitution, TypeConstructorSubstitution.createByConstructorsMap(mutableSubstitution)
        );

        for (TypeParameterDescriptor descriptor : typeParameters) {
            TypeParameterDescriptorImpl substituted = substitutedMap.get(descriptor);
            for (KotlinType upperBound : descriptor.getUpperBounds()) {
                KotlinType substitutedBound = substitutor.substitute(upperBound, Variance.IN_VARIANCE);
                assert substitutedBound != null : "Upper bound failed to substitute: " + descriptor;
                substituted.addUpperBound(substitutedBound);
            }
            substituted.setInitialized();
        }

        return substitutor;
    }
}