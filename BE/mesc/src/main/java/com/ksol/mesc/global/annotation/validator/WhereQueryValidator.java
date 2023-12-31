package com.ksol.mesc.global.annotation.validator;

import com.ksol.mesc.global.annotation.WhereQuery;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class WhereQueryValidator implements ConstraintValidator<WhereQuery, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) {
			return false;
		}
		if (value.equals("")) {
			return true;
		}
		value = value.toLowerCase();
		return value.startsWith("where");
	}
}
