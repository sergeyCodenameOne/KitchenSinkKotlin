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

package kotlin.reflect.jvm.internal.impl.load.java.structure

import kotlin.reflect.jvm.internal.impl.descriptors.Visibility
import kotlin.reflect.jvm.internal.impl.name.ClassId
import kotlin.reflect.jvm.internal.impl.name.FqName
import kotlin.reflect.jvm.internal.impl.name.Name

interface JavaElement

interface JavaNamedElement : JavaElement {
    val name: Name
}

interface JavaAnnotationOwner : JavaElement {
    val annotations: Collection<JavaAnnotation>
    fun findAnnotation(fqName: FqName): JavaAnnotation?

    val isDeprecatedInJavaDoc: Boolean
}

interface JavaModifierListOwner : JavaElement {
    val isAbstract: Boolean
    val isStatic: Boolean
    val isFinal: Boolean
    val visibility: Visibility
}

interface JavaTypeParameterListOwner : JavaElement {
    val typeParameters: List<JavaTypeParameter>
}

interface JavaAnnotation : JavaElement {
    val arguments: Collection<JavaAnnotationArgument>
    val classId: ClassId?

    fun resolve(): JavaClass?
}

interface JavaPackage : JavaElement {
    val fqName: FqName
    val subPackages: Collection<JavaPackage>

    fun getClasses(nameFilter: (Name) -> Boolean): Collection<JavaClass>
}

interface JavaClassifier : JavaNamedElement, JavaAnnotationOwner

interface JavaClass : JavaClassifier, JavaTypeParameterListOwner, JavaModifierListOwner {
    val fqName: FqName?

    val supertypes: Collection<JavaClassifierType>
    val innerClasses: Collection<JavaClass>
    val outerClass: JavaClass?

    val isInterface: Boolean
    val isAnnotationType: Boolean
    val isEnum: Boolean
    val lightClassOriginKind: LightClassOriginKind?

    val methods: Collection<JavaMethod>
    val fields: Collection<JavaField>
    val constructors: Collection<JavaConstructor>
}

enum class LightClassOriginKind {
    SOURCE, BINARY
}

interface JavaMember : JavaModifierListOwner, JavaAnnotationOwner, JavaNamedElement {
    val containingClass: JavaClass
}

interface JavaMethod : JavaMember, JavaTypeParameterListOwner {
    val valueParameters: List<JavaValueParameter>
    val returnType: JavaType

    val hasAnnotationParameterDefaultValue: Boolean
}

interface JavaField : JavaMember {
    val isEnumEntry: Boolean
    val type: JavaType
}

interface JavaConstructor : JavaMember, JavaTypeParameterListOwner {
    val valueParameters: List<JavaValueParameter>
}

interface JavaValueParameter : JavaAnnotationOwner {
    val name: Name?
    val type: JavaType
    val isVararg: Boolean
}

interface JavaTypeParameter : JavaClassifier {
    val upperBounds: Collection<JavaClassifierType>
}
