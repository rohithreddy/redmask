package com.hashedin.redmask.configurations;

public class Constants {
  public static final String MASK_STRING_FUNC_NAME="anonymize";
  public static final String MASK_STRING_FILE = "src/main/resources/strings/AnonymizePartial.sql";
  public static final String MASK_STRING_COMMENT = "\n\n-- Postgres function to anonymize the string field.\n";

  public static final String MASK_EMAIL_FUNC_NAME="emailmask";
  public static final String MASK_EMAIL_FILE = "src/main/resources/SpecializedFunctions/Email.sql";
  public static final String MASK_EMAIL_COMMENT = "\n\n-- Postgres function to mask email type.\n";

  public static final String MASK_INTEGER_FIXED_SIZE_FUNC_NAME="generate";
  public static final String MASK_INTEGER_FIXED_SIZE_FILE = "src/main/resources/Integer_float/Generate.sql";
  public static final String MASK_INTEGER_FIXED_SIZE_COMMENT = "\n\n-- Postgres function to generate random number of fixed length.\n";

  public static final String MASK_INTEGER_WITHIN_RANGE_FUNC_NAME="random_int_between";
  public static final String MASK_INTEGER_WITHIN_RANGE_FILE = "src/main/resources/Integer_float/RandomInt.sql";
  public static final String MASK_INTEGER_WITHIN_RANGE_COMMENT = "\n\n-- Postgres function to generate a random number between a given range.\n";

  public static final String MASK_INTEGER_FIXED_VALUE_FUNC_NAME="replaceby";
  public static final String MASK_INTEGER_FIXED_VALUE_FILE = "src/main/resources/Integer_float/ReplaceByInteger.sql";
  public static final String MASK_INTEGER_FIXED_VALUE_COMMENT = "\n\n-- Postgres function to anonymize the integer field with given value.\n";

  public static final String MASK_FLOAT_FIXED_VALUE_FUNC_NAME="replaceby";
  public static final String MASK_FLOAT_FIXED_VALUE_FILE = "src/main/resources/Integer_float/ReplaceByFloat.sql";
  public static final String MASK_FLOAT_FIXED_VALUE_COMMENT = "\n\n-- Postgres function to anonymize the float field with given value.\n";

  public static final String MASK_NUMERIC_RANGE_FUNC_NAME="range_numeric";
  public static final String MASK_NUMERIC_RANGE_FILE = "src/main/resources/Integer_float/RangeNumeric.sql";
  public static final String MASK_NUMERIC_RANGE_COMMENT = "\n\n-- Postgres function to convert numeric type to a range.\n";

  public static final String MASK_INTEGER_RANGE_FUNC_NAME="range_int4";
  public static final String MASK_INTEGER_RANGE_FILE = "src/main/resources/Integer_float/RangeInt4.sql";
  public static final String MASK_INTEGER_RANGE_COMMENT = "\n\n-- Postgres function to convert to integer range.\n";

  public static final String MASK_BIGINT_RANGE_FUNC_NAME="range_int8";
  public static final String MASK_BIGINT_RANGE_FILE = "src/main/resources/Integer_float/RangeInt8.sql";
  public static final String MASK_BIGINT_RANGE_COMMENT = "\n\n-- Postgres function to convert to big integer range.\n";

  public static final String MASK_MEAN_FUNC_NAME="substitute_mean";
  public static final String MASK_MEAN_FILE = "src/main/resources/Integer_float/SubstituteMean.sql";
  public static final String MASK_MEAN_COMMENT = "\n\n-- Postgres function to anonymize the integer field by column mean.\n";

  public static final String MASK_MODE_FUNC_NAME="substitute_mode";
  public static final String MASK_MODE_FILE =  "src/main/resources/Integer_float/SubstituteMode.sql";
  public static final String MASK_MODE_COMMENT = "\n\n-- Postgres function to anonymize the integer field by column mode.\n";

  public static final String MASK_NUMBERS_FUNC_NAME="anonymize_numbers";
  public static final String MASK_NUMBERS_FILE = "src/main/resources/strings/AnonymizeNumber.sql";
  public static final String MASK_NUMBERS_COMMENT = "\n\n-- Postgres function to anonymize number in a string.\n";

  public static final String MASK_CARD_FUNC_NAME="cardmask";
  public static final String MASK_CARD_FILE = "src/main/resources/SpecializedFunctions/CardMask.sql";
  public static final String MASK_CARD_COMMENT = "\n\n-- Postgres function to anonymize card details.\n";
}
