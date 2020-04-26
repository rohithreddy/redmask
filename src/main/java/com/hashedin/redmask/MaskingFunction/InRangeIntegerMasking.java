package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.InvalidParameterValueException;
import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.configurations.MaskingConstants;
import com.hashedin.redmask.configurations.TemplateConfiguration;
import com.hashedin.redmask.configurations.UnknownParameterException;
import com.hashedin.redmask.service.MaskingQueryUtil;
import com.hashedin.redmask.service.MaskingRuleDef;
import freemarker.template.TemplateException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InRangeIntegerMasking extends MaskingRuleDef {
  private static final Logger log = LogManager.getLogger(BigIntRangeMasking.class);

  private static final String PARAM_MINIMUM = "min";
  private static final String PARAM_MINIMUM__DEFAULT = "0";

  private static final String PARAM_MAXIMUM = "max";
  private static final String PARAM_MAXIMUM__DEFAULT = "10";

  private static final List<String> expectedParametersList = new ArrayList<String>(
      Arrays.asList(PARAM_MAXIMUM, PARAM_MINIMUM));

  public InRangeIntegerMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  public InRangeIntegerMasking() {
  }

  @Override
  public void addFunctionDefinition(TemplateConfiguration config, Set<String> funcSet) {
    try {
      funcSet.add(MaskingQueryUtil.maskIntegerInRange(config));
      log.info("Function added for Mask Type {}", this.getMaskType());
    } catch (IOException | TemplateException ex) {
      log.error("Error occurred while adding MaskFunction for Mask Type {} ", this.getMaskType());
    }
  }

  @Override
  public String getSubQuery(TemplateConfiguration config, String tableName)
      throws InvalidParameterValueException, UnknownParameterException {
    List<String> paramsList = new ArrayList<>();
    paramsList.add(this.getColumnName());
    try {
      if (validateAndAddParameters(paramsList)) {
        return MaskingQueryUtil.processQueryTemplate(config,
            MaskingConstants.MASK_INTEGER_WITHIN_RANGE_FUNC, paramsList);
      }
    } catch (IOException | TemplateException ex) {
      log.error("Error occurred while adding MaskFunction for Mask Type {} ", this.getMaskType());
    }
    return this.getColumnName();
  }

  private boolean validateAndAddParameters(List<String> parameters)
      throws InvalidParameterValueException, UnknownParameterException {
    for (String key : this.getMaskParams().keySet()) {
      if (!expectedParametersList.contains(key)) {
        throw new UnknownParameterException("Unrecognised parameter" + key + " supplied to "
            + this.getMaskType() + " for column " + this.getColumnName());
      }
    }

    if (this.getMaskParams().isEmpty() || this.getMaskParams() == null) {
      parameters.addAll(Arrays.asList(PARAM_MINIMUM__DEFAULT, PARAM_MAXIMUM__DEFAULT));
    }

    int min = Integer.parseInt(this.getMaskParams()
        .getOrDefault(PARAM_MINIMUM, PARAM_MINIMUM__DEFAULT));
    int max = Integer.parseInt(this.getMaskParams()
        .getOrDefault(PARAM_MAXIMUM, PARAM_MAXIMUM__DEFAULT));
    if (max > min) {
      parameters.add(String.valueOf(min));
      parameters.add(String.valueOf(max));
      return true;
    } else {
      throw new InvalidParameterValueException(
          String.format("\'%s\' should be greater than \'%s\'", PARAM_MAXIMUM, PARAM_MINIMUM));
    }
  }
}
