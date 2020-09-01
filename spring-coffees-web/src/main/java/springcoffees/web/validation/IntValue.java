package springcoffees.web.validation;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.*;

/*
 *  A validation constraint annotation and its validator to guarantee
 *  that a field of string type is convertible into an integer value.
 */

@Deprecated
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IntValue.IntValidator.class)
public @interface IntValue {

    String message() default "This field may accept only integer values.";
    int min() default Integer.MIN_VALUE;
    int max() default Integer.MAX_VALUE;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};


    class IntValidator implements ConstraintValidator<IntValue, String> {
        private int min, max;

        @Override
        public void initialize(IntValue constraintAnnotation) {
            this.min = constraintAnnotation.min();
            this.max = constraintAnnotation.max();
        }

        @Override
        public boolean isValid(String field, ConstraintValidatorContext constraintValidatorContext) {
            try {
                int id = Integer.parseInt(field);
                return (id >= min && id <= max);
            } catch (NumberFormatException ex) {
                return false;
            }
        }
    }

}

