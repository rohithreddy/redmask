package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.InvalidParameterValueException;
import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.configurations.MaskingConstants;
import com.hashedin.redmask.configurations.MissingParameterException;
import com.hashedin.redmask.configurations.TemplateConfiguration;
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

public class InRangeIntegerMasking extends MaskingRuleDef {
  private static final Logger log = LogManager.getLogger(BigIntRangeMasking.class);


  public InRangeIntegerMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  InRangeIntegerMasking() {
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
  public String getSubQuery(TemplateConfiguration config, String tableName) throws MissingParameterException {
    List<String> paramsList = new ArrayList<>();
    paramsList.add(this.getColumnName());
    try {
      if (validateAndAddParameters(paramsList)) {
        return MaskingQueryUtil.processQueryTemplate(config, MaskingConstants.MASK_INTEGER_WITHIN_RANGE_FUNC, paramsList);
      } else {
        throw new MissingParameterException("Expected parameters: min, max ");
      }
    } catch (IOException | TemplateException ex) {
      log.error("Error occurred while adding MaskFunction for Mask Type {} ", this.getMaskType());
    }
    return this.getColumnName();
  }

  protected boolean validateAndAddParameters(List<String> parameters) {
    if ((this.getMaskParams().containsKey("min")) && (this.getMaskParams().containsKey("max"))) {
      int min = Integer.parseInt(this.getMaskParams().get("min"));
      int max = Integer.parseInt(this.getMaskParams().get("max"));
      if (max > min) {
        parameters.add(String.valueOf(min));
        parameters.add(String.valueOf(max));
        return true;
      } else {
        throw new InvalidParameterValueException("\'max\' should be greater than \'min\'");
      }
    }
    return false;
  }
}
