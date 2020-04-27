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
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FixedValueFloatMasking extends MaskingRuleDef {
  private static final Logger log = LogManager.getLogger(BigIntRangeMasking.class);

  private static final String PARAM_VALUE = "value";

  private static final String PARAM_VALUE_DEFAULT = "0.00";


  public FixedValueFloatMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  public FixedValueFloatMasking() {
  }

  @Override
  public void addFunctionDefinition(TemplateConfiguration config, Set<String> funcSet) {
    try {
      funcSet.add(MaskingQueryUtil.maskFloatFixedValue(config));
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
      if (this.validateAndAddParameters(paramsList)) {
        return MaskingQueryUtil.processQueryTemplate(config,
            MaskingConstants.MASK_FLOAT_FIXED_VALUE_FUNC, paramsList);
      }
    } catch (IOException | TemplateException ex) {
      log.error("Error occurred while adding MaskFunction for Mask Type {} ", this.getMaskType());
    }
    return this.getColumnName();
  }

  private boolean validateAndAddParameters(List<String> parameters)
      throws InvalidParameterValueException, UnknownParameterException {
    for (String key : this.getMaskParams().keySet()) {
      if (!key.equals(PARAM_VALUE)) {
        throw new UnknownParameterException("Unrecognised parameter" + key + " supplied to "
            + this.getMaskType() + " for column " + this.getColumnName());
      }
    }
    if (this.getMaskParams().isEmpty() || this.getMaskParams() == null) {
      parameters.add(PARAM_VALUE);
      return true;
    }
    float value = Float.parseFloat(this.getMaskParams()
        .getOrDefault(PARAM_VALUE, PARAM_VALUE_DEFAULT));
    parameters.add(String.valueOf(value));
    return true;
  }
}
