package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.configurations.MaskingConstants;
import com.hashedin.redmask.configurations.TemplateConfiguration;
import com.hashedin.redmask.exception.InvalidParameterValueException;
import com.hashedin.redmask.exception.UnknownParameterException;
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

public class FixedSizeIntegerMasking extends MaskingRuleDef {
  private static final Logger log = LogManager.getLogger(BigIntRangeMasking.class);

  private static final String PARAM_SIZE = "size";
  private static final String PARAM_SIZE_DEFAULT = "2";

  public FixedSizeIntegerMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  public FixedSizeIntegerMasking() {
  }

  @Override
  public void addFunctionDefinition(TemplateConfiguration config, Set<String> funcSet) {
    try {
      funcSet.add(MaskingQueryUtil.maskIntegerInRange(config));
      funcSet.add(MaskingQueryUtil.maskIntegerFixedSize(config));
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
            MaskingConstants.MASK_INTEGER_FIXED_SIZE_FUNC, paramsList);
      }
    } catch (IOException | TemplateException ex) {
      log.error("Error occurred while adding MaskFunction for Mask Type {} ", this.getMaskType());
    }
    return this.getColumnName();

  }

  private boolean validateAndAddParameters(List<String> parameters)
      throws InvalidParameterValueException, UnknownParameterException {
    for (String key : this.getMaskParams().keySet()) {
      if (!key.equals(PARAM_SIZE)) {
        throw new UnknownParameterException("Unrecognised parameter" + key + " supplied to "
            + this.getMaskType() + " for column " + this.getColumnName());
      }
    }
    if (this.getMaskParams().isEmpty() || this.getMaskParams() == null) {
      parameters.add(PARAM_SIZE);
      return true;
    }
    int size = Integer.parseInt(this.getMaskParams().getOrDefault(PARAM_SIZE, PARAM_SIZE_DEFAULT));
    if (size > 0) {
      parameters.add(String.valueOf(size));
      return true;
    } else {
      throw new InvalidParameterValueException(
          String.format("\'%s\' value should be greater than 0", PARAM_SIZE));
    }
  }
}
