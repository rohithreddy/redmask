package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.configurations.MaskingConstants;
import com.hashedin.redmask.service.MaskingQueryUtil;
import com.hashedin.redmask.service.MaskingRuleDef;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FixedValueIntegerMasking extends MaskingRuleDef {

  public FixedValueIntegerMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  FixedValueIntegerMasking() {
  }

  @Override
  public void addFunctionDefinition(MaskConfiguration config, Set<String> funcSet) throws IOException, TemplateException {
    funcSet.add(MaskingQueryUtil.maskIntegerFixedValue(config));
  }

  @Override
  public String getSubQuery(MaskConfiguration config, String tableName) throws IOException, TemplateException {
    List<String> paramsList = new ArrayList<>();
    paramsList.add(this.getColumnName());
    if (validateAndAddParameters(paramsList))
      return MaskingQueryUtil.processQueryTemplate(config, MaskingConstants.MASK_INTEGER_FIXED_VALUE_FUNC, paramsList);
    return this.getColumnName();
  }

  @Override
  protected boolean validateAndAddParameters(List<String> parameters) {
    if (this.getMaskParams().containsKey("value")) {
      // to assure the value is of Integer type
      int value = Integer.parseInt(this.getMaskParams().get("value"));
      parameters.add(String.valueOf(value));
      return true;
    }
    return false;
  }
}
