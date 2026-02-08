package de.innologic.iamservice.persistence.softdelete;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Blockiert CrudRepository.delete*(...) global.
 * Schalte es ab via: iam.persistence.prevent-hard-delete=false
 */
@Aspect
@Component
@ConditionalOnProperty(name = "iam.persistence.prevent-hard-delete", havingValue = "true", matchIfMissing = true)
public class PreventHardDeleteAspect {

    @Around("execution(* org.springframework.data.repository.CrudRepository+.delete*(..))")
    public Object blockDeleteCalls(ProceedingJoinPoint pjp) {
        throw new UnsupportedOperationException(
                "Hard delete is disabled. Use SoftDeleteService instead."
        );
    }
}
