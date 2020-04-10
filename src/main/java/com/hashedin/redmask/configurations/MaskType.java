package com.hashedin.redmask.configurations;

public enum MaskType {

  STRING_MASKING,
  RANDOM_PHONE,
  DESTRUCTION,

  // Masking rule related to email field.
  EMAIL_SHOW_DOMAIN,
  EMAIL_SHOW_FIRST_CHARACTER_DOMAIN,
  EMAIL_SHOW_FIRST_CHARACTERS,
  EMAIL_MASK_ALPHANUMERIC,

  // Masking rule related to Integer and float field.
  RANDOM_INTEGER_FIXED_WIDTH,
  RANDOM_INTEGER_WITHIN_RANGE,
  INTEGER_FIXED_VALUE,
  FLOAT_FIXED_VALUE,

  // Masking integer/numeric field within a range.
  NUMERIC_RANGE,
  INTEGER_RANGE,
  BIGINT_RANGE,

  MEAN_VALUE,
  MODE_VALUE,

  // Masking rule related to Credit card field.
  CREDIT_CARD_SHOW_FIRST,
  CREDIT_CARD_SHOW_LAST,
  CREDIT_CARD_SHOW_FIRST_LAST

}
