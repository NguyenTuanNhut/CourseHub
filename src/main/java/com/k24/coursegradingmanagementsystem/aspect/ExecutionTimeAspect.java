package com.k24.coursegradingmanagementsystem.aspect;

import com.k24.coursegradingmanagementsystem.dto.request.GradeSubmissionRequest;
import com.k24.coursegradingmanagementsystem.dto.response.GradeResponse;
import com.k24.coursegradingmanagementsystem.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Aspect
@Component
@Slf4j
public class ExecutionTimeAspect {

    @Around("@annotation(logExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint, LogExecutionTime logExecutionTime) throws Throwable {
        long start = System.nanoTime();
        String action = logExecutionTime.action();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        Long userId = null;
        String role = null;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            userId = userDetails.getUser().getId();
            role = userDetails.getUser().getRole().name();
        }

        Object result;
        try {
            result = joinPoint.proceed();
            long durationMs = (System.nanoTime() - start) / 1_000_000;

            log.info("timestamp={} action={} class={} method={} userId={} role={} durationMs={} status=SUCCESS",
                    Instant.now(), action, className, methodName, userId, role, durationMs);

            return result;
        } catch (Throwable ex) {
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            log.error("timestamp={} action={} class={} method={} userId={} role={} durationMs={} status=FAILED exceptionType={}",
                    Instant.now(), action, className, methodName, userId, role, durationMs, ex.getClass().getSimpleName());
            throw ex;
        }
    }

    @AfterReturning(pointcut = "execution(* com.k24.coursegradingmanagementsystem.service.GradeService.gradeSubmission(..))", returning = "result")
    public void logSuccessfulGrading(JoinPoint joinPoint, Object result) {
        if (result instanceof GradeResponse grade) {
            log.info("Lecturer ID: {} graded Submission ID: {} with Score: {}",
                    grade.getLecturerId(), grade.getSubmissionId(), grade.getScore());

            log.info("action=GRADE_SUBMISSION lecturerId={} submissionId={} score={} status=SUCCESS",
                    grade.getLecturerId(), grade.getSubmissionId(), grade.getScore());
        }
    }

    @AfterThrowing(pointcut = "execution(* com.k24.coursegradingmanagementsystem.service.GradeService.gradeSubmission(..))", throwing = "ex")
    public void logFailedGrading(JoinPoint joinPoint, Throwable ex) {
        Object[] args = joinPoint.getArgs();
        Long lecturerId = null;
        Long submissionId = null;
        Double score = null;
        if (args.length > 0 && args[0] instanceof Long) {
            lecturerId = (Long) args[0];
        }
        if (args.length > 1 && args[1] instanceof GradeSubmissionRequest request) {
            submissionId = request.getSubmissionId();
            score = request.getScore();
        }
        log.error("action=GRADE_SUBMISSION lecturerId={} submissionId={} score={} status=FAILED exceptionType={}",
                lecturerId, submissionId, score, ex.getClass().getSimpleName());
    }
}
