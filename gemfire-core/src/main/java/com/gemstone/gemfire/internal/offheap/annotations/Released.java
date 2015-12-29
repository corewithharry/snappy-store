/*
 * Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
package com.gemstone.gemfire.internal.offheap.annotations;

import static com.gemstone.gemfire.internal.offheap.annotations.OffHeapIdentifier.*;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a reference count decrement to an off-heap value:
 * <ul>
 * <li>Use this annotation on a parameter declaration to indicate that the method will decrement (call release) on the off-heap reference.</li>
 * <li>When used on method declarations this annotation indicates that the method will call release on one or more field members of the class instance.</li>
 * <li>When used on a local variable this annotation indicates that the variable will be released within the containing method (typically in a finally block).</li>
 * <li>Can also be used on fields to augment the @Retained annotation.</li>
 * </ul>
 * 
 * One or more OffHeapIdentifiers may be supplied if the developer wishes to link this annotation with other
 * off-heap annotations.
 * 
 * @author rholmes
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.PARAMETER,
              ElementType.METHOD,
              ElementType.LOCAL_VARIABLE,
              ElementType.FIELD})
@Documented
public @interface Released {
  OffHeapIdentifier[] value() default DEFAULT;
}
