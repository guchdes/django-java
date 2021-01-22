/**
 * MIT License
 *
 * Copyright (c) 2021 the original author or authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.guchdes.django.bson.projection.pojo;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

/**
 * The default Conventions
 *
 * @since 3.5
 * @see Convention
 */
public final class Conventions {

    /**
     * The default class and property conventions
     *
     * <ul>
     *     <li>Sets the discriminator key if not set to {@code _t} and the discriminator value if not set to the
     *     ClassModels simple type name.</li>
     *     <li>Configures the PropertyModels. If the {@code idProperty} isn't set and there is a
     *     property named {@code getId()}, {@code id} or {@code _id} it will be marked as the idProperty.</li>
     * </ul>
     */
    public static final Convention CLASS_AND_PROPERTY_CONVENTION = new ConventionDefaultsImpl();

    /**
     * The annotation convention.
     *
     * <p>Applies all the conventions related to the default annotations.</p>
     */
    public static final Convention ANNOTATION_CONVENTION = new ConventionAnnotationImpl();

    /**
     * A convention that enables private fields to be set using reflection.
     *
     * <p>This convention mimics how some other JSON libraries directly set a private field when there is no setter.</p>
     * <p>Note: This convention is not part of the {@code DEFAULT_CONVENTIONS} list and must explicitly be set.</p>
     *
     * @since 3.6
     */
    public static final Convention SET_PRIVATE_FIELDS_CONVENTION = new ConventionSetPrivateFieldImpl();

    /**
     * A convention that uses getter methods as setters for collections and maps if there is no setter.
     *
     * <p>This convention mimics how JAXB mutate collections and maps.</p>
     * <p>Note: This convention is not part of the {@code DEFAULT_CONVENTIONS} list and must explicitly be set.</p>
     *
     * @since 3.6
     */
    public static final Convention USE_GETTERS_FOR_SETTERS = new ConventionUseGettersAsSettersImpl();


    /**
     * A convention that sets the IdGenerator if the id property is either a {@link org.bson.types.ObjectId} or
     * {@link org.bson.BsonObjectId}.
     *
     * @since 3.10
     */
    public static final Convention OBJECT_ID_GENERATORS = new ConventionObjectIdGeneratorsImpl();

    /**
     * The default conventions list
     */
    public static final List<Convention> DEFAULT_CONVENTIONS =
            unmodifiableList(asList(CLASS_AND_PROPERTY_CONVENTION, ANNOTATION_CONVENTION, OBJECT_ID_GENERATORS));

    /**
     * An empty conventions list
     */
    public static final List<Convention> NO_CONVENTIONS = Collections.emptyList();

    private Conventions() {
    }
}
