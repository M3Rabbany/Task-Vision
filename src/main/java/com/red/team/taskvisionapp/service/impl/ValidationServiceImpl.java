//package com.red.team.taskvisionapp.service.impl;
//
//import jakarta.validation.ConstraintViolation;
//import jakarta.validation.ConstraintViolationException;
//import jakarta.validation.Validator;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.Set;
//
//@Service
//@RequiredArgsConstructor
//public class ValidationServiceImpl {
//    private final Validator validator;
//
//    public void validate(Object request){
//        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(request);
//
//        if(!constraintViolations.isEmpty()){
//            throw new ConstraintViolationException(constraintViolations);
//        }
//    }
//}
