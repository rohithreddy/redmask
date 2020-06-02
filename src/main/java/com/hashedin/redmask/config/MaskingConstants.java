package com.hashedin.redmask.config;

import java.util.Arrays;
import java.util.List;

public class MaskingConstants {
  
  private MaskingConstants() {
  //not called
  }

  public static final String DB_SUPER_USER = "DB_SUPER_USER";
  public static final String DB_SUPER_USER_PASSWORD = "DB_SUPER_USER_PASSWORD";
  public static final String DB_USER = "DB_USER";
  public static final String DB_USER_PASSWORD = "DB_USER_PASSWORD";

  public static final String MASK_STRING_FUNC = "anonymizeString";
  public static final String MASK_STRING_FILE = "/strings/AnonymizeString.sql";
  public static final String MASK_STRING_COMMENT = 
      "\n\n-- Function to anonymize the string field.\n";

  public static final String MASK_EMAIL_FUNC = "emailmask";
  public static final String MASK_EMAIL_FILE = "/SpecializedFunctions/Email.sql";
  public static final String MASK_EMAIL_COMMENT = "\n\n-- Function to mask email type.\n";

  public static final String MASK_INTEGER_FIXED_SIZE_FUNC = "generate";
  public static final String MASK_INTEGER_FIXED_SIZE_FILE =
      "/Integer_float/Generate.sql";
  public static final String MASK_INTEGER_FIXED_SIZE_COMMENT =
      "\n\n-- Function to generate random number of fixed length.\n";

  public static final String MASK_INTEGER_WITHIN_RANGE_FUNC = "random_int_between";
  public static final String MASK_INTEGER_WITHIN_RANGE_FILE =
      "/Integer_float/RandomInt.sql";
  public static final String MASK_INTEGER_WITHIN_RANGE_COMMENT =
      "\n\n-- Function to generate a random number between a given range.\n";

  public static final String MASK_INTEGER_FIXED_VALUE_FUNC = "replaceby";
  public static final String MASK_INTEGER_FIXED_VALUE_FILE =
      "/Integer_float/ReplaceByInteger.sql";
  public static final String MASK_INTEGER_FIXED_VALUE_COMMENT =
      "\n\n-- Function to anonymize the integer field with given value.\n";

  public static final String MASK_FLOAT_FIXED_VALUE_FUNC = "replaceby";
  public static final String MASK_FLOAT_FIXED_VALUE_FILE =
      "/Integer_float/ReplaceByFloat.sql";
  public static final String MASK_FLOAT_FIXED_VALUE_COMMENT =
      "\n\n-- Function to anonymize the float field with given value.\n";

  public static final String MASK_NUMERIC_RANGE_FUNC = "range_numeric";
  public static final String MASK_NUMERIC_RANGE_FILE =
      "/Integer_float/RangeNumeric.sql";
  public static final String MASK_NUMERIC_RANGE_COMMENT =
      "\n\n-- Function to convert numeric type to a range.\n";

  public static final String MASK_INTEGER_RANGE_FUNC = "range_int4";
  public static final String MASK_INTEGER_RANGE_FILE =
      "/Integer_float/RangeInt4.sql";
  public static final String MASK_INTEGER_RANGE_COMMENT =
      "\n\n-- Function to convert to integer range.\n";

  public static final String MASK_BIGINT_RANGE_FUNC = "range_int8";
  public static final String MASK_BIGINT_RANGE_FILE =
      "/Integer_float/RangeInt8.sql";
  public static final String MASK_BIGINT_RANGE_COMMENT =
      "\n\n-- Function to convert to big integer range.\n";

  public static final String MASK_NUMBERS_FUNC = "anonymize_numbers";
  public static final String MASK_NUMBERS_FILE = "/strings/AnonymizeNumber.sql";
  public static final String MASK_NUMBERS_COMMENT =
      "\n\n-- Function to anonymize number in a string.\n";

  public static final String MASK_CARD_FUNC = "cardmask";
  public static final String MASK_CARD_FILE =
      "/SpecializedFunctions/CardMask.sql";
  public static final String MASK_CARD_COMMENT =
      "\n\n-- Function to anonymize card details.\n";

  public static final List<MaskType> REDSHIFT_SNOWFLAKE_NOT_SUPPORTED_MASK_TYPES =
      Arrays.asList(MaskType.NUMERIC_RANGE, MaskType.BIGINT_RANGE, MaskType.INTEGER_RANGE);
}
