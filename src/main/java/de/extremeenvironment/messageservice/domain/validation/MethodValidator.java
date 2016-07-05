package de.extremeenvironment.messageservice.domain.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by on 05.07.16.
 *
 * @author David Steiman
 */
public class MethodValidator implements ConstraintValidator<ValidateMethod, Object> {

    Logger log = LoggerFactory.getLogger(getClass());

    private String validateMethodName;

    @Override
    public void initialize(ValidateMethod validateMethod) {
        validateMethodName = validateMethod.method();
    }

    @Override
    public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
        Method validatingMethod = ReflectionUtils.findMethod(o.getClass(), validateMethodName, new Class[]{});
        if (validatingMethod != null) {
            try {
                return (Boolean) validatingMethod.invoke(o);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("validation method error: {}", e.getMessage());
                return false;
            }
        } else {
            log.error("validator method '{}' not found", validateMethodName);
            return false;
        }
    }
}
