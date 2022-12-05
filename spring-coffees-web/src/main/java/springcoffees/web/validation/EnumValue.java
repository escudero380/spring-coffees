package springcoffees.web.validation;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.*;
import java.util.Arrays;

/*
 *  A validation constraint annotation and its validator to guarantee
 *  that field's value matches enum constants of the given enum type.
 */

@Deprecated
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValue.EnumValidator.class)
public @interface EnumValue {

    String message() default "Wrong value was previously entered (chose proper one and re-submit the form)";
    Class<? extends Enum<?>> enumType();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};


    class EnumValidator implements ConstraintValidator<EnumValue, String> {
        private Class<? extends Enum<?>> enumClass;

        @Override
        public void initialize(EnumValue constraintAnnotation) {
            this.enumClass = constraintAnnotation.enumType();
        }

        @Override
        public boolean isValid(String field, ConstraintValidatorContext constraintValidatorContext) {
            return Arrays.stream(enumClass.getEnumConstants())
                    .map(Enum::name)
                    .anyMatch(field::equals);
        }
    }

}

