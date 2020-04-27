package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.configurations.MaskingConstants;
import com.hashedin.redmask.configurations.TemplateConfiguration;
import com.hashedin.redmask.exception.InvalidParameterValueException;
import com.hashedin.redmask.exception.UnknownParameterException;
import com.hashedin.redmask.service.MaskingQueryUtil;
import com.hashedin.redmask.service.MaskingRuleDef;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringMasking extends MaskingRuleDef {

  private static final Logger log = LoggerFactory.getLogger(StringMasking.class);
  
  private static final String PARAM_REPLACEMENT_PATTERN = "pattern";
  private static final String PARAM_PATTERN_DEFAULT = "*";

  private static final String PARAM_SHOW_FIRST = "show_first";
  private static final String PARAM_SHOW_FIRST_DEFAULT = "0";

  private static final String PARAM_SHOW_LAST = "show_last";
  private static final String PARAM_SHOW_LAST_DEFAULT = "0";

  private static final List<String> EXPECTED_PARAMETERS_LIST = new ArrayList<String>(
      Arrays.asList(PARAM_REPLACEMENT_PATTERN, PARAM_SHOW_FIRST, PARAM_SHOW_LAST));

  public StringMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  public StringMasking() {
  }

  @Override
  public void addFunctionDefinition(TemplateConfiguration config, Set<String> funcSet) {
    try {
      funcSet.add(MaskingQueryUtil.maskString(config));
      log.info("Function added for Mask Type {}", this.getMaskType());
    } catch (IOException | TemplateException ex) {
      log.error("Error occurred while adding MaskFunction for Mask Type {} ",
          this.getMaskType());
    }
  }

  @Override
  public String getSubQuery(TemplateConfiguration config, String tableName)
      throws InvalidParameterValueException, UnknownParameterException {
    List<String> paramsList = new ArrayList<>();
    paramsList.add(this.getColumnName());
    try {
      if (validateAndAddParameters(paramsList)) {
        return MaskingQueryUtil.processQueryTemplate(
            config, MaskingConstants.MASK_STRING_FUNC, paramsList);
      }
    } catch (IOException | TemplateException ex) {
      log.error("Error occurred while adding MaskFunction for Mask Type {} ",
          this.getMaskType());
    }
    return this.getColumnName();
  }

  private boolean validateAndAddParameters(List<String> parameters)
      throws InvalidParameterValueException, UnknownParameterException {
    for (String key : this.getMaskParams().keySet()) {
      if (!EXPECTED_PARAMETERS_LIST.contains(key)) {
        throw new UnknownParameterException("Unrecognised parameter" + key + " supplied to "
            + this.getMaskType() + " for column " + this.getColumnName());
      }
    }

    if (this.getMaskParams().isEmpty() || this.getMaskParams() == null) {
      parameters.addAll(Arrays.asList(PARAM_PATTERN_DEFAULT,
          PARAM_SHOW_FIRST_DEFAULT, PARAM_SHOW_LAST_DEFAULT));
    }
    String pattern = this.getMaskParams()
        .getOrDefault(PARAM_REPLACEMENT_PATTERN, PARAM_PATTERN_DEFAULT);
    int prefix = Integer.parseInt(this.getMaskParams()
        .getOrDefault(PARAM_SHOW_FIRST, PARAM_SHOW_FIRST_DEFAULT));
    int suffix = Integer.parseInt(this.getMaskParams()
        .getOrDefault(PARAM_SHOW_LAST, PARAM_SHOW_LAST_DEFAULT));

    if (prefix < 0) {
      throw new InvalidParameterValueException(
          String.format("\'%s\' value should be greater than or equal to 0", PARAM_SHOW_FIRST));

    }
    if (suffix < 0) {
      throw new InvalidParameterValueException(
          String.format("\'%s\' value should be greater than or equal to 0", PARAM_SHOW_LAST));
    }
    parameters.add(pattern);
    parameters.add(String.valueOf(prefix));
    parameters.add(String.valueOf(suffix));
    return true;
  }
}