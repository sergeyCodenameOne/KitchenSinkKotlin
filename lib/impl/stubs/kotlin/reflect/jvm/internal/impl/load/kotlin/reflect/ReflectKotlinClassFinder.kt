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

package kotlin.reflect.jvm.internal.impl.load.kotlin.reflect

import kotlin.reflect.jvm.internal.impl.load.java.reflect.tryLoadClass
import kotlin.reflect.jvm.internal.impl.load.java.structure.JavaClass
import kotlin.reflect.jvm.internal.impl.load.kotlin.KotlinClassFinder
import kotlin.reflect.jvm.internal.impl.load.kotlin.KotlinJvmBinaryClass
import kotlin.reflect.jvm.internal.impl.name.ClassId

class ReflectKotlinClassFinder(private val classLoader: ClassLoader) : KotlinClassFinder {
    private fun findKotlinClass(fqName: String): KotlinJvmBinaryClass? {
        return classLoader.tryLoadClass(fqName)?.let { ReflectKotlinClass.create(it) }
    }

    override fun findKotlinClass(classId: ClassId) = findKotlinClass(classId.toRuntimeFqName())

    override fun findKotlinClass(javaClass: JavaClass): KotlinJvmBinaryClass? {
        // TODO: go through javaClass's class loader
        return findKotlinClass(javaClass.fqName?.asString() ?: return null)
    }
}

private fun ClassId.toRuntimeFqName(): String {
    val className = relativeClassName.asString().replace('.', '$')
    return if (packageFqName.isRoot) className else "${packageFqName}.$className"
}
